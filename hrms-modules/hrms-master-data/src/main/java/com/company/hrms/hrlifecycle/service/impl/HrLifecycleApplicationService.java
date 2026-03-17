package com.company.hrms.hrlifecycle.service.impl;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import com.company.hrms.hrlifecycle.repository.HrLifecycleRepository;
import com.company.hrms.hrlifecycle.service.HrLifecycleModuleApi;
import com.company.hrms.masterdata.reference.api.PagedResult;
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
public class HrLifecycleApplicationService implements HrLifecycleModuleApi {

    private static final String ACTOR = "system";

    private final HrLifecycleRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public HrLifecycleApplicationService(
            HrLifecycleRepository repository,
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
    public Mono<HrLifecycleModels.MasterViewDto> create(HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request) {
        return withTenant().flatMap(tenantId -> validate(tenantId, resource, request)
                .then(requireCodeAndName(request))
                .then(repository.codeExists(tenantId, resource, request.code().trim(), null))
                .flatMap(exists -> exists
                        ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                        : repository.create(tenantId, resource, normalize(request), ACTOR))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                .flatMap(saved -> publishAudit(tenantId, "HR_LIFECYCLE_CREATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                        .thenReturn(saved)));
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> update(
            HrLifecycleModels.Resource resource,
            UUID id,
            HrLifecycleModels.MasterUpsertRequest request
    ) {
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id)
                .switchIfEmpty(notFound(resource))
                .then(validate(tenantId, resource, request))
                .then(requireCodeAndName(request))
                .then(repository.codeExists(tenantId, resource, request.code().trim(), id))
                .flatMap(exists -> exists
                        ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                        : repository.update(tenantId, resource, id, normalize(request), ACTOR))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                .flatMap(saved -> publishAudit(tenantId, "HR_LIFECYCLE_UPDATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                        .thenReturn(saved)));
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> get(HrLifecycleModels.Resource resource, UUID id) {
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id).switchIfEmpty(notFound(resource)));
    }

    @Override
    public Mono<PagedResult<HrLifecycleModels.MasterViewDto>> list(HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query) {
        HrLifecycleModels.SearchQuery normalized = normalizeQuery(query);
        return withTenant().flatMap(tenantId -> repository.list(tenantId, resource, normalized)
                .collectList()
                .zipWith(repository.count(tenantId, resource, normalized))
                .map(tuple -> {
                    int page = normalized.limit() == 0 ? 0 : normalized.offset() / normalized.limit();
                    int totalPages = tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / normalized.limit());
                    return new PagedResult<>(tuple.getT1(), page, normalized.limit(), tuple.getT2(), totalPages);
                }));
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> updateStatus(
            HrLifecycleModels.Resource resource,
            UUID id,
            HrLifecycleModels.StatusUpdateCommand command
    ) {
        // Extension point for Step 8 follow-up hard rules:
        // - block deactivation when linked active transactions exist
        // - enforce replacement strategy for employee status / lifecycle stage changes
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id)
                .switchIfEmpty(notFound(resource))
                .then(repository.updateStatus(tenantId, resource, id, command.active(), ACTOR))
                .flatMap(saved -> publishAudit(
                                tenantId,
                                "HR_LIFECYCLE_STATUS_UPDATED",
                                resource.path(),
                                saved.id(),
                                Map.of("active", command.active()))
                        .thenReturn(saved)));
    }

    @Override
    public Flux<HrLifecycleModels.OptionViewDto> options(HrLifecycleModels.Resource resource, String q, int limit, boolean activeOnly) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return withTenant().flatMapMany(tenantId -> repository.options(tenantId, resource, q, safeLimit, activeOnly));
    }

    private Mono<Void> validate(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request) {
        return switch (resource) {
            case HOLIDAY_CALENDARS -> validateHolidayCalendar(tenantId, request);
            case LEAVE_TYPES -> validateLeaveType(tenantId, request);
            case SHIFTS -> validateShift(request);
            case ATTENDANCE_SOURCES -> validateAttendanceSource(request);
            case ONBOARDING_TASK_TYPES, OFFBOARDING_TASK_TYPES -> validateTaskType(request);
            case EVENT_TYPES -> validateEventType(request);
            case EMPLOYEE_STATUSES -> Mono.empty();
            case EMPLOYMENT_LIFECYCLE_STAGES -> validateLifecycleStage(request);
        };
    }

    private Mono<Void> validateHolidayCalendar(String tenantId, HrLifecycleModels.MasterUpsertRequest request) {
        if (request.calendarYear() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CALENDAR_YEAR_REQUIRED", "calendarYear is required"));
        }
        if (request.calendarYear() < 1900 || request.calendarYear() > 3000) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_CALENDAR_YEAR", "calendarYear must be between 1900 and 3000"));
        }
        if (!StringUtils.hasText(request.calendarType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CALENDAR_TYPE_REQUIRED", "calendarType is required"));
        }
        Mono<Void> validateType = validateEnum(request.calendarType(), HrLifecycleModels.CalendarType.class, "INVALID_CALENDAR_TYPE");
        if (!StringUtils.hasText(request.countryCode())) {
            return validateType;
        }
        return validateType.then(repository.existsReferenceCode(tenantId, "master_data.countries", "country_code", request.countryCode().trim(), false)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_COUNTRY_CODE", "countryCode is invalid"))));
    }

    private Mono<Void> validateLeaveType(String tenantId, HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.leaveCategory())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_CATEGORY_REQUIRED", "leaveCategory is required"));
        }
        return validateEnum(request.leaveCategory(), HrLifecycleModels.LeaveCategory.class, "INVALID_LEAVE_CATEGORY")
                .then(validateReferenceCode(tenantId, request.genderApplicability(), "master_data.genders", "gender_code", false, "INVALID_GENDER_APPLICABILITY"))
                .then(validateReferenceCode(tenantId, request.religionApplicability(), "master_data.religions", "religion_code", false, "INVALID_RELIGION_APPLICABILITY"))
                .then(validateReferenceCode(
                        tenantId,
                        request.nationalisationApplicability(),
                        "master_data.nationalisation_categories",
                        "nationalisation_category_code",
                        true,
                        "INVALID_NATIONALISATION_APPLICABILITY"));
    }

    private Mono<Void> validateShift(HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.shiftType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_TYPE_REQUIRED", "shiftType is required"));
        }
        if (request.breakDurationMinutes() != null && request.breakDurationMinutes() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_BREAK_DURATION", "breakDurationMinutes must be >= 0"));
        }
        if (request.graceInMinutes() != null && request.graceInMinutes() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_GRACE_IN", "graceInMinutes must be >= 0"));
        }
        if (request.graceOutMinutes() != null && request.graceOutMinutes() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_GRACE_OUT", "graceOutMinutes must be >= 0"));
        }
        return validateEnum(request.shiftType(), HrLifecycleModels.ShiftType.class, "INVALID_SHIFT_TYPE")
                .then(Mono.defer(() -> {
                    String shiftType = request.shiftType().trim().toUpperCase();
                    // Non-flexible templates require explicit boundaries.
                    // FLEXIBLE templates may defer timing policy to later modules.
                    if (!"FLEXIBLE".equals(shiftType) && (request.startTime() == null || request.endTime() == null)) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_TIME_REQUIRED", "startTime and endTime are required"));
                    }
                    // Keep overnight=false permissive for master setup; runtime attendance can enforce stricter rules.
                    if (request.startTime() != null && request.endTime() != null && request.startTime().equals(request.endTime())) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SHIFT_TIME", "startTime and endTime cannot be same"));
                    }
                    // SPLIT/ROTATING remain header-level in this step.
                    // Detailed sessions and rotation plans belong to roster/assignment modules.
                    return Mono.empty();
                }));
    }

    private Mono<Void> validateAttendanceSource(HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.sourceType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SOURCE_TYPE_REQUIRED", "sourceType is required"));
        }
        return validateEnum(request.sourceType(), HrLifecycleModels.AttendanceSourceType.class, "INVALID_SOURCE_TYPE");
    }

    private Mono<Void> validateTaskType(HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.assigneeType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ASSIGNEE_TYPE_REQUIRED", "assigneeType is required"));
        }
        return validateEnum(request.assigneeType(), HrLifecycleModels.AssigneeType.class, "INVALID_ASSIGNEE_TYPE");
    }

    private Mono<Void> validateEventType(HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.eventGroup())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "EVENT_GROUP_REQUIRED", "eventGroup is required"));
        }
        return validateEnum(request.eventGroup(), HrLifecycleModels.EventGroup.class, "INVALID_EVENT_GROUP");
    }

    private Mono<Void> validateLifecycleStage(HrLifecycleModels.MasterUpsertRequest request) {
        if (request.stageOrder() != null && request.stageOrder() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_STAGE_ORDER", "stageOrder must be >= 0"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateReferenceCode(
            String tenantId,
            String code,
            String table,
            String codeColumn,
            boolean tenantOwned,
            String errorCode
    ) {
        if (!StringUtils.hasText(code)) {
            return Mono.empty();
        }
        return repository.existsReferenceCode(tenantId, table, codeColumn, code.trim(), tenantOwned)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, errorCode, "Invalid reference code: " + code)));
    }

    private Mono<Void> requireCodeAndName(HrLifecycleModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CODE_NAME_REQUIRED", "code and name are required"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateEnum(String value, Class<? extends Enum<?>> enumType, String errorCode) {
        try {
            Enum.valueOf((Class) enumType, value.trim().toUpperCase());
            return Mono.empty();
        } catch (IllegalArgumentException ex) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, errorCode, "Invalid value: " + value));
        }
    }

    private HrLifecycleModels.MasterUpsertRequest normalize(HrLifecycleModels.MasterUpsertRequest request) {
        return new HrLifecycleModels.MasterUpsertRequest(
                trimToNull(request.code()),
                trimToNull(request.name()),
                trimToNull(request.countryCode()),
                request.calendarYear(),
                uppercaseOrNull(request.calendarType()),
                request.hijriEnabledFlag(),
                request.weekendAdjustmentFlag(),
                uppercaseOrNull(request.leaveCategory()),
                request.paidFlag(),
                request.supportingDocumentRequiredFlag(),
                trimToNull(request.genderApplicability()),
                trimToNull(request.religionApplicability()),
                trimToNull(request.nationalisationApplicability()),
                uppercaseOrNull(request.shiftType()),
                request.startTime(),
                request.endTime(),
                request.breakDurationMinutes(),
                request.overnightFlag(),
                request.graceInMinutes(),
                request.graceOutMinutes(),
                uppercaseOrNull(request.sourceType()),
                request.trustedSourceFlag(),
                request.manualOverrideFlag(),
                trimToNull(request.taskCategory()),
                request.mandatoryFlag(),
                uppercaseOrNull(request.assigneeType()),
                uppercaseOrNull(request.eventGroup()),
                request.employmentActiveFlag(),
                request.selfServiceAccessFlag(),
                request.stageOrder(),
                request.entryStageFlag(),
                request.exitStageFlag(),
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

    private HrLifecycleModels.SearchQuery normalizeQuery(HrLifecycleModels.SearchQuery query) {
        if (query == null) {
            return new HrLifecycleModels.SearchQuery(
                    null, null, 50, 0, null,
                    null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null,
                    null, null, null,
                    null, null);
        }
        return new HrLifecycleModels.SearchQuery(
                query.q(),
                query.active(),
                Math.min(Math.max(query.limit(), 1), 500),
                Math.max(0, query.offset()),
                query.sort(),
                query.countryCode(),
                query.calendarYear(),
                query.calendarType(),
                query.hijriEnabledFlag(),
                query.weekendAdjustmentFlag(),
                query.leaveCategory(),
                query.paidFlag(),
                query.supportingDocumentRequiredFlag(),
                query.genderApplicability(),
                query.religionApplicability(),
                query.nationalisationApplicability(),
                query.shiftType(),
                query.overnightFlag(),
                query.sourceType(),
                query.trustedSourceFlag(),
                query.manualOverrideFlag(),
                query.assigneeType(),
                query.mandatoryFlag(),
                query.taskCategory(),
                query.eventGroup(),
                query.employmentActiveFlag(),
                query.selfServiceAccessFlag(),
                query.entryStageFlag(),
                query.exitStageFlag());
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, tenantId, action, targetType, targetId.toString(), metadata));
    }

    private <T> Mono<T> notFound(HrLifecycleModels.Resource resource) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource.path() + " record not found"));
    }
}
