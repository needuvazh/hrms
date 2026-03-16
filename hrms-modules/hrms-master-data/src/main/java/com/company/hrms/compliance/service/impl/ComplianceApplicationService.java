package com.company.hrms.compliance.service.impl;

import com.company.hrms.compliance.model.ComplianceModels;
import com.company.hrms.compliance.repository.ComplianceRepository;
import com.company.hrms.compliance.service.ComplianceModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ComplianceApplicationService implements ComplianceModuleApi {

    private static final String ACTOR = "system";

    private final ComplianceRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public ComplianceApplicationService(
            ComplianceRepository repository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher
    ) {
        this.repository = repository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> create(ComplianceModels.Resource resource, ComplianceModels.MasterUpsertRequest request) {
        return withTenant()
                .flatMap(tenantId -> validate(resource, request, null)
                        .then(repository.codeExists(tenantId, resource, request.code().trim(), null))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.create(tenantId, resource, normalize(resource, request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "COMPLIANCE_CREATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> update(ComplianceModels.Resource resource, UUID id, ComplianceModels.MasterUpsertRequest request) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(validate(resource, request, id))
                        .then(repository.codeExists(tenantId, resource, request.code().trim(), id))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.update(tenantId, resource, id, normalize(resource, request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "COMPLIANCE_UPDATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> get(ComplianceModels.Resource resource, UUID id) {
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id).switchIfEmpty(notFound(resource)));
    }

    @Override
    public Flux<ComplianceModels.MasterViewDto> list(ComplianceModels.Resource resource, ComplianceModels.SearchQuery query) {
        ComplianceModels.SearchQuery normalized = normalizeQuery(query);
        return withTenant().flatMapMany(tenantId -> repository.list(tenantId, resource, normalized));
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> updateStatus(ComplianceModels.Resource resource, UUID id, ComplianceModels.StatusUpdateCommand command) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(repository.updateStatus(tenantId, resource, id, command.active(), ACTOR))
                        .flatMap(saved -> publishAudit(
                                        tenantId,
                                        "COMPLIANCE_STATUS_UPDATED",
                                        resource.path(),
                                        saved.id(),
                                        Map.of("active", command.active()))
                                .thenReturn(saved)));
    }

    @Override
    public Flux<ComplianceModels.OptionViewDto> options(ComplianceModels.Resource resource, String q, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return withTenant().flatMapMany(tenantId -> repository.options(tenantId, resource, q, safeLimit));
    }

    private Mono<Void> validate(ComplianceModels.Resource resource, ComplianceModels.MasterUpsertRequest request, UUID excludeId) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CODE_NAME_REQUIRED", "Code and name are required"));
        }
        if (resource == ComplianceModels.Resource.BENEFICIARY_TYPES
                && request.priorityOrder() != null
                && request.priorityOrder() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_PRIORITY_ORDER", "Priority order must be >= 0"));
        }
        if (resource == ComplianceModels.Resource.VISA_TYPES || resource == ComplianceModels.Resource.SPONSOR_TYPES) {
            if (!StringUtils.hasText(request.appliesTo())) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "APPLIES_TO_REQUIRED", "Applies-to is required"));
            }
            try {
                ComplianceModels.AppliesTo.valueOf(request.appliesTo().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_APPLIES_TO", "Invalid applies-to value"));
            }
        }
        if (resource == ComplianceModels.Resource.CIVIL_ID_TYPES) {
            if (!StringUtils.hasText(request.appliesTo())) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "APPLIES_TO_REQUIRED", "Applies-to is required"));
            }
            try {
                ComplianceModels.CivilIdAppliesTo.valueOf(request.appliesTo().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_APPLIES_TO", "Invalid applies-to value"));
            }
        }
        return Mono.empty();
    }

    private ComplianceModels.MasterUpsertRequest normalize(ComplianceModels.Resource resource, ComplianceModels.MasterUpsertRequest request) {
        String appliesTo = request.appliesTo();
        if (StringUtils.hasText(appliesTo)) {
            appliesTo = appliesTo.trim().toUpperCase();
        }
        return new ComplianceModels.MasterUpsertRequest(
                request.code().trim(),
                request.name().trim(),
                trimToNull(request.visaCategory()),
                appliesTo,
                request.renewableFlag(),
                request.expiryTrackingRequired(),
                request.omaniFlag(),
                request.countsForOmanisationFlag(),
                request.pensionEligibleFlag(),
                request.occupationalHazardEligibleFlag(),
                request.govtContributionApplicableFlag(),
                request.priorityOrder(),
                request.insuranceEligibleFlag(),
                request.familyVisaEligibleFlag(),
                trimToNull(request.description()),
                request.active());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Mono<String> withTenant() {
        return enablementGuard.requireModuleEnabled("master-data")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required"))));
    }

    private ComplianceModels.SearchQuery normalizeQuery(ComplianceModels.SearchQuery query) {
        if (query == null) {
            return new ComplianceModels.SearchQuery(null, null, 50, 0);
        }
        return new ComplianceModels.SearchQuery(
                query.q(),
                query.active(),
                Math.min(Math.max(query.limit(), 1), 500),
                Math.max(0, query.offset()));
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, tenantId, action, targetType, targetId.toString(), metadata));
    }

    private <T> Mono<T> notFound(ComplianceModels.Resource resource) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource.path() + " record not found"));
    }
}
