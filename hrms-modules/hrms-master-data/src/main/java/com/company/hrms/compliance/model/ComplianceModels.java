package com.company.hrms.compliance.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class ComplianceModels {

    private ComplianceModels() {
    }

    public enum AppliesTo {
        EMPLOYEE,
        DEPENDENT,
        BOTH
    }

    public enum CivilIdAppliesTo {
        OMANI,
        EXPATRIATE,
        BOTH
    }

    public enum Resource {
        VISA_TYPES("visa-types", "master_data.visa_types", "visa_type_code", "visa_type_name"),
        RESIDENCE_STATUSES("residence-statuses", "master_data.residence_statuses", "residence_status_code", "residence_status_name"),
        LABOUR_CARD_TYPES("labour-card-types", "master_data.labour_card_types", "labour_card_type_code", "labour_card_type_name"),
        CIVIL_ID_TYPES("civil-id-types", "master_data.civil_id_types", "civil_id_type_code", "civil_id_type_name"),
        PASSPORT_TYPES("passport-types", "master_data.passport_types", "passport_type_code", "passport_type_name"),
        SPONSOR_TYPES("sponsor-types", "master_data.sponsor_types", "sponsor_type_code", "sponsor_type_name"),
        WORK_PERMIT_TYPES("work-permit-types", "master_data.work_permit_types", "work_permit_type_code", "work_permit_type_name"),
        NATIONALISATION_CATEGORIES(
                "nationalisation-categories",
                "master_data.nationalisation_categories",
                "nationalisation_category_code",
                "nationalisation_category_name"),
        SOCIAL_INSURANCE_TYPES(
                "social-insurance-types",
                "master_data.social_insurance_eligibility_types",
                "social_insurance_type_code",
                "social_insurance_type_name"),
        BENEFICIARY_TYPES("beneficiary-types", "master_data.beneficiary_types", "beneficiary_type_code", "beneficiary_type_name"),
        DEPENDENT_TYPES("dependent-types", "master_data.dependent_types", "dependent_type_code", "dependent_type_name");

        private final String path;
        private final String table;
        private final String codeColumn;
        private final String nameColumn;

        Resource(String path, String table, String codeColumn, String nameColumn) {
            this.path = path;
            this.table = table;
            this.codeColumn = codeColumn;
            this.nameColumn = nameColumn;
        }

        public String path() {
            return path;
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
            for (Resource value : values()) {
                if (value.path.equalsIgnoreCase(path)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown compliance resource: " + path);
        }

        public static Set<String> supportedPaths() {
            return Set.of(
                    "visa-types",
                    "residence-statuses",
                    "labour-card-types",
                    "civil-id-types",
                    "passport-types",
                    "sponsor-types",
                    "work-permit-types",
                    "nationalisation-categories",
                    "social-insurance-types",
                    "beneficiary-types",
                    "dependent-types");
        }
    }

    public record SearchQuery(String q, Boolean active, int limit, int offset) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record MasterUpsertRequest(
            String code,
            String name,
            String visaCategory,
            String appliesTo,
            Boolean renewableFlag,
            Boolean expiryTrackingRequired,
            Boolean omaniFlag,
            Boolean countsForOmanisationFlag,
            Boolean pensionEligibleFlag,
            Boolean occupationalHazardEligibleFlag,
            Boolean govtContributionApplicableFlag,
            Integer priorityOrder,
            Boolean insuranceEligibleFlag,
            Boolean familyVisaEligibleFlag,
            String description,
            Boolean active
    ) {
    }

    public record MasterViewDto(
            UUID id,
            String tenantId,
            String code,
            String name,
            String visaCategory,
            String appliesTo,
            Boolean renewableFlag,
            Boolean expiryTrackingRequired,
            Boolean omaniFlag,
            Boolean countsForOmanisationFlag,
            Boolean pensionEligibleFlag,
            Boolean occupationalHazardEligibleFlag,
            Boolean govtContributionApplicableFlag,
            Integer priorityOrder,
            Boolean insuranceEligibleFlag,
            Boolean familyVisaEligibleFlag,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }
}
