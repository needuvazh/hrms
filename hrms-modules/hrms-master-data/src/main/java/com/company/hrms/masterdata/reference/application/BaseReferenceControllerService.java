package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import com.company.hrms.masterdata.reference.infrastructure.ReferenceResourceDao;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

abstract class BaseReferenceControllerService implements ReferenceControllerService {
    private static final String ACTOR = "system";
    private static final String TENANT = "platform";

    private final ReferenceMasterMapper mapper;
    private final AuditEventPublisher auditEventPublisher;
    private final ReferenceResourceDao dao;
    private final ReferenceResource resource;

    protected BaseReferenceControllerService(
            ReferenceMasterMapper mapper,
            AuditEventPublisher auditEventPublisher,
            ReferenceResourceDao dao,
            ReferenceResource resource
    ) {
        this.mapper = mapper;
        this.auditEventPublisher = auditEventPublisher;
        this.dao = dao;
        this.resource = resource;
    }

    @Override
    public Mono<ReferenceMasterViewDto> create(ReferenceMasterUpsertRequest request) {
        return normalize(request)
                .flatMap(normalized -> validate(normalized, null).then(dao.create(normalized, ACTOR)))
                .flatMap(row -> publishAudit("REFERENCE_CREATED", row.id(), Map.of("code", row.code(), "name", row.name()))
                        .thenReturn(mapper.toView(row)));
    }

    @Override
    public Mono<ReferenceMasterViewDto> update(UUID id, ReferenceMasterUpsertRequest request) {
        return dao.findById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found")))
                .flatMap(current -> normalize(request)
                        .flatMap(normalized -> validate(normalized, id).then(dao.update(id, normalized, ACTOR)))
                        .flatMap(row -> publishAudit("REFERENCE_UPDATED", id, Map.of("code", row.code(), "name", row.name()))
                                .thenReturn(mapper.toView(row))));
    }

    @Override
    public Mono<ReferenceMasterViewDto> getById(UUID id) {
        return dao.findById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found")))
                .map(mapper::toView);
    }

