package com.company.hrms.jobarchitecture.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class JobArchitectureModels {

    private JobArchitectureModels() {
    }

    public enum VacancyStatus {
        VACANT,
        PARTIALLY_FILLED,
        FILLED,
        FROZEN
    }

    public enum SeparationCategory {
        RESIGNATION,
        TERMINATION,
        RETIREMENT,
        CONTRACT_EXPIRY,
        DEATH,
        ABSCONDING,
        OTHER
    }

    public enum Resource {
        DESIGNATIONS("designations", "designation_code", "designation_name"),
        JOB_FAMILIES("job_families", "job_family_code", "job_family_name"),
        JOB_FUNCTIONS("job_functions", "job_function_code", "job_function_name"),
        GRADE_BANDS("grade_bands", "grade_band_code", "grade_band_name"),
        GRADES("grades", "grade_code", "grade_name"),
        POSITIONS("positions", "position_code", "position_name"),
        EMPLOYMENT_TYPES("employment_types", "employment_type_code", "employment_type_name"),
        WORKER_TYPES("worker_types", "worker_type_code", "worker_type_name"),
        EMPLOYEE_CATEGORIES("employee_categories", "employee_category_code", "employee_category_name"),
        EMPLOYEE_SUBCATEGORIES("employee_subcategories", "employee_subcategory_code", "employee_subcategory_name"),
        CONTRACT_TYPES("contract_types", "contract_type_code", "contract_type_name"),
        PROBATION_POLICIES("probation_policies", "probation_policy_code", "probation_policy_name"),
        NOTICE_PERIOD_POLICIES("notice_period_policies", "notice_policy_code", "notice_policy_name"),
        TRANSFER_TYPES("transfer_types", "transfer_type_code", "transfer_type_name"),
        PROMOTION_TYPES("promotion_types", "promotion_type_code", "promotion_type_name"),
        SEPARATION_REASONS("separation_reasons", "separation_reason_code", "separation_reason_name");

        private final String table;
        private final String codeColumn;
        private final String nameColumn;

        Resource(String table, String codeColumn, String nameColumn) {
            this.table = table;
            this.codeColumn = codeColumn;
            this.nameColumn = nameColumn;
        }

        public String table() {
            return table;
        }

        public String codeColumn() {
            return codeColumn;
        }

        public String nameColumn() {
            return nameColumn;
        }

        public static Resource fromPath(String path) {
            for (Resource resource : values()) {
                if (resource.table.equalsIgnoreCase(path)) {
                    return resource;
                }
            }
            throw new IllegalArgumentException("Unknown resource: " + path);
        }

        public static Set<String> supportedPaths() {
            return Set.of(
                    "designations", "job-families", "job-functions", "grade-bands", "grades", "positions",
                    "employment-types", "worker-types", "employee-categories", "employee-subcategories",
                    "contract-types", "probation-policies", "notice-period-policies", "transfer-types",
                    "promotion-types", "separation-reasons");
        }
    }

    public record SearchQuery(
            String q,
            Boolean active,
            int limit,
            int offset,
            UUID jobFamilyId,
            UUID jobFunctionId,
            UUID gradeBandId,
            UUID designationId,
            UUID gradeId,
            UUID legalEntityId,
            UUID branchId,
            UUID departmentId,
            UUID costCenterId,
            String vacancyStatus,
            Boolean criticalPositionFlag,
            UUID employeeCategoryId
    ) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record MasterUpsertRequest(
            String code,
            String name,
            String shortName,
            UUID jobFamilyId,
            UUID jobFunctionId,
            String description,
            Integer bandOrder,
            UUID gradeBandId,
            Integer rankingOrder,
            BigDecimal salaryScaleMin,
            BigDecimal salaryScaleMax,
            UUID designationId,
            UUID gradeId,
            UUID legalEntityId,
            UUID branchId,
            UUID businessUnitId,
            UUID divisionId,
            UUID departmentId,
            UUID sectionId,
            UUID workLocationId,
            UUID costCenterId,
            UUID reportingUnitId,
            UUID reportsToPositionId,
            Integer approvedHeadcount,
            Integer filledHeadcount,
            String vacancyStatus,
            Boolean criticalPositionFlag,
            Boolean contractRequired,
            UUID employeeCategoryId,
            Boolean fixedTermFlag,
            Integer defaultDurationDays,
            Boolean renewalAllowed,
            Integer durationDays,
            Boolean extensionAllowed,
            Integer maxExtensionDays,
            Boolean confirmationRequired,
            Integer employeeNoticeDays,
            Integer employerNoticeDays,
            Boolean paymentInLieuAllowed,
            Boolean gardenLeaveAllowed,
            String separationCategory,
            Boolean voluntaryFlag,
            Boolean finalSettlementRequired,
            Boolean active
    ) {
    }

    public record MasterViewDto(
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
    }
}
