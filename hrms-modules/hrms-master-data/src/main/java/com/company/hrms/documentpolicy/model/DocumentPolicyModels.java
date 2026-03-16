package com.company.hrms.documentpolicy.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DocumentPolicyModels {

    private DocumentPolicyModels() {
    }

    public enum DocumentFor {
        EMPLOYEE,
        EMPLOYER,
        DEPENDENT,
        BOTH
    }

    public enum MimeGroup {
        PDF,
        IMAGE,
        OFFICE_DOC,
        ARCHIVE,
        OTHER
    }

    public enum Resource {
        DOCUMENT_CATEGORIES("document-categories", "master_data.document_categories", "document_category_code", "document_category_name"),
        DOCUMENT_TYPES("document-types", "master_data.document_types", "document_type_code", "document_type_name"),
        DOCUMENT_APPLICABILITY_RULES(
                "document-applicability-rules",
                "master_data.document_applicability_rules",
                "applicability_rule_code",
                "applicability_rule_code"),
        DOCUMENT_EXPIRY_RULES("document-expiry-rules", "master_data.document_expiry_rules", "expiry_rule_code", "expiry_rule_code"),
        POLICY_DOCUMENT_TYPES(
                "policy-document-types",
                "master_data.policy_document_types",
                "policy_document_type_code",
                "policy_document_type_name"),
        POLICY_ACKNOWLEDGEMENT_TYPES(
                "policy-acknowledgement-types",
                "master_data.policy_acknowledgement_types",
                "policy_ack_type_code",
                "policy_ack_type_name"),
        ATTACHMENT_CATEGORIES(
                "attachment-categories",
                "master_data.attachment_categories",
                "attachment_category_code",
                "attachment_category_name");

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
            throw new IllegalArgumentException("Unknown document-policy resource: " + path);
        }

        public static Set<String> supportedPaths() {
            return Set.of(
                    "document-categories",
                    "document-types",
                    "document-applicability-rules",
                    "document-expiry-rules",
                    "policy-document-types",
                    "policy-acknowledgement-types",
                    "attachment-categories");
        }

        public Set<String> sortableColumns() {
            return switch (this) {
                case DOCUMENT_CATEGORIES -> Set.of("document_category_code", "document_category_name", "display_order", "updated_at", "created_at");
                case DOCUMENT_TYPES -> Set.of("document_type_code", "document_type_name", "updated_at", "created_at");
                case DOCUMENT_APPLICABILITY_RULES -> Set.of("applicability_rule_code", "updated_at", "created_at");
                case DOCUMENT_EXPIRY_RULES -> Set.of("expiry_rule_code", "updated_at", "created_at");
                case POLICY_DOCUMENT_TYPES -> Set.of("policy_document_type_code", "policy_document_type_name", "updated_at", "created_at");
                case POLICY_ACKNOWLEDGEMENT_TYPES -> Set.of("policy_ack_type_code", "policy_ack_type_name", "updated_at", "created_at");
                case ATTACHMENT_CATEGORIES -> Set.of("attachment_category_code", "attachment_category_name", "max_file_size_mb", "updated_at", "created_at");
            };
        }
    }

    public record SearchQuery(
            String q,
            Boolean active,
            int limit,
            int offset,
            String sort,
            UUID documentCategoryId,
            String documentFor,
            UUID documentTypeId,
            UUID workerTypeId,
            UUID employeeCategoryId,
            UUID nationalisationCategoryId,
            UUID legalEntityId,
            UUID jobFamilyId,
            UUID designationId,
            UUID dependentTypeId,
            Boolean mandatoryFlag,
            Boolean onboardingRequiredFlag,
            Boolean expiryTrackingRequired,
            Boolean renewalRequired,
            Boolean blockTransactionOnExpiryFlag,
            String mimeGroup
    ) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record MasterUpsertRequest(
            String code,
            String name,
            String shortDescription,
            String documentFor,
            UUID documentCategoryId,
            Boolean attachmentRequired,
            Boolean issueDateRequired,
            Boolean expiryDateRequired,
            Boolean referenceNoRequired,
            Boolean multipleAllowed,
            Integer displayOrder,
            UUID documentTypeId,
            UUID workerTypeId,
            UUID employeeCategoryId,
            UUID nationalisationCategoryId,
            UUID legalEntityId,
            UUID jobFamilyId,
            UUID designationId,
            UUID dependentTypeId,
            Boolean mandatoryFlag,
            Boolean onboardingRequiredFlag,
            Boolean expiryTrackingRequired,
            Boolean renewalRequired,
            List<Integer> alertDaysBefore,
            Integer gracePeriodDays,
            Boolean blockTransactionOnExpiryFlag,
            Boolean versionRequiredFlag,
            Boolean eSignatureRequiredFlag,
            Boolean reackOnVersionChangeFlag,
            Boolean annualReackFlag,
            String mimeGroup,
            Integer maxFileSizeMb,
            String description,
            Boolean active
    ) {
    }

    public record MasterViewDto(
            UUID id,
            String tenantId,
            String code,
            String name,
            String shortDescription,
            String documentFor,
            UUID documentCategoryId,
            Boolean attachmentRequired,
            Boolean issueDateRequired,
            Boolean expiryDateRequired,
            Boolean referenceNoRequired,
            Boolean multipleAllowed,
            Integer displayOrder,
            UUID documentTypeId,
            UUID workerTypeId,
            UUID employeeCategoryId,
            UUID nationalisationCategoryId,
            UUID legalEntityId,
            UUID jobFamilyId,
            UUID designationId,
            UUID dependentTypeId,
            Boolean mandatoryFlag,
            Boolean onboardingRequiredFlag,
            Boolean expiryTrackingRequired,
            Boolean renewalRequired,
            List<Integer> alertDaysBefore,
            Integer gracePeriodDays,
            Boolean blockTransactionOnExpiryFlag,
            Boolean versionRequiredFlag,
            Boolean eSignatureRequiredFlag,
            Boolean reackOnVersionChangeFlag,
            Boolean annualReackFlag,
            String mimeGroup,
            Integer maxFileSizeMb,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }
}
