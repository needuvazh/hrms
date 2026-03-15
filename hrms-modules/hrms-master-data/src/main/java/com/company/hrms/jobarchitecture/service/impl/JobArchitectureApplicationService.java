package com.company.hrms.jobarchitecture.service.impl;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import com.company.hrms.jobarchitecture.repository.JobArchitectureRepository;
import com.company.hrms.jobarchitecture.service.JobArchitectureModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class JobArchitectureApplicationService implements JobArchitectureModuleApi {

    private static final String ACTOR = "system";

    private final JobArchitectureRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public JobArchitectureApplicationService(
            JobArchitectureRepository repository,
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
    public Mono<JobArchitectureModels.MasterViewDto> create(JobArchitectureModels.Resource resource, JobArchitectureModels.MasterUpsertRequest request) {
        return withTenant()
                .flatMap(tenantId -> validateForResource(tenantId, resource, request, null)
                        .then(repository.codeExists(tenantId, resource, request.code(), null))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.create(tenantId, resource, request, ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "JOB_ARCHITECTURE_CREATED", resource.name(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> update(JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.MasterUpsertRequest request) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(validateForResource(tenantId, resource, request, id))
                        .then(repository.codeExists(tenantId, resource, request.code(), id))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.update(tenantId, resource, id, request, ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "JOB_ARCHITECTURE_UPDATED", resource.name(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> get(JobArchitectureModels.Resource resource, UUID id) {
        return withTenant().flatMap(tenantId -> repository.get(tenantId, resource, id).switchIfEmpty(notFound(resource)));
    }

    @Override
    public Flux<JobArchitectureModels.MasterViewDto> list(JobArchitectureModels.Resource resource, JobArchitectureModels.SearchQuery query) {
        JobArchitectureModels.SearchQuery normalized = normalizeQuery(query);
        return withTenant().flatMapMany(tenantId -> repository.list(tenantId, resource, normalized));
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> updateStatus(JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.StatusUpdateCommand command) {
        return withTenant()
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .flatMap(existing -> validateStatusRules(tenantId, resource, existing, command.active())
                                .then(repository.updateStatus(tenantId, resource, id, command.active(), ACTOR))
                                .flatMap(saved -> publishAudit(tenantId, "JOB_ARCHITECTURE_STATUS_UPDATED", resource.name(), saved.id(), Map.of("active", command.active()))
                                        .thenReturn(saved))));
    }

    @Override
    public Flux<JobArchitectureModels.OptionViewDto> options(JobArchitectureModels.Resource resource, String q, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return withTenant().flatMapMany(tenantId -> repository.options(tenantId, resource, q, safeLimit));
    }

    private Mono<Void> validateForResource(String tenantId, JobArchitectureModels.Resource resource, JobArchitectureModels.MasterUpsertRequest request, UUID currentId) {
        if (!StringUtils.hasText(request.code()) || !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CODE_NAME_REQUIRED", "Code and name are required"));
        }

        Mono<Void> common = Mono.empty();
        if (resource == JobArchitectureModels.Resource.DESIGNATIONS) {
            common = Mono.when(
                    optionalExists(tenantId, "job_architecture.job_families", request.jobFamilyId(), "JOB_FAMILY_NOT_FOUND"),
                    optionalExists(tenantId, "job_architecture.job_functions", request.jobFunctionId(), "JOB_FUNCTION_NOT_FOUND"));
        } else if (resource == JobArchitectureModels.Resource.JOB_FUNCTIONS) {
            common = optionalExists(tenantId, "job_architecture.job_families", request.jobFamilyId(), "JOB_FAMILY_NOT_FOUND");
        } else if (resource == JobArchitectureModels.Resource.GRADES) {
            common = optionalExists(tenantId, "job_architecture.grade_bands", request.gradeBandId(), "GRADE_BAND_NOT_FOUND")
                    .then(validateSalaryRange(request));
        } else if (resource == JobArchitectureModels.Resource.POSITIONS) {
            common = validatePosition(tenantId, request, currentId);
        } else if (resource == JobArchitectureModels.Resource.EMPLOYEE_SUBCATEGORIES) {
            if (request.employeeCategoryId() == null) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_CATEGORY_REQUIRED", "Employee category is required"));
            }
            common = requireExists(tenantId, "job_architecture.employee_categories", request.employeeCategoryId(), "EMPLOYEE_CATEGORY_NOT_FOUND");
        } else if (resource == JobArchitectureModels.Resource.CONTRACT_TYPES) {
            if (request.defaultDurationDays() != null && request.defaultDurationDays() <= 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DURATION", "Default duration days must be positive"));
            }
        } else if (resource == JobArchitectureModels.Resource.PROBATION_POLICIES) {
            if (request.durationDays() == null || request.durationDays() <= 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DURATION", "Duration days must be greater than zero"));
            }
            if (Boolean.TRUE.equals(request.extensionAllowed()) && request.maxExtensionDays() != null && request.maxExtensionDays() < 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_MAX_EXTENSION", "Max extension days must be >= 0"));
            }
            if (!Boolean.TRUE.equals(request.extensionAllowed()) && request.maxExtensionDays() != null && request.maxExtensionDays() > 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_MAX_EXTENSION", "Max extension days should be null or 0 when extension is not allowed"));
            }
        } else if (resource == JobArchitectureModels.Resource.NOTICE_PERIOD_POLICIES) {
            if (request.employeeNoticeDays() != null && request.employeeNoticeDays() < 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_EMP_NOTICE", "Employee notice days must be >= 0"));
            }
            if (request.employerNoticeDays() != null && request.employerNoticeDays() < 0) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_EMPR_NOTICE", "Employer notice days must be >= 0"));
            }
        } else if (resource == JobArchitectureModels.Resource.SEPARATION_REASONS) {
            if (!StringUtils.hasText(request.separationCategory())) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SEPARATION_CATEGORY_REQUIRED", "Separation category is required"));
            }
            try {
                JobArchitectureModels.SeparationCategory.valueOf(request.separationCategory().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SEPARATION_CATEGORY", "Invalid separation category"));
            }
        }
        return common;
    }

    private Mono<Void> validatePosition(String tenantId, JobArchitectureModels.MasterUpsertRequest request, UUID currentId) {
        if (request.designationId() == null || request.gradeId() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DESIGNATION_GRADE_REQUIRED", "Designation and grade are required"));
        }
        int approved = request.approvedHeadcount() == null ? 0 : request.approvedHeadcount();
        int filled = request.filledHeadcount() == null ? 0 : request.filledHeadcount();
        if (approved < 0 || filled < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_HEADCOUNT", "Headcount values must be >= 0"));
        }
        if (filled > approved) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "HEADCOUNT_EXCEEDED", "Filled headcount cannot exceed approved headcount"));
        }
        if (StringUtils.hasText(request.vacancyStatus())) {
            try {
                JobArchitectureModels.VacancyStatus.valueOf(request.vacancyStatus().trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_VACANCY_STATUS", "Invalid vacancy status"));
            }
        }
        if (currentId != null && request.reportsToPositionId() != null && currentId.equals(request.reportsToPositionId())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "POSITION_CYCLE", "Position cannot report to itself"));
        }

        Mono<Void> refs = Mono.when(
                requireExists(tenantId, "job_architecture.designations", request.designationId(), "DESIGNATION_NOT_FOUND"),
                requireExists(tenantId, "job_architecture.grades", request.gradeId(), "GRADE_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.job_families", request.jobFamilyId(), "JOB_FAMILY_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.job_functions", request.jobFunctionId(), "JOB_FUNCTION_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.grade_bands", request.gradeBandId(), "GRADE_BAND_NOT_FOUND"),
                optionalExists(tenantId, "organization.legal_entities", request.legalEntityId(), "LEGAL_ENTITY_NOT_FOUND"),
                optionalExists(tenantId, "organization.branches", request.branchId(), "BRANCH_NOT_FOUND"),
                optionalExists(tenantId, "organization.business_units", request.businessUnitId(), "BUSINESS_UNIT_NOT_FOUND"),
                optionalExists(tenantId, "organization.divisions", request.divisionId(), "DIVISION_NOT_FOUND"),
                optionalExists(tenantId, "organization.departments", request.departmentId(), "DEPARTMENT_NOT_FOUND"),
                optionalExists(tenantId, "organization.sections", request.sectionId(), "SECTION_NOT_FOUND"),
                optionalExists(tenantId, "organization.work_locations", request.workLocationId(), "WORK_LOCATION_NOT_FOUND"),
                optionalExists(tenantId, "organization.cost_centers", request.costCenterId(), "COST_CENTER_NOT_FOUND"),
                optionalExists(tenantId, "organization.reporting_units", request.reportingUnitId(), "REPORTING_UNIT_NOT_FOUND"),
                optionalExists(tenantId, "job_architecture.positions", request.reportsToPositionId(), "REPORTS_TO_POSITION_NOT_FOUND")
        );

        return refs.then(validatePositionCycle(tenantId, currentId, request.reportsToPositionId()));
    }

    private Mono<Void> validatePositionCycle(String tenantId, UUID currentId, UUID parentId) {
        if (currentId == null || parentId == null) {
            return Mono.empty();
        }
        return repository.findParentPosition(tenantId, parentId)
                .flatMap(next -> {
                    if (next == null) {
                        return Mono.empty();
                    }
                    if (currentId.equals(next)) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "POSITION_CYCLE", "Circular position hierarchy detected"));
                    }
                    return validatePositionCycle(tenantId, currentId, next);
                })
                .switchIfEmpty(Mono.empty());
    }

    private Mono<Void> validateSalaryRange(JobArchitectureModels.MasterUpsertRequest request) {
        if (request.salaryScaleMin() != null && request.salaryScaleMax() != null
                && request.salaryScaleMin().compareTo(request.salaryScaleMax()) > 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SALARY_RANGE", "Salary min cannot exceed salary max"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateStatusRules(
            String tenantId,
            JobArchitectureModels.Resource resource,
            JobArchitectureModels.MasterViewDto existing,
            boolean nextActive
    ) {
        if (resource == JobArchitectureModels.Resource.POSITIONS && existing.reportsToPositionId() != null && !nextActive) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "POSITION_DEACTIVATION_BLOCKED", "Cannot deactivate position with reporting linkage without reassignment"));
        }
        return Mono.empty();
    }

    private Mono<Void> requireExists(String tenantId, String tableName, UUID id, String errorCode) {
        return repository.existsById(tenantId, tableName, id)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, errorCode, errorCode.replace('_', ' ').toLowerCase())));
    }

    private Mono<Void> optionalExists(String tenantId, String tableName, UUID id, String errorCode) {
        if (id == null) {
            return Mono.empty();
        }
        return requireExists(tenantId, tableName, id, errorCode);
    }

    private Mono<String> withTenant() {
        return enablementGuard.requireModuleEnabled("job-architecture")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required"))));
    }

    private JobArchitectureModels.SearchQuery normalizeQuery(JobArchitectureModels.SearchQuery query) {
        if (query == null) {
            return new JobArchitectureModels.SearchQuery(null, null, 50, 0,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null);
        }
        return new JobArchitectureModels.SearchQuery(
                query.q(), query.active(), Math.min(Math.max(query.limit(), 1), 500), Math.max(0, query.offset()),
                query.jobFamilyId(), query.jobFunctionId(), query.gradeBandId(), query.designationId(), query.gradeId(),
                query.legalEntityId(), query.branchId(), query.departmentId(), query.costCenterId(),
                query.vacancyStatus(), query.criticalPositionFlag(), query.employeeCategoryId());
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, tenantId, action, targetType, targetId.toString(), metadata));
    }

    private <T> Mono<T> notFound(JobArchitectureModels.Resource resource) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource.name() + " record not found"));
    }
}
