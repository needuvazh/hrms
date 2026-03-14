package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import com.company.hrms.masterdata.reference.infrastructure.ReferenceMasterRepository;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReferenceMasterApplicationService implements ReferenceMasterService {

    private static final String ACTOR = "system";
    private static final String TENANT = "platform";

    private final ReferenceMasterRepository repository;
    private final ReferenceMasterMapper mapper;
    private final AuditEventPublisher auditEventPublisher;

    public ReferenceMasterApplicationService(
            ReferenceMasterRepository repository,
            ReferenceMasterMapper mapper,
            AuditEventPublisher auditEventPublisher
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<ReferenceMasterViewDto> create(ReferenceResource resource, ReferenceMasterUpsertRequest request) {
        return validate(resource, request, null)
                .then(repository.create(resource, normalize(resource, request), ACTOR))
                .flatMap(row -> publishAudit("REFERENCE_CREATED", resource, row.id(), Map.of("code", row.code(), "name", row.name()))
                        .thenReturn(mapper.toView(row)));
    }

    @Override
    public Mono<ReferenceMasterViewDto> update(ReferenceResource resource, UUID id, ReferenceMasterUpsertRequest request) {
        return repository.findById(resource, id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found")))
                .flatMap(current -> validate(resource, request, id)
                        .then(repository.update(resource, id, normalize(resource, request), ACTOR))
                        .flatMap(row -> publishAudit("REFERENCE_UPDATED", resource, id, Map.of("code", row.code(), "name", row.name()))
                                .thenReturn(mapper.toView(row))));
    }

    @Override
    public Mono<ReferenceMasterViewDto> getById(ReferenceResource resource, UUID id) {
        return repository.findById(resource, id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found")))
                .map(mapper::toView);
    }

    @Override
    public Mono<PagedResult<ReferenceMasterViewDto>> list(ReferenceResource resource, ReferenceSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 200);
        ReferenceSearchQuery normalized = new ReferenceSearchQuery(query.q(), query.active(), safePage, safeSize, query.sort(), query.skillCategoryId());
        return repository.list(resource, normalized).map(mapper::toView).collectList()
                .zipWith(repository.count(resource, normalized))
                .map(tuple -> new PagedResult<>(
                        tuple.getT1(),
                        safePage,
                        safeSize,
                        tuple.getT2(),
                        tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / safeSize)));
    }

    @Override
    public Mono<Void> updateStatus(ReferenceResource resource, UUID id, boolean active) {
        return repository.existsById(resource, id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found"));
                    }
                    return repository.updateStatus(resource, id, active, ACTOR)
                            .then(publishAudit("REFERENCE_STATUS_CHANGED", resource, id, Map.of("active", active)));
                });
    }

    @Override
    public Flux<ReferenceOptionViewDto> options(ReferenceResource resource, boolean activeOnly) {
        return repository.options(resource, activeOnly);
    }

    private Mono<Void> validate(ReferenceResource resource, ReferenceMasterUpsertRequest request, UUID excludeId) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "REFERENCE_CODE_NAME_REQUIRED", "Code and name are required"));
        }

        Mono<Boolean> codeExists = repository.existsCode(resource, request.code().trim(), excludeId);
        Mono<Boolean> nameExists = repository.existsName(resource, request.name().trim(), excludeId);

        return Mono.zip(codeExists, nameExists)
                .flatMap(tuple -> {
                    if (tuple.getT1()) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "REFERENCE_CODE_EXISTS", "Code already exists"));
                    }
                    if (requiresUniqueName(resource) && tuple.getT2()) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "REFERENCE_NAME_EXISTS", "Name already exists"));
                    }
                    return validateCrossReferences(resource, request);
                });
    }

    private Mono<Void> validateCrossReferences(ReferenceResource resource, ReferenceMasterUpsertRequest request) {
        if (resource == ReferenceResource.COUNTRIES
                && (!StringUtils.hasText(request.iso2Code()) || !StringUtils.hasText(request.iso3Code()))) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_ISO_REQUIRED", "ISO2 and ISO3 codes are required"));
        }
        if (resource == ReferenceResource.COUNTRIES && StringUtils.hasText(request.defaultCurrencyCode())) {
            return repository.existsCurrencyCode(request.defaultCurrencyCode().trim())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DEFAULT_CURRENCY", "Default currency code is invalid")));
        }
        if (resource == ReferenceResource.NATIONALITIES && StringUtils.hasText(request.countryCode())) {
            return repository.existsCountryCode(request.countryCode().trim())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_COUNTRY_CODE", "Country code is invalid")));
        }
        if (resource == ReferenceResource.SKILLS) {
            if (request.skillCategoryId() == null) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SKILL_CATEGORY_REQUIRED", "Skill category is required"));
            }
            return repository.existsById(ReferenceResource.SKILL_CATEGORIES, request.skillCategoryId())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SKILL_CATEGORY", "Skill category does not exist")));
        }
        if (resource == ReferenceResource.DOCUMENT_TYPES) {
            if (request.documentFor() == null) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_FOR_REQUIRED", "Document for is required"));
            }
            if (request.alertDaysBefore() != null && request.alertDaysBefore() < 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_ALERT_DAYS", "Alert days before cannot be negative"));
            }
            if (Boolean.FALSE.equals(request.alertRequired()) && request.alertDaysBefore() != null && request.alertDaysBefore() > 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_ALERT_CONFIGURATION", "Alert days cannot be positive when alert is disabled"));
            }
        }
        return Mono.empty();
    }

    private boolean requiresUniqueName(ReferenceResource resource) {
        return List.of(
                ReferenceResource.RELIGIONS,
                ReferenceResource.MARITAL_STATUSES,
                ReferenceResource.NATIONALITIES,
                ReferenceResource.CERTIFICATION_TYPES,
                ReferenceResource.SKILL_CATEGORIES).contains(resource);
    }

    private ReferenceMasterUpsertRequest normalize(ReferenceResource resource, ReferenceMasterUpsertRequest request) {
        if (resource == ReferenceResource.DOCUMENT_TYPES && Boolean.FALSE.equals(request.alertRequired())) {
            return new ReferenceMasterUpsertRequest(
                    request.code(),
                    request.name(),
                    request.shortName(),
                    request.iso2Code(),
                    request.iso3Code(),
                    request.phoneCode(),
                    request.nationalityName(),
                    request.defaultCurrencyCode(),
                    request.defaultTimezone(),
                    request.gccFlag(),
                    request.decimalPlaces(),
                    request.nativeName(),
                    request.rtlEnabled(),
                    request.countryCode(),
                    request.gccNationalFlag(),
                    request.omaniFlag(),
                    request.displayOrder(),
                    request.dependentAllowed(),
                    request.emergencyContactAllowed(),
                    request.beneficiaryAllowed(),
                    request.shortDescription(),
                    request.documentFor(),
                    request.issueDateRequired(),
                    request.expiryDateRequired(),
                    request.alertRequired(),
                    0,
                    request.rankingOrder(),
                    request.expiryTrackingRequired(),
                    request.issuingBodyRequired(),
                    request.description(),
                    request.skillCategoryId(),
                    request.active());
        }
        return request;
    }

    private Mono<Void> publishAudit(String action, ReferenceResource resource, UUID id, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(
                ACTOR,
                TENANT,
                action,
                resource.path(),
                id.toString(),
                metadata));
    }
}