    @Override
    public Mono<PagedResult<ReferenceMasterViewDto>> list(ReferenceSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 200);
        ReferenceSearchQuery normalizedQuery = new ReferenceSearchQuery(
                query.q(), query.active(), safePage, safeSize, query.sort(), query.skillCategoryId(), query.all());
        return dao.list(normalizedQuery).map(mapper::toView).collectList()
                .zipWith(dao.count(normalizedQuery))
                .map(tuple -> {
                    int responsePage = normalizedQuery.all() ? 0 : safePage;
                    int responseSize = normalizedQuery.all() ? tuple.getT1().size() : safeSize;
                    int totalPages = normalizedQuery.all()
                            ? (tuple.getT2() == 0 ? 0 : 1)
                            : (tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / safeSize));
                    return new PagedResult<>(tuple.getT1(), responsePage, responseSize, tuple.getT2(), totalPages);
                });
    }

    @Override
    public Mono<Void> updateStatus(UUID id, boolean active) {
        return dao.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "REFERENCE_NOT_FOUND", "Reference record not found"));
                    }
                    return dao.updateStatus(id, active, ACTOR)
                            .then(publishAudit("REFERENCE_STATUS_CHANGED", id, Map.of("active", active)));
                });
    }

    @Override
    public Flux<ReferenceOptionViewDto> options(boolean activeOnly) {
        return dao.options(activeOnly);
    }

    private Mono<Void> validate(ReferenceMasterUpsertRequest request, UUID excludeId) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "REFERENCE_CODE_NAME_REQUIRED", "Code and name are required"));
        }

        Mono<Boolean> codeExists = dao.existsCode(request.code().trim(), excludeId);
        Mono<Boolean> nameExists = dao.existsName(request.name().trim(), excludeId);

        return Mono.zip(codeExists, nameExists)
                .flatMap(tuple -> {
                    if (tuple.getT1()) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "REFERENCE_CODE_EXISTS", "Code already exists"));
                    }
                    if (requiresUniqueName() && tuple.getT2()) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "REFERENCE_NAME_EXISTS", "Name already exists"));
                    }
                    return validateCrossReferences(request);
                });
    }

    private Mono<Void> validateCrossReferences(ReferenceMasterUpsertRequest request) {
        if (resource == ReferenceResource.COUNTRIES
                && (!StringUtils.hasText(request.iso2Code()) || !StringUtils.hasText(request.iso3Code()))) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_ISO_REQUIRED", "ISO2 and ISO3 codes are required"));
        }
        if (resource == ReferenceResource.COUNTRIES && StringUtils.hasText(request.defaultCurrencyCode())) {
            return dao.existsCurrencyCode(request.defaultCurrencyCode().trim())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DEFAULT_CURRENCY", "Default currency code is invalid")));
        }
        if (resource == ReferenceResource.NATIONALITIES && StringUtils.hasText(request.countryCode())) {
            return dao.existsCountryCode(request.countryCode().trim())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_COUNTRY_CODE", "Country code is invalid")));
        }
        if (resource == ReferenceResource.SKILLS) {
            if (request.skillCategoryId() == null) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SKILL_CATEGORY_REQUIRED", "Skill category is required"));
            }
            return dao.existsSkillCategoryById(request.skillCategoryId())
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

    private boolean requiresUniqueName() {
        return List.of(
                ReferenceResource.RELIGIONS,
                ReferenceResource.MARITAL_STATUSES,
                ReferenceResource.NATIONALITIES,
                ReferenceResource.CERTIFICATION_TYPES,
                ReferenceResource.SKILL_CATEGORIES).contains(resource);
    }

    private Mono<ReferenceMasterUpsertRequest> normalize(ReferenceMasterUpsertRequest request) {
        ReferenceMasterUpsertRequest normalized = request;

        if (resource == ReferenceResource.COUNTRIES) {
            String effectiveCode = StringUtils.hasText(request.countryCode())
                    ? request.countryCode().trim()
                    : request.code().trim();
            normalized = copy(normalized, effectiveCode, effectiveCode, normalized.defaultCurrencyCode(), normalized.alertDaysBefore());
        }

        if (resource == ReferenceResource.DOCUMENT_TYPES && Boolean.FALSE.equals(normalized.alertRequired())) {
            normalized = copy(normalized, normalized.code(), normalized.countryCode(), normalized.defaultCurrencyCode(), 0);
        }

        if (resource == ReferenceResource.COUNTRIES && StringUtils.hasText(normalized.defaultCurrencyCode())) {
            String token = normalized.defaultCurrencyCode().trim();
            ReferenceMasterUpsertRequest requestSnapshot = normalized;
            return dao.resolveCurrencyCode(token)
                    .defaultIfEmpty(token)
                    .map(resolved -> copy(requestSnapshot, requestSnapshot.code(), requestSnapshot.countryCode(), resolved, requestSnapshot.alertDaysBefore()));
        }

        return Mono.just(normalized);
    }

    private ReferenceMasterUpsertRequest copy(
            ReferenceMasterUpsertRequest request,
            String code,
            String countryCode,
            String defaultCurrencyCode,
            Integer alertDaysBefore
    ) {
        return new ReferenceMasterUpsertRequest(
                code,
                request.name(),
                request.shortName(),
                request.iso2Code(),
                request.iso3Code(),
                request.phoneCode(),
                request.nationalityName(),
                defaultCurrencyCode,
                request.defaultTimezone(),
                request.gccFlag(),
                request.decimalPlaces(),
                request.nativeName(),
                request.rtlEnabled(),
                countryCode,
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
                alertDaysBefore,
                request.rankingOrder(),
                request.expiryTrackingRequired(),
                request.issuingBodyRequired(),
                request.description(),
                request.skillCategoryId(),
                request.active());
    }

    private Mono<Void> publishAudit(String action, UUID id, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, TENANT, action, resource.path(), id.toString(), metadata));
    }
}
