package com.company.hrms.workflowaccess.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class WorkflowAccessModels {

    private WorkflowAccessModels() {
    }

    public enum RoleType {
        SYSTEM,
        TENANT,
        CUSTOM
    }

    public enum PermissionActionType {
        VIEW,
        CREATE,
        EDIT,
        DELETE,
        APPROVE,
        REJECT,
        EXPORT,
        PUBLISH,
        ACKNOWLEDGE,
        INITIATE
    }

    public enum ScopeType {
        SELF,
        TEAM,
        DEPARTMENT,
        ENTITY,
        ALL
    }

    public enum InitiationChannel {
        ESS,
        MSS,
        HR,
        SYSTEM,
        PAYROLL,
        ADMIN
    }

    public enum ApproverSourceType {
        ROLE,
        USER,
        REPORT_TO_POSITION,
        DEPARTMENT_HEAD,
        WORKFLOW_INITIATOR_MANAGER
    }

    public enum ChannelType {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }

    public enum Resource {
        ROLES("roles", "master_data.roles", "role_code", "role_name", true),
        PERMISSIONS("permissions", "master_data.permissions", "permission_code", "permission_name", false),
        ROLE_PERMISSION_MAPPINGS("role-permission-mappings", "master_data.role_permission_mappings", "id", "id", true),
        WORKFLOW_TYPES("workflow-types", "master_data.workflow_types", "workflow_type_code", "workflow_type_name", false),
        APPROVAL_MATRICES("approval-matrices", "master_data.approval_matrices", "approval_matrix_code", "matrix_name", true),
        NOTIFICATION_TEMPLATES("notification-templates", "master_data.notification_templates", "template_code", "template_name", true),
        SERVICE_REQUEST_TYPES("service-request-types", "master_data.service_request_types", "service_request_type_code", "service_request_type_name", true),
        DELEGATION_TYPES("delegation-types", "master_data.delegation_types", "delegation_type_code", "delegation_type_name", false),
        APPROVAL_ACTION_TYPES("approval-action-types", "master_data.approval_action_types", "approval_action_type_code", "approval_action_type_name", false);

        private final String path;
        private final String table;
        private final String codeColumn;
        private final String nameColumn;
        private final boolean tenantOwned;

        Resource(String path, String table, String codeColumn, String nameColumn, boolean tenantOwned) {
            this.path = path;
            this.table = table;
            this.codeColumn = codeColumn;
            this.nameColumn = nameColumn;
            this.tenantOwned = tenantOwned;
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

        public boolean tenantOwned() {
            return tenantOwned;
        }

        public static Resource fromPath(String path) {
            for (Resource value : values()) {
                if (value.path.equalsIgnoreCase(path)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown workflow-access resource: " + path);
        }

        public Set<String> sortableColumns() {
            return switch (this) {
                case ROLES -> Set.of("role_code", "role_name", "updated_at");
                case PERMISSIONS -> Set.of("permission_code", "permission_name", "module_code", "updated_at");
                case ROLE_PERMISSION_MAPPINGS -> Set.of("updated_at", "created_at");
                case WORKFLOW_TYPES -> Set.of("workflow_type_code", "workflow_type_name", "module_name", "updated_at");
                case APPROVAL_MATRICES -> Set.of("approval_matrix_code", "matrix_name", "level_no", "updated_at");
                case NOTIFICATION_TEMPLATES -> Set.of("template_code", "template_name", "event_code", "updated_at");
                case SERVICE_REQUEST_TYPES -> Set.of("service_request_type_code", "service_request_type_name", "category", "updated_at");
                case DELEGATION_TYPES -> Set.of("delegation_type_code", "delegation_type_name", "updated_at");
                case APPROVAL_ACTION_TYPES -> Set.of("approval_action_type_code", "approval_action_type_name", "updated_at");
            };
        }
    }

    public record SearchQuery(
            String q,
            Boolean active,
            int limit,
            int offset,
            String sort,
            String roleType,
            String moduleCode,
            String actionType,
            String scopeType,
            UUID roleId,
            UUID permissionId,
            Boolean allowFlag,
            String dataScopeOverride,
            String moduleName,
            String initiationChannel,
            Boolean approvalRequiredFlag,
            UUID workflowTypeId,
            UUID legalEntityId,
            UUID branchId,
            UUID departmentId,
            UUID employeeCategoryId,
            UUID workerTypeId,
            UUID serviceRequestTypeId,
            String approverSourceType,
            Integer levelNo,
            Boolean delegationAllowedFlag,
            String eventCode,
            String channelType,
            String languageCode,
            String category,
            Boolean attachmentRequiredFlag,
            Boolean autoCloseAllowedFlag,
            Boolean approvalAllowedFlag,
            Boolean actionAllowedFlag,
            Boolean viewAllowedFlag,
            Boolean temporaryOnlyFlag,
            Boolean finalActionFlag
    ) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record MasterUpsertRequest(
            String code,
            String name,
            String roleType,
            String moduleCode,
            String actionType,
            String scopeType,
            UUID roleId,
            UUID permissionId,
            Boolean allowFlag,
            String dataScopeOverride,
            String moduleName,
            String initiationChannel,
            Boolean approvalRequiredFlag,
            UUID workflowTypeId,
            String matrixName,
            UUID legalEntityId,
            UUID branchId,
            UUID departmentId,
            UUID employeeCategoryId,
            UUID workerTypeId,
            UUID serviceRequestTypeId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer levelNo,
            String approverSourceType,
            UUID approverRoleId,
            String approverUserRef,
            UUID approvalActionTypeId,
            Integer escalationDays,
            Boolean delegationAllowedFlag,
            String eventCode,
            String channelType,
            String subjectTemplate,
            String bodyTemplate,
            String languageCode,
            String category,
            Boolean attachmentRequiredFlag,
            Boolean autoCloseAllowedFlag,
            Boolean approvalAllowedFlag,
            Boolean actionAllowedFlag,
            Boolean viewAllowedFlag,
            Boolean temporaryOnlyFlag,
            Boolean finalActionFlag,
            String description,
            Boolean active
    ) {
    }

    public record MasterViewDto(
            UUID id,
            String tenantId,
            String code,
            String name,
            String roleType,
            String moduleCode,
            String actionType,
            String scopeType,
            UUID roleId,
            UUID permissionId,
            Boolean allowFlag,
            String dataScopeOverride,
            String moduleName,
            String initiationChannel,
            Boolean approvalRequiredFlag,
            UUID workflowTypeId,
            String matrixName,
            UUID legalEntityId,
            UUID branchId,
            UUID departmentId,
            UUID employeeCategoryId,
            UUID workerTypeId,
            UUID serviceRequestTypeId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer levelNo,
            String approverSourceType,
            UUID approverRoleId,
            String approverUserRef,
            UUID approvalActionTypeId,
            Integer escalationDays,
            Boolean delegationAllowedFlag,
            String eventCode,
            String channelType,
            String subjectTemplate,
            String bodyTemplate,
            String languageCode,
            String category,
            Boolean attachmentRequiredFlag,
            Boolean autoCloseAllowedFlag,
            Boolean approvalAllowedFlag,
            Boolean actionAllowedFlag,
            Boolean viewAllowedFlag,
            Boolean temporaryOnlyFlag,
            Boolean finalActionFlag,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }
}
