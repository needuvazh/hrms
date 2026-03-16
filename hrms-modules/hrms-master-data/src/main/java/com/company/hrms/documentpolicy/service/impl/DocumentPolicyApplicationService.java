package com.company.hrms.documentpolicy.service.impl;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import com.company.hrms.documentpolicy.repository.DocumentPolicyRepository;
import com.company.hrms.documentpolicy.service.DocumentPolicyModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentPolicyApplicationService implements DocumentPolicyModuleApi {

    private static final String ACTOR = "system";

    private final DocumentPolicyRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public DocumentPolicyApplicationService(
            DocumentPolicyRepository repository,
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
    public Mono<DocumentPolicyModels.MasterViewDto> create(DocumentPolicyModels.Resource resource, DocumentPolicyModels.MasterUpsertRequest request) {
        return withTenant()
                .flatMap(tenantId -> validate(tenantId, resource, request)
                        .then(repository.codeExists(tenantId, resource, request.code().trim(), null))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.create(tenantId, resource, normalize(request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "DOCUMENT_POLICY_CREATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> update(
            DocumentPolicyModels.Resource resource,
            UUID id,
            DocumentPolicyModels.MasterUpsertRequest request
    ) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(validate(tenantId, resource, request))
                        .then(repository.codeExists(tenantId, resource, request.code().trim(), id))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.update(tenantId, resource, id, normalize(request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "DOCUMENT_POLICY_UPDATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> get(DocumentPolicyModels.Resource resource, UUID id) {
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id).switchIfEmpty(notFound(resource)));
    }

    @Override
    public Flux<DocumentPolicyModels.MasterViewDto> list(DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query) {
        DocumentPolicyModels.SearchQuery normalized = normalizeQuery(query);
        return withTenant().flatMapMany(tenantId -> repository.list(tenantId, resource, normalized));
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> updateStatus(
            DocumentPolicyModels.Resource resource,
            UUID id,
            DocumentPolicyModels.StatusUpdateCommand command
    ) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(repository.updateStatus(tenantId, resource, id, command.active(), ACTOR))
                        .flatMap(saved -> publishAudit(
                                        tenantId,
                                        "DOCUMENT_POLICY_STATUS_UPDATED",
                                        resource.path(),
                                        saved.id(),
                                        Map.of("active", command.active()))
                                .thenReturn(saved)));
    }

    @Override
    public Flux<DocumentPolicyModels.OptionViewDto> options(DocumentPolicyModels.Resource resource, String q, int limit, boolean activeOnly) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return withTenant().flatMapMany(tenantId -> repository.options(tenantId, resource, q, safeLimit, activeOnly));
    }

    private Mono<Void> validate(String tenantId, DocumentPolicyModels.Resource resource, DocumentPolicyModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CODE_NAME_REQUIRED", "Code and name are required"));
        }
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_CATEGORIES
                && request.displayOrder() != null
                && request.displayOrder() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DISPLAY_ORDER", "Display order must be >= 0"));
        }
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_TYPES) {
            return validateDocumentType(tenantId, request);
        }
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_APPLICABILITY_RULES) {
            return validateApplicabilityRule(tenantId, request);
        }
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_EXPIRY_RULES) {
            return validateExpiryRule(tenantId, request);
        }
        if (resource == DocumentPolicyModels.Resource.ATTACHMENT_CATEGORIES) {
            return validateAttachmentCategory(request);
        }
        return Mono.empty();
    }

    private Mono<Void> validateDocumentType(String tenantId, DocumentPolicyModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.documentFor())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_FOR_REQUIRED", "Document-for is required"));
        }
        try {
            DocumentPolicyModels.DocumentFor.valueOf(request.documentFor().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DOCUMENT_FOR", "Invalid document-for value"));
        }
        if (request.documentCategoryId() == null) {
            return Mono.empty();
        }
        return requireExists(tenantId, "master_data.document_categories", request.documentCategoryId(), "DOCUMENT_CATEGORY_NOT_FOUND");
    }

    private Mono<Void> validateApplicabilityRule(String tenantId, DocumentPolicyModels.MasterUpsertRequest request) {
        if (request.documentTypeId() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_TYPE_REQUIRED", "Document type is required"));
        }
        return Mono.when(
                requireExists(tenantId, "master_data.document_types", request.documentTypeId(), "DOCUMENT_TYPE_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.worker_types", request.workerTypeId(), "WORKER_TYPE_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.employee_categories", request.employeeCategoryId(), "EMPLOYEE_CATEGORY_NOT_FOUND"),
                optionalExists(tenantId, "master_data.nationalisation_categories", request.nationalisationCategoryId(), "NATIONALISATION_CATEGORY_NOT_FOUND"),
                optionalExists(tenantId, "organization.legal_entities", request.legalEntityId(), "LEGAL_ENTITY_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.job_families", request.jobFamilyId(), "JOB_FAMILY_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.designations", request.designationId(), "DESIGNATION_NOT_FOUND"),
                optionalExists(tenantId, "master_data.dependent_types", request.dependentTypeId(), "DEPENDENT_TYPE_NOT_FOUND"));
    }

    private Mono<Void> validateExpiryRule(String tenantId, DocumentPolicyModels.MasterUpsertRequest request) {
        if (request.documentTypeId() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_TYPE_REQUIRED", "Document type is required"));
        }
        if (request.gracePeriodDays() != null && request.gracePeriodDays() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_GRACE_PERIOD", "Grace period must be >= 0"));
        }
        List<Integer> alerts = request.alertDaysBefore() == null ? List.of() : request.alertDaysBefore();
        if (alerts.stream().anyMatch(v -> v == null || v < 0)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_ALERT_DAYS", "Alert days must be non-negative integers"));
        }
        if (new HashSet<>(alerts).size() != alerts.size()) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DUPLICATE_ALERT_DAYS", "Alert days cannot contain duplicates"));
        }
        for (int i = 1; i < alerts.size(); i++) {
            if (alerts.get(i) > alerts.get(i - 1)) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_ALERT_ORDER", "Alert days must be in descending order"));
            }
        }
        if (Boolean.FALSE.equals(request.expiryTrackingRequired())) {
            if (Boolean.TRUE.equals(request.renewalRequired()) || !alerts.isEmpty()) {
                return Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "INCONSISTENT_EXPIRY_RULE",
                        "Renewal and alerts are not allowed when expiry tracking is disabled"));
            }
        }
        return requireExists(tenantId, "master_data.document_types", request.documentTypeId(), "DOCUMENT_TYPE_NOT_FOUND");
    }

    private Mono<Void> validateAttachmentCategory(DocumentPolicyModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.mimeGroup())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "MIME_GROUP_REQUIRED", "Mime group is required"));
        }
        try {
            DocumentPolicyModels.MimeGroup.valueOf(request.mimeGroup().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_MIME_GROUP", "Invalid mime group"));
        }
        if (request.maxFileSizeMb() != null && request.maxFileSizeMb() <= 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_MAX_FILE_SIZE", "Max file size must be > 0"));
        }
        return Mono.empty();
    }

    private Mono<Void> requireExists(String tenantId, String table, UUID id, String errorCode) {
        return repository.existsById(tenantId, table, id)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, errorCode, errorCode.replace('_', ' ').toLowerCase())));
    }

    private Mono<Void> optionalExists(String tenantId, String table, UUID id, String errorCode) {
        if (id == null) {
            return Mono.empty();
        }
        return requireExists(tenantId, table, id, errorCode);
    }

    private DocumentPolicyModels.MasterUpsertRequest normalize(DocumentPolicyModels.MasterUpsertRequest request) {
        return new DocumentPolicyModels.MasterUpsertRequest(
                request.code().trim(),
                request.name().trim(),
                trimToNull(request.shortDescription()),
                uppercaseOrNull(request.documentFor()),
                request.documentCategoryId(),
                request.attachmentRequired(),
                request.issueDateRequired(),
                request.expiryDateRequired(),
                request.referenceNoRequired(),
                request.multipleAllowed(),
                request.displayOrder(),
                request.documentTypeId(),
                request.workerTypeId(),
                request.employeeCategoryId(),
                request.nationalisationCategoryId(),
                request.legalEntityId(),
                request.jobFamilyId(),
                request.designationId(),
                request.dependentTypeId(),
                request.mandatoryFlag(),
                request.onboardingRequiredFlag(),
                request.expiryTrackingRequired(),
                request.renewalRequired(),
                request.alertDaysBefore(),
                request.gracePeriodDays(),
                request.blockTransactionOnExpiryFlag(),
                request.versionRequiredFlag(),
                request.eSignatureRequiredFlag(),
                request.reackOnVersionChangeFlag(),
                request.annualReackFlag(),
                uppercaseOrNull(request.mimeGroup()),
                request.maxFileSizeMb(),
                trimToNull(request.description()),
                request.active());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String uppercaseOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private Mono<String> withTenant() {
        return enablementGuard.requireModuleEnabled("master-data")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required"))));
    }

    private DocumentPolicyModels.SearchQuery normalizeQuery(DocumentPolicyModels.SearchQuery query) {
        if (query == null) {
            return new DocumentPolicyModels.SearchQuery(
                    null,
                    null,
                    50,
                    0,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }
        return new DocumentPolicyModels.SearchQuery(
                query.q(),
                query.active(),
                Math.min(Math.max(query.limit(), 1), 500),
                Math.max(0, query.offset()),
                query.sort(),
                query.documentCategoryId(),
                query.documentFor(),
                query.documentTypeId(),
                query.workerTypeId(),
                query.employeeCategoryId(),
                query.nationalisationCategoryId(),
                query.legalEntityId(),
                query.jobFamilyId(),
                query.designationId(),
                query.dependentTypeId(),
                query.mandatoryFlag(),
                query.onboardingRequiredFlag(),
                query.expiryTrackingRequired(),
                query.renewalRequired(),
                query.blockTransactionOnExpiryFlag(),
                query.mimeGroup());
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, tenantId, action, targetType, targetId.toString(), metadata));
    }

    private <T> Mono<T> notFound(DocumentPolicyModels.Resource resource) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource.path() + " record not found"));
    }
}
