package com.company.hrms.jobarchitecture.repository;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class JobArchitectureR2dbcRepository implements JobArchitectureRepository {

    private final DatabaseClient databaseClient;

    public JobArchitectureR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> create(String tenantId, JobArchitectureModels.Resource resource, JobArchitectureModels.MasterUpsertRequest request, String actor) {
        UUID id = UUID.randomUUID();
        return upsert(tenantId, resource, id, request, actor, true);
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> update(String tenantId, JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.MasterUpsertRequest request, String actor) {
        return upsert(tenantId, resource, id, request, actor, false);
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> get(String tenantId, JobArchitectureModels.Resource resource, UUID id) {
        return databaseClient.sql("SELECT * FROM job_architecture." + resource.table() + " WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, meta) -> mapResource(resource, row))
                .one();
    }

    @Override
    public Flux<JobArchitectureModels.MasterViewDto> list(String tenantId, JobArchitectureModels.Resource resource, JobArchitectureModels.SearchQuery query) {
        String codeColumn = resource.codeColumn();
        String nameColumn = resource.nameColumn();
        StringBuilder sql = new StringBuilder("SELECT * FROM job_architecture.").append(resource.table()).append(" WHERE tenant_id = :tenantId");
        if (StringUtils.hasText(query.q())) {
            sql.append(" AND (lower(").append(codeColumn).append(") LIKE lower(:q) OR lower(").append(nameColumn).append(") LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND active = :active");
        }

        if (resource == JobArchitectureModels.Resource.DESIGNATIONS) {
            if (query.jobFamilyId() != null) sql.append(" AND job_family_id = :jobFamilyId");
            if (query.jobFunctionId() != null) sql.append(" AND job_function_id = :jobFunctionId");
        } else if (resource == JobArchitectureModels.Resource.JOB_FUNCTIONS) {
            if (query.jobFamilyId() != null) sql.append(" AND job_family_id = :jobFamilyId");
        } else if (resource == JobArchitectureModels.Resource.GRADES) {
            if (query.gradeBandId() != null) sql.append(" AND grade_band_id = :gradeBandId");
        } else if (resource == JobArchitectureModels.Resource.POSITIONS) {
            if (query.designationId() != null) sql.append(" AND designation_id = :designationId");
            if (query.gradeId() != null) sql.append(" AND grade_id = :gradeId");
            if (query.gradeBandId() != null) sql.append(" AND grade_band_id = :gradeBandId");
            if (query.legalEntityId() != null) sql.append(" AND legal_entity_id = :legalEntityId");
            if (query.branchId() != null) sql.append(" AND branch_id = :branchId");
            if (query.departmentId() != null) sql.append(" AND department_id = :departmentId");
            if (query.costCenterId() != null) sql.append(" AND cost_center_id = :costCenterId");
            if (StringUtils.hasText(query.vacancyStatus())) sql.append(" AND vacancy_status = :vacancyStatus");
            if (query.criticalPositionFlag() != null) sql.append(" AND critical_position_flag = :criticalPositionFlag");
        } else if (resource == JobArchitectureModels.Resource.EMPLOYEE_SUBCATEGORIES && query.employeeCategoryId() != null) {
            sql.append(" AND employee_category_id = :employeeCategoryId");
        }

        sql.append(" ORDER BY updated_at DESC LIMIT :limit OFFSET :offset");
        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("limit", query.limit())
                .bind("offset", query.offset());

        if (StringUtils.hasText(query.q())) spec = spec.bind("q", "%" + query.q().trim() + "%");
        if (query.active() != null) spec = spec.bind("active", query.active());
        if (query.jobFamilyId() != null) spec = spec.bind("jobFamilyId", query.jobFamilyId());
        if (query.jobFunctionId() != null) spec = spec.bind("jobFunctionId", query.jobFunctionId());
        if (query.gradeBandId() != null) spec = spec.bind("gradeBandId", query.gradeBandId());
        if (query.designationId() != null) spec = spec.bind("designationId", query.designationId());
        if (query.gradeId() != null) spec = spec.bind("gradeId", query.gradeId());
        if (query.legalEntityId() != null) spec = spec.bind("legalEntityId", query.legalEntityId());
        if (query.branchId() != null) spec = spec.bind("branchId", query.branchId());
        if (query.departmentId() != null) spec = spec.bind("departmentId", query.departmentId());
        if (query.costCenterId() != null) spec = spec.bind("costCenterId", query.costCenterId());
        if (StringUtils.hasText(query.vacancyStatus())) spec = spec.bind("vacancyStatus", query.vacancyStatus().trim().toUpperCase());
        if (query.criticalPositionFlag() != null) spec = spec.bind("criticalPositionFlag", query.criticalPositionFlag());
        if (query.employeeCategoryId() != null) spec = spec.bind("employeeCategoryId", query.employeeCategoryId());

        return spec.map((row, meta) -> mapResource(resource, row)).all();
    }

    @Override
    public Mono<JobArchitectureModels.MasterViewDto> updateStatus(String tenantId, JobArchitectureModels.Resource resource, UUID id, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE job_architecture.%s
                        SET active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE tenant_id = :tenantId AND id = :id
                        """.formatted(resource.table()))
                .bind("active", active)
                .bind("updatedBy", actor)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then(get(tenantId, resource, id));
    }

    @Override
    public Flux<JobArchitectureModels.OptionViewDto> options(String tenantId, JobArchitectureModels.Resource resource, String q, int limit) {
        String sql = """
                SELECT id, %s AS code, %s AS name
                FROM job_architecture.%s
                WHERE tenant_id = :tenantId
                  AND active = TRUE
                  AND (lower(%s) LIKE lower(:q) OR lower(%s) LIKE lower(:q))
                ORDER BY %s ASC
                LIMIT :limit
                """.formatted(resource.codeColumn(), resource.nameColumn(), resource.table(), resource.codeColumn(), resource.nameColumn(), resource.nameColumn());
        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("q", StringUtils.hasText(q) ? "%" + q.trim() + "%" : "%")
                .bind("limit", limit)
                .map((row, meta) -> new JobArchitectureModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> codeExists(String tenantId, JobArchitectureModels.Resource resource, String code, UUID excludeId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM job_architecture.")
                .append(resource.table())
                .append(" WHERE tenant_id = :tenantId AND lower(")
                .append(resource.codeColumn())
                .append(") = lower(:code)");
        if (excludeId != null) {
            sql.append(" AND id <> :excludeId");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("code", code.trim());
        if (excludeId != null) spec = spec.bind("excludeId", excludeId);
        return spec.map((row, meta) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Boolean> existsById(String tenantId, String tableName, UUID id) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM " + tableName + " WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, meta) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<UUID> findParentPosition(String tenantId, UUID id) {
        return databaseClient.sql("SELECT reports_to_position_id FROM job_architecture.positions WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, meta) -> row.get("reports_to_position_id", UUID.class))
                .one();
    }

    private Mono<JobArchitectureModels.MasterViewDto> upsert(
            String tenantId,
            JobArchitectureModels.Resource resource,
            UUID id,
            JobArchitectureModels.MasterUpsertRequest request,
            String actor,
            boolean isCreate
    ) {
        String code = request.code().trim().toUpperCase();
        String name = request.name().trim();
        boolean active = request.active() == null || request.active();

        if (resource == JobArchitectureModels.Resource.POSITIONS) {
            return upsertPosition(tenantId, id, request, actor, active, code, name, isCreate);
        }

        String sql = switch (resource) {
            case DESIGNATIONS -> isCreate
                    ? "INSERT INTO job_architecture.designations(id, tenant_id, designation_code, designation_name, short_name, job_family_id, job_function_id, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :shortName, :jobFamilyId, :jobFunctionId, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.designations SET designation_code = :code, designation_name = :name, short_name = :shortName, job_family_id = :jobFamilyId, job_function_id = :jobFunctionId, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case JOB_FAMILIES -> isCreate
                    ? "INSERT INTO job_architecture.job_families(id, tenant_id, job_family_code, job_family_name, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.job_families SET job_family_code = :code, job_family_name = :name, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case JOB_FUNCTIONS -> isCreate
                    ? "INSERT INTO job_architecture.job_functions(id, tenant_id, job_function_code, job_function_name, job_family_id, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :jobFamilyId, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.job_functions SET job_function_code = :code, job_function_name = :name, job_family_id = :jobFamilyId, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case GRADE_BANDS -> isCreate
                    ? "INSERT INTO job_architecture.grade_bands(id, tenant_id, grade_band_code, grade_band_name, band_order, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :bandOrder, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.grade_bands SET grade_band_code = :code, grade_band_name = :name, band_order = :bandOrder, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case GRADES -> isCreate
                    ? "INSERT INTO job_architecture.grades(id, tenant_id, grade_code, grade_name, grade_band_id, ranking_order, salary_scale_min, salary_scale_max, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :gradeBandId, :rankingOrder, :salaryScaleMin, :salaryScaleMax, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.grades SET grade_code = :code, grade_name = :name, grade_band_id = :gradeBandId, ranking_order = :rankingOrder, salary_scale_min = :salaryScaleMin, salary_scale_max = :salaryScaleMax, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case EMPLOYMENT_TYPES -> isCreate
                    ? "INSERT INTO job_architecture.employment_types(id, tenant_id, employment_type_code, employment_type_name, contract_required, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :contractRequired, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.employment_types SET employment_type_code = :code, employment_type_name = :name, contract_required = :contractRequired, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case WORKER_TYPES -> isCreate
                    ? "INSERT INTO job_architecture.worker_types(id, tenant_id, worker_type_code, worker_type_name, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.worker_types SET worker_type_code = :code, worker_type_name = :name, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case EMPLOYEE_CATEGORIES -> isCreate
                    ? "INSERT INTO job_architecture.employee_categories(id, tenant_id, employee_category_code, employee_category_name, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.employee_categories SET employee_category_code = :code, employee_category_name = :name, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case EMPLOYEE_SUBCATEGORIES -> isCreate
                    ? "INSERT INTO job_architecture.employee_subcategories(id, tenant_id, employee_subcategory_code, employee_subcategory_name, employee_category_id, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :employeeCategoryId, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.employee_subcategories SET employee_subcategory_code = :code, employee_subcategory_name = :name, employee_category_id = :employeeCategoryId, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case CONTRACT_TYPES -> isCreate
                    ? "INSERT INTO job_architecture.contract_types(id, tenant_id, contract_type_code, contract_type_name, fixed_term_flag, default_duration_days, renewal_allowed, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :fixedTermFlag, :defaultDurationDays, :renewalAllowed, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.contract_types SET contract_type_code = :code, contract_type_name = :name, fixed_term_flag = :fixedTermFlag, default_duration_days = :defaultDurationDays, renewal_allowed = :renewalAllowed, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case PROBATION_POLICIES -> isCreate
                    ? "INSERT INTO job_architecture.probation_policies(id, tenant_id, probation_policy_code, probation_policy_name, duration_days, extension_allowed, max_extension_days, confirmation_required, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :durationDays, :extensionAllowed, :maxExtensionDays, :confirmationRequired, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.probation_policies SET probation_policy_code = :code, probation_policy_name = :name, duration_days = :durationDays, extension_allowed = :extensionAllowed, max_extension_days = :maxExtensionDays, confirmation_required = :confirmationRequired, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case NOTICE_PERIOD_POLICIES -> isCreate
                    ? "INSERT INTO job_architecture.notice_period_policies(id, tenant_id, notice_policy_code, notice_policy_name, employee_notice_days, employer_notice_days, payment_in_lieu_allowed, garden_leave_allowed, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :employeeNoticeDays, :employerNoticeDays, :paymentInLieuAllowed, :gardenLeaveAllowed, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.notice_period_policies SET notice_policy_code = :code, notice_policy_name = :name, employee_notice_days = :employeeNoticeDays, employer_notice_days = :employerNoticeDays, payment_in_lieu_allowed = :paymentInLieuAllowed, garden_leave_allowed = :gardenLeaveAllowed, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case TRANSFER_TYPES -> isCreate
                    ? "INSERT INTO job_architecture.transfer_types(id, tenant_id, transfer_type_code, transfer_type_name, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.transfer_types SET transfer_type_code = :code, transfer_type_name = :name, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case PROMOTION_TYPES -> isCreate
                    ? "INSERT INTO job_architecture.promotion_types(id, tenant_id, promotion_type_code, promotion_type_name, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.promotion_types SET promotion_type_code = :code, promotion_type_name = :name, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            case SEPARATION_REASONS -> isCreate
                    ? "INSERT INTO job_architecture.separation_reasons(id, tenant_id, separation_reason_code, separation_reason_name, separation_category, voluntary_flag, final_settlement_required, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :separationCategory, :voluntaryFlag, :finalSettlementRequired, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                    : "UPDATE job_architecture.separation_reasons SET separation_reason_code = :code, separation_reason_name = :name, separation_category = :separationCategory, voluntary_flag = :voluntaryFlag, final_settlement_required = :finalSettlementRequired, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
            default -> throw new IllegalStateException("Unsupported resource for generic upsert");
        };

        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", code)
                .bind("name", name)
                .bind("active", active)
                .bind("actor", actor);

        spec = bindNullable(spec, "shortName", request.shortName(), String.class);
        spec = bindNullable(spec, "jobFamilyId", request.jobFamilyId(), UUID.class);
        spec = bindNullable(spec, "jobFunctionId", request.jobFunctionId(), UUID.class);
        spec = bindNullable(spec, "description", request.description(), String.class);
        spec = bindNullable(spec, "bandOrder", request.bandOrder(), Integer.class);
        spec = bindNullable(spec, "gradeBandId", request.gradeBandId(), UUID.class);
        spec = bindNullable(spec, "rankingOrder", request.rankingOrder(), Integer.class);
        spec = bindNullable(spec, "salaryScaleMin", request.salaryScaleMin(), BigDecimal.class);
        spec = bindNullable(spec, "salaryScaleMax", request.salaryScaleMax(), BigDecimal.class);
        spec = bindNullable(spec, "contractRequired", request.contractRequired(), Boolean.class);
        spec = bindNullable(spec, "employeeCategoryId", request.employeeCategoryId(), UUID.class);
        spec = bindNullable(spec, "fixedTermFlag", request.fixedTermFlag(), Boolean.class);
        spec = bindNullable(spec, "defaultDurationDays", request.defaultDurationDays(), Integer.class);
        spec = bindNullable(spec, "renewalAllowed", request.renewalAllowed(), Boolean.class);
        spec = bindNullable(spec, "durationDays", request.durationDays(), Integer.class);
        spec = bindNullable(spec, "extensionAllowed", request.extensionAllowed(), Boolean.class);
        spec = bindNullable(spec, "maxExtensionDays", request.maxExtensionDays(), Integer.class);
        spec = bindNullable(spec, "confirmationRequired", request.confirmationRequired(), Boolean.class);
        spec = bindNullable(spec, "employeeNoticeDays", request.employeeNoticeDays(), Integer.class);
        spec = bindNullable(spec, "employerNoticeDays", request.employerNoticeDays(), Integer.class);
        spec = bindNullable(spec, "paymentInLieuAllowed", request.paymentInLieuAllowed(), Boolean.class);
        spec = bindNullable(spec, "gardenLeaveAllowed", request.gardenLeaveAllowed(), Boolean.class);
        spec = bindNullable(spec, "separationCategory", request.separationCategory() == null ? null : request.separationCategory().trim().toUpperCase(), String.class);
        spec = bindNullable(spec, "voluntaryFlag", request.voluntaryFlag(), Boolean.class);
        spec = bindNullable(spec, "finalSettlementRequired", request.finalSettlementRequired(), Boolean.class);

        return spec.fetch().rowsUpdated().then(get(tenantId, resource, id));
    }

    private Mono<JobArchitectureModels.MasterViewDto> upsertPosition(
            String tenantId,
            UUID id,
            JobArchitectureModels.MasterUpsertRequest request,
            String actor,
            boolean active,
            String code,
            String name,
            boolean isCreate
    ) {
        String sql = isCreate
                ? "INSERT INTO job_architecture.positions(id, tenant_id, position_code, position_name, designation_id, job_family_id, job_function_id, grade_id, grade_band_id, legal_entity_id, branch_id, business_unit_id, division_id, department_id, section_id, work_location_id, cost_center_id, reporting_unit_id, reports_to_position_id, approved_headcount, filled_headcount, vacancy_status, critical_position_flag, description, active, created_at, updated_at, created_by, updated_by) VALUES (:id, :tenantId, :code, :name, :designationId, :jobFamilyId, :jobFunctionId, :gradeId, :gradeBandId, :legalEntityId, :branchId, :businessUnitId, :divisionId, :departmentId, :sectionId, :workLocationId, :costCenterId, :reportingUnitId, :reportsToPositionId, :approvedHeadcount, :filledHeadcount, :vacancyStatus, :criticalPositionFlag, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :actor, :actor)"
                : "UPDATE job_architecture.positions SET position_code = :code, position_name = :name, designation_id = :designationId, job_family_id = :jobFamilyId, job_function_id = :jobFunctionId, grade_id = :gradeId, grade_band_id = :gradeBandId, legal_entity_id = :legalEntityId, branch_id = :branchId, business_unit_id = :businessUnitId, division_id = :divisionId, department_id = :departmentId, section_id = :sectionId, work_location_id = :workLocationId, cost_center_id = :costCenterId, reporting_unit_id = :reportingUnitId, reports_to_position_id = :reportsToPositionId, approved_headcount = :approvedHeadcount, filled_headcount = :filledHeadcount, vacancy_status = :vacancyStatus, critical_position_flag = :criticalPositionFlag, description = :description, active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :actor WHERE tenant_id = :tenantId AND id = :id";
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", code)
                .bind("name", name)
                .bind("designationId", request.designationId())
                .bind("gradeId", request.gradeId())
                .bind("approvedHeadcount", request.approvedHeadcount() == null ? 0 : request.approvedHeadcount())
                .bind("filledHeadcount", request.filledHeadcount() == null ? 0 : request.filledHeadcount())
                .bind("vacancyStatus", request.vacancyStatus() == null ? JobArchitectureModels.VacancyStatus.VACANT.name() : request.vacancyStatus().trim().toUpperCase())
                .bind("criticalPositionFlag", request.criticalPositionFlag() != null && request.criticalPositionFlag())
                .bind("active", active)
                .bind("actor", actor);
        spec = bindNullable(spec, "jobFamilyId", request.jobFamilyId(), UUID.class);
        spec = bindNullable(spec, "jobFunctionId", request.jobFunctionId(), UUID.class);
        spec = bindNullable(spec, "gradeBandId", request.gradeBandId(), UUID.class);
        spec = bindNullable(spec, "legalEntityId", request.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "branchId", request.branchId(), UUID.class);
        spec = bindNullable(spec, "businessUnitId", request.businessUnitId(), UUID.class);
        spec = bindNullable(spec, "divisionId", request.divisionId(), UUID.class);
        spec = bindNullable(spec, "departmentId", request.departmentId(), UUID.class);
        spec = bindNullable(spec, "sectionId", request.sectionId(), UUID.class);
        spec = bindNullable(spec, "workLocationId", request.workLocationId(), UUID.class);
        spec = bindNullable(spec, "costCenterId", request.costCenterId(), UUID.class);
        spec = bindNullable(spec, "reportingUnitId", request.reportingUnitId(), UUID.class);
        spec = bindNullable(spec, "reportsToPositionId", request.reportsToPositionId(), UUID.class);
        spec = bindNullable(spec, "description", request.description(), String.class);
        return spec.fetch().rowsUpdated().then(get(tenantId, JobArchitectureModels.Resource.POSITIONS, id));
    }

    private JobArchitectureModels.MasterViewDto mapResource(JobArchitectureModels.Resource resource, Row row) {
        UUID id = row.get("id", UUID.class);
        String tenantId = row.get("tenant_id", String.class);
        boolean active = Boolean.TRUE.equals(row.get("active", Boolean.class));
        Instant createdAt = row.get("created_at", Instant.class);
        Instant updatedAt = row.get("updated_at", Instant.class);
        String createdBy = row.get("created_by", String.class);
        String updatedBy = row.get("updated_by", String.class);

        return switch (resource) {
            case DESIGNATIONS -> baseSimple(id, tenantId, row.get("designation_code", String.class), row.get("designation_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case JOB_FAMILIES -> baseSimple(id, tenantId, row.get("job_family_code", String.class), row.get("job_family_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case JOB_FUNCTIONS -> baseSimple(id, tenantId, row.get("job_function_code", String.class), row.get("job_function_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case GRADE_BANDS -> baseSimple(id, tenantId, row.get("grade_band_code", String.class), row.get("grade_band_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case GRADES -> baseSimple(id, tenantId, row.get("grade_code", String.class), row.get("grade_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case POSITIONS -> baseSimple(id, tenantId, row.get("position_code", String.class), row.get("position_name", String.class), row.get("description", String.class), row.get("reports_to_position_id", UUID.class), row.get("approved_headcount", Integer.class), row.get("filled_headcount", Integer.class), row.get("vacancy_status", String.class), active, createdAt, updatedAt, createdBy, updatedBy);
            case EMPLOYMENT_TYPES -> baseSimple(id, tenantId, row.get("employment_type_code", String.class), row.get("employment_type_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case WORKER_TYPES -> baseSimple(id, tenantId, row.get("worker_type_code", String.class), row.get("worker_type_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case EMPLOYEE_CATEGORIES -> baseSimple(id, tenantId, row.get("employee_category_code", String.class), row.get("employee_category_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case EMPLOYEE_SUBCATEGORIES -> baseSimple(id, tenantId, row.get("employee_subcategory_code", String.class), row.get("employee_subcategory_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case CONTRACT_TYPES -> baseSimple(id, tenantId, row.get("contract_type_code", String.class), row.get("contract_type_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case PROBATION_POLICIES -> baseSimple(id, tenantId, row.get("probation_policy_code", String.class), row.get("probation_policy_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case NOTICE_PERIOD_POLICIES -> baseSimple(id, tenantId, row.get("notice_policy_code", String.class), row.get("notice_policy_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case TRANSFER_TYPES -> baseSimple(id, tenantId, row.get("transfer_type_code", String.class), row.get("transfer_type_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case PROMOTION_TYPES -> baseSimple(id, tenantId, row.get("promotion_type_code", String.class), row.get("promotion_type_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
            case SEPARATION_REASONS -> baseSimple(id, tenantId, row.get("separation_reason_code", String.class), row.get("separation_reason_name", String.class), row.get("description", String.class), null, null, null, null, active, createdAt, updatedAt, createdBy, updatedBy);
        };
    }

    private JobArchitectureModels.MasterViewDto baseSimple(
            UUID id,
            String tenantId,
            String code,
            String name,
            String description,
            UUID reportsToPositionId,
            Integer approvedHeadcount,
            Integer filledHeadcount,
            String vacancyStatus,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
        return new JobArchitectureModels.MasterViewDto(
                id, tenantId, code, name, description,
                reportsToPositionId,
                approvedHeadcount,
                filledHeadcount,
                vacancyStatus,
                active, createdAt, updatedAt, createdBy, updatedBy);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
