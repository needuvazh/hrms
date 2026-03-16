package com.company.hrms.workflowaccess.repository;

import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
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
public class WorkflowAccessR2dbcRepository implements WorkflowAccessRepository {

    private final DatabaseClient databaseClient;

    public WorkflowAccessR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> create(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            WorkflowAccessModels.MasterUpsertRequest request,
            String actor
    ) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec spec = databaseClient.sql(insertSql(resource))
                .bind("id", id)
                .bind("code", request.code().trim())
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> update(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            UUID id,
            WorkflowAccessModels.MasterUpsertRequest request,
            String actor
    ) {
        GenericExecuteSpec spec = databaseClient.sql(updateSql(resource))
                .bind("id", id)
                .bind("code", request.code().trim())
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> get(String tenantId, WorkflowAccessModels.Resource resource, UUID id) {
        String sql = baseSelect(resource)
                + (resource.tenantOwned() ? " WHERE t.tenant_id = :tenantId AND t.id = :id" : " WHERE t.id = :id");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("id", id);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Flux<WorkflowAccessModels.MasterViewDto> list(String tenantId, WorkflowAccessModels.Resource resource, WorkflowAccessModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder(baseSelect(resource)).append(" WHERE 1=1");
        if (resource.tenantOwned()) {
            sql.append(" AND t.tenant_id = :tenantId");
        }
        String like = "%";
        if (StringUtils.hasText(query.q())) {
            like = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(cast(t.").append(resource.codeColumn()).append(" as text)) LIKE lower(:q) OR lower(cast(t.")
                    .append(resource.nameColumn()).append(" as text)) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        applyResourceFilters(sql, resource, query);
        sql.append(" ORDER BY ").append(resolveSort(resource, query.sort())).append(" LIMIT :limit OFFSET :offset");

        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("limit", query.limit())
                .bind("offset", query.offset());
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", like);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        spec = bindResourceFilters(spec, resource, query);
        return spec.map((row, metadata) -> mapRow(row, resource)).all();
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> updateStatus(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    ) {
        String sql = "UPDATE " + resource.table() + " SET active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :updatedBy WHERE id = :id"
                + (resource.tenantOwned() ? " AND tenant_id = :tenantId" : "")
                + " RETURNING *";
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("active", active)
                .bind("updatedBy", actor)
                .bind("id", id);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Flux<WorkflowAccessModels.OptionViewDto> options(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            String q,
            int limit,
            boolean activeOnly
    ) {
        String sql;
        if (resource == WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS) {
            sql = "SELECT id, cast(id as text) AS code, 'Role-Permission Mapping' AS name FROM " + resource.table() + " t WHERE 1=1"
                    + (resource.tenantOwned() ? " AND t.tenant_id = :tenantId" : "")
                    + (activeOnly ? " AND t.active = TRUE" : "")
                    + " ORDER BY t.updated_at DESC LIMIT :limit";
        } else {
            sql = "SELECT id, cast(" + resource.codeColumn() + " as text) AS code, cast(" + resource.nameColumn() + " as text) AS name FROM "
                    + resource.table()
                    + " t WHERE 1=1"
                    + (resource.tenantOwned() ? " AND t.tenant_id = :tenantId" : "")
                    + (activeOnly ? " AND t.active = TRUE" : "")
                    + (StringUtils.hasText(q)
                            ? " AND (lower(cast(t." + resource.codeColumn() + " as text)) LIKE lower(:q) OR lower(cast(t." + resource.nameColumn() + " as text)) LIKE lower(:q))"
                            : "")
                    + " ORDER BY t." + resource.nameColumn() + " ASC LIMIT :limit";
        }
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("limit", limit);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        if (StringUtils.hasText(q) && resource != WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS) {
            spec = spec.bind("q", "%" + q.trim() + "%");
        }
        return spec.map((row, metadata) -> new WorkflowAccessModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> codeExists(String tenantId, WorkflowAccessModels.Resource resource, String code, UUID excludeId) {
        String sql = "SELECT count(*) AS cnt FROM " + resource.table() + " WHERE lower(cast(" + resource.codeColumn() + " as text)) = lower(:code)"
                + (resource.tenantOwned() ? " AND tenant_id = :tenantId" : "")
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("code", code);
        if (resource.tenantOwned()) {
            spec = spec.bind("tenantId", tenantId);
        }
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Boolean> existsById(String tenantId, String tableName, UUID id, boolean tenantOwned) {
        if (id == null) {
            return Mono.just(false);
        }
        String sql = "SELECT count(*) AS cnt FROM " + tableName + " WHERE id = :id" + (tenantOwned ? " AND tenant_id = :tenantId" : "");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("id", id);
        if (tenantOwned) {
            spec = spec.bind("tenantId", tenantId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    private String baseSelect(WorkflowAccessModels.Resource resource) {
        return "SELECT t.* FROM " + resource.table() + " t";
    }

    private String resolveSort(WorkflowAccessModels.Resource resource, String sort) {
        String fallback = "t.updated_at DESC";
        if (!StringUtils.hasText(sort)) {
            return fallback;
        }
        String[] parts = sort.split(",");
        String column = parts[0].trim();
        String direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) ? "ASC" : "DESC";
        if (!resource.sortableColumns().contains(column)) {
            return fallback;
        }
        return "t." + column + " " + direction;
    }

    private void applyResourceFilters(StringBuilder sql, WorkflowAccessModels.Resource resource, WorkflowAccessModels.SearchQuery query) {
        switch (resource) {
            case ROLES -> {
                if (StringUtils.hasText(query.roleType())) {
                    sql.append(" AND t.role_type = :roleType");
                }
            }
            case PERMISSIONS -> {
                if (StringUtils.hasText(query.moduleCode())) {
                    sql.append(" AND lower(t.module_code) = lower(:moduleCode)");
                }
                if (StringUtils.hasText(query.actionType())) {
                    sql.append(" AND t.action_type = :actionType");
                }
                if (StringUtils.hasText(query.scopeType())) {
                    sql.append(" AND t.scope_type = :scopeType");
                }
            }
            case ROLE_PERMISSION_MAPPINGS -> {
                if (query.roleId() != null) {
                    sql.append(" AND t.role_id = :roleId");
                }
                if (query.permissionId() != null) {
                    sql.append(" AND t.permission_id = :permissionId");
                }
                if (query.allowFlag() != null) {
                    sql.append(" AND t.allow_flag = :allowFlag");
                }
                if (StringUtils.hasText(query.dataScopeOverride())) {
                    sql.append(" AND t.data_scope_override = :dataScopeOverride");
                }
            }
            case WORKFLOW_TYPES -> {
                if (StringUtils.hasText(query.moduleName())) {
                    sql.append(" AND lower(t.module_name) = lower(:moduleName)");
                }
                if (StringUtils.hasText(query.initiationChannel())) {
                    sql.append(" AND t.initiation_channel = :initiationChannel");
                }
                if (query.approvalRequiredFlag() != null) {
                    sql.append(" AND t.approval_required_flag = :approvalRequiredFlag");
                }
            }
            case APPROVAL_MATRICES -> {
                if (query.workflowTypeId() != null) {
                    sql.append(" AND t.workflow_type_id = :workflowTypeId");
                }
                if (query.legalEntityId() != null) {
                    sql.append(" AND t.legal_entity_id = :legalEntityId");
                }
                if (query.branchId() != null) {
                    sql.append(" AND t.branch_id = :branchId");
                }
                if (query.departmentId() != null) {
                    sql.append(" AND t.department_id = :departmentId");
                }
                if (query.employeeCategoryId() != null) {
                    sql.append(" AND t.employee_category_id = :employeeCategoryId");
                }
                if (query.workerTypeId() != null) {
                    sql.append(" AND t.worker_type_id = :workerTypeId");
                }
                if (query.serviceRequestTypeId() != null) {
                    sql.append(" AND t.service_request_type_id = :serviceRequestTypeId");
                }
                if (StringUtils.hasText(query.approverSourceType())) {
                    sql.append(" AND t.approver_source_type = :approverSourceType");
                }
                if (query.levelNo() != null) {
                    sql.append(" AND t.level_no = :levelNo");
                }
                if (query.delegationAllowedFlag() != null) {
                    sql.append(" AND t.delegation_allowed_flag = :delegationAllowedFlag");
                }
            }
            case NOTIFICATION_TEMPLATES -> {
                if (StringUtils.hasText(query.eventCode())) {
                    sql.append(" AND lower(t.event_code) = lower(:eventCode)");
                }
                if (StringUtils.hasText(query.channelType())) {
                    sql.append(" AND t.channel_type = :channelType");
                }
                if (StringUtils.hasText(query.languageCode())) {
                    sql.append(" AND lower(t.language_code) = lower(:languageCode)");
                }
            }
            case SERVICE_REQUEST_TYPES -> {
                if (StringUtils.hasText(query.category())) {
                    sql.append(" AND lower(t.category) = lower(:category)");
                }
                if (query.workflowTypeId() != null) {
                    sql.append(" AND t.workflow_type_id = :workflowTypeId");
                }
                if (query.attachmentRequiredFlag() != null) {
                    sql.append(" AND t.attachment_required_flag = :attachmentRequiredFlag");
                }
                if (query.autoCloseAllowedFlag() != null) {
                    sql.append(" AND t.auto_close_allowed_flag = :autoCloseAllowedFlag");
                }
            }
            case DELEGATION_TYPES -> {
                if (query.approvalAllowedFlag() != null) {
                    sql.append(" AND t.approval_allowed_flag = :approvalAllowedFlag");
                }
                if (query.actionAllowedFlag() != null) {
                    sql.append(" AND t.action_allowed_flag = :actionAllowedFlag");
                }
                if (query.viewAllowedFlag() != null) {
                    sql.append(" AND t.view_allowed_flag = :viewAllowedFlag");
                }
                if (query.temporaryOnlyFlag() != null) {
                    sql.append(" AND t.temporary_only_flag = :temporaryOnlyFlag");
                }
            }
            case APPROVAL_ACTION_TYPES -> {
                if (query.finalActionFlag() != null) {
                    sql.append(" AND t.final_action_flag = :finalActionFlag");
                }
            }
        }
    }

    private GenericExecuteSpec bindResourceFilters(GenericExecuteSpec spec, WorkflowAccessModels.Resource resource, WorkflowAccessModels.SearchQuery query) {
        switch (resource) {
            case ROLES -> {
                if (StringUtils.hasText(query.roleType())) spec = spec.bind("roleType", query.roleType().trim().toUpperCase());
            }
            case PERMISSIONS -> {
                if (StringUtils.hasText(query.moduleCode())) spec = spec.bind("moduleCode", query.moduleCode().trim());
                if (StringUtils.hasText(query.actionType())) spec = spec.bind("actionType", query.actionType().trim().toUpperCase());
                if (StringUtils.hasText(query.scopeType())) spec = spec.bind("scopeType", query.scopeType().trim().toUpperCase());
            }
            case ROLE_PERMISSION_MAPPINGS -> {
                if (query.roleId() != null) spec = spec.bind("roleId", query.roleId());
                if (query.permissionId() != null) spec = spec.bind("permissionId", query.permissionId());
                if (query.allowFlag() != null) spec = spec.bind("allowFlag", query.allowFlag());
                if (StringUtils.hasText(query.dataScopeOverride())) spec = spec.bind("dataScopeOverride", query.dataScopeOverride().trim().toUpperCase());
            }
            case WORKFLOW_TYPES -> {
                if (StringUtils.hasText(query.moduleName())) spec = spec.bind("moduleName", query.moduleName().trim());
                if (StringUtils.hasText(query.initiationChannel())) spec = spec.bind("initiationChannel", query.initiationChannel().trim().toUpperCase());
                if (query.approvalRequiredFlag() != null) spec = spec.bind("approvalRequiredFlag", query.approvalRequiredFlag());
            }
            case APPROVAL_MATRICES -> {
                if (query.workflowTypeId() != null) spec = spec.bind("workflowTypeId", query.workflowTypeId());
                if (query.legalEntityId() != null) spec = spec.bind("legalEntityId", query.legalEntityId());
                if (query.branchId() != null) spec = spec.bind("branchId", query.branchId());
                if (query.departmentId() != null) spec = spec.bind("departmentId", query.departmentId());
                if (query.employeeCategoryId() != null) spec = spec.bind("employeeCategoryId", query.employeeCategoryId());
                if (query.workerTypeId() != null) spec = spec.bind("workerTypeId", query.workerTypeId());
                if (query.serviceRequestTypeId() != null) spec = spec.bind("serviceRequestTypeId", query.serviceRequestTypeId());
                if (StringUtils.hasText(query.approverSourceType())) spec = spec.bind("approverSourceType", query.approverSourceType().trim().toUpperCase());
                if (query.levelNo() != null) spec = spec.bind("levelNo", query.levelNo());
                if (query.delegationAllowedFlag() != null) spec = spec.bind("delegationAllowedFlag", query.delegationAllowedFlag());
            }
            case NOTIFICATION_TEMPLATES -> {
                if (StringUtils.hasText(query.eventCode())) spec = spec.bind("eventCode", query.eventCode().trim());
                if (StringUtils.hasText(query.channelType())) spec = spec.bind("channelType", query.channelType().trim().toUpperCase());
                if (StringUtils.hasText(query.languageCode())) spec = spec.bind("languageCode", query.languageCode().trim());
            }
            case SERVICE_REQUEST_TYPES -> {
                if (StringUtils.hasText(query.category())) spec = spec.bind("category", query.category().trim());
                if (query.workflowTypeId() != null) spec = spec.bind("workflowTypeId", query.workflowTypeId());
                if (query.attachmentRequiredFlag() != null) spec = spec.bind("attachmentRequiredFlag", query.attachmentRequiredFlag());
                if (query.autoCloseAllowedFlag() != null) spec = spec.bind("autoCloseAllowedFlag", query.autoCloseAllowedFlag());
            }
            case DELEGATION_TYPES -> {
                if (query.approvalAllowedFlag() != null) spec = spec.bind("approvalAllowedFlag", query.approvalAllowedFlag());
                if (query.actionAllowedFlag() != null) spec = spec.bind("actionAllowedFlag", query.actionAllowedFlag());
                if (query.viewAllowedFlag() != null) spec = spec.bind("viewAllowedFlag", query.viewAllowedFlag());
                if (query.temporaryOnlyFlag() != null) spec = spec.bind("temporaryOnlyFlag", query.temporaryOnlyFlag());
            }
            case APPROVAL_ACTION_TYPES -> {
                if (query.finalActionFlag() != null) spec = spec.bind("finalActionFlag", query.finalActionFlag());
            }
        }
        return spec;
    }

    private String insertSql(WorkflowAccessModels.Resource resource) {
        return switch (resource) {
            case ROLES -> """
                    INSERT INTO master_data.roles(id, tenant_id, role_code, role_name, role_type, description, active, created_by, updated_by)
                    VALUES (:id, :tenantId, :code, :name, :roleType, :description, :active, :createdBy, :updatedBy) RETURNING *
                    """;
            case PERMISSIONS -> """
                    INSERT INTO master_data.permissions(id, permission_code, permission_name, module_code, action_type, scope_type, description, active, created_by, updated_by)
                    VALUES (:id, :code, :name, :moduleCode, :actionType, :scopeType, :description, :active, :createdBy, :updatedBy) RETURNING *
                    """;
            case ROLE_PERMISSION_MAPPINGS -> """
                    INSERT INTO master_data.role_permission_mappings(
                        id, tenant_id, role_id, permission_id, allow_flag, data_scope_override, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :roleId, :permissionId, :allowFlag, :dataScopeOverride, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case WORKFLOW_TYPES -> """
                    INSERT INTO master_data.workflow_types(
                        id, workflow_type_code, workflow_type_name, module_name, initiation_channel, approval_required_flag,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :moduleName, :initiationChannel, :approvalRequiredFlag,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case APPROVAL_MATRICES -> """
                    INSERT INTO master_data.approval_matrices(
                        id, tenant_id, approval_matrix_code, workflow_type_id, matrix_name, legal_entity_id, branch_id,
                        department_id, employee_category_id, worker_type_id, service_request_type_id, min_amount,
                        max_amount, level_no, approver_source_type, approver_role_id, approver_user_ref,
                        approval_action_type_id, escalation_days, delegation_allowed_flag, description,
                        active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :workflowTypeId, :matrixName, :legalEntityId, :branchId,
                        :departmentId, :employeeCategoryId, :workerTypeId, :serviceRequestTypeId, :minAmount,
                        :maxAmount, :levelNo, :approverSourceType, :approverRoleId, :approverUserRef,
                        :approvalActionTypeId, :escalationDays, :delegationAllowedFlag, :description,
                        :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case NOTIFICATION_TEMPLATES -> """
                    INSERT INTO master_data.notification_templates(
                        id, tenant_id, template_code, template_name, event_code, channel_type, subject_template,
                        body_template, language_code, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :eventCode, :channelType, :subjectTemplate,
                        :bodyTemplate, :languageCode, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SERVICE_REQUEST_TYPES -> """
                    INSERT INTO master_data.service_request_types(
                        id, tenant_id, service_request_type_code, service_request_type_name, category, workflow_type_id,
                        attachment_required_flag, auto_close_allowed_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :category, :workflowTypeId,
                        :attachmentRequiredFlag, :autoCloseAllowedFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DELEGATION_TYPES -> """
                    INSERT INTO master_data.delegation_types(
                        id, delegation_type_code, delegation_type_name, approval_allowed_flag, action_allowed_flag,
                        view_allowed_flag, temporary_only_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :approvalAllowedFlag, :actionAllowedFlag,
                        :viewAllowedFlag, :temporaryOnlyFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case APPROVAL_ACTION_TYPES -> """
                    INSERT INTO master_data.approval_action_types(
                        id, approval_action_type_code, approval_action_type_name, final_action_flag,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :finalActionFlag,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
        };
    }

    private String updateSql(WorkflowAccessModels.Resource resource) {
        String tenantWhere = " AND tenant_id = :tenantId";
        return switch (resource) {
            case ROLES -> """
                    UPDATE master_data.roles SET
                        role_code = :code,
                        role_name = :name,
                        role_type = :roleType,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id AND tenant_id = :tenantId RETURNING *
                    """;
            case PERMISSIONS -> """
                    UPDATE master_data.permissions SET
                        permission_code = :code,
                        permission_name = :name,
                        module_code = :moduleCode,
                        action_type = :actionType,
                        scope_type = :scopeType,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case ROLE_PERMISSION_MAPPINGS -> """
                    UPDATE master_data.role_permission_mappings SET
                        role_id = :roleId,
                        permission_id = :permissionId,
                        allow_flag = :allowFlag,
                        data_scope_override = :dataScopeOverride,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id AND tenant_id = :tenantId RETURNING *
                    """;
            case WORKFLOW_TYPES -> """
                    UPDATE master_data.workflow_types SET
                        workflow_type_code = :code,
                        workflow_type_name = :name,
                        module_name = :moduleName,
                        initiation_channel = :initiationChannel,
                        approval_required_flag = :approvalRequiredFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case APPROVAL_MATRICES -> """
                    UPDATE master_data.approval_matrices SET
                        approval_matrix_code = :code,
                        workflow_type_id = :workflowTypeId,
                        matrix_name = :matrixName,
                        legal_entity_id = :legalEntityId,
                        branch_id = :branchId,
                        department_id = :departmentId,
                        employee_category_id = :employeeCategoryId,
                        worker_type_id = :workerTypeId,
                        service_request_type_id = :serviceRequestTypeId,
                        min_amount = :minAmount,
                        max_amount = :maxAmount,
                        level_no = :levelNo,
                        approver_source_type = :approverSourceType,
                        approver_role_id = :approverRoleId,
                        approver_user_ref = :approverUserRef,
                        approval_action_type_id = :approvalActionTypeId,
                        escalation_days = :escalationDays,
                        delegation_allowed_flag = :delegationAllowedFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id AND tenant_id = :tenantId RETURNING *
                    """;
            case NOTIFICATION_TEMPLATES -> """
                    UPDATE master_data.notification_templates SET
                        template_code = :code,
                        template_name = :name,
                        event_code = :eventCode,
                        channel_type = :channelType,
                        subject_template = :subjectTemplate,
                        body_template = :bodyTemplate,
                        language_code = :languageCode,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id AND tenant_id = :tenantId RETURNING *
                    """;
            case SERVICE_REQUEST_TYPES -> """
                    UPDATE master_data.service_request_types SET
                        service_request_type_code = :code,
                        service_request_type_name = :name,
                        category = :category,
                        workflow_type_id = :workflowTypeId,
                        attachment_required_flag = :attachmentRequiredFlag,
                        auto_close_allowed_flag = :autoCloseAllowedFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id AND tenant_id = :tenantId RETURNING *
                    """;
            case DELEGATION_TYPES -> """
                    UPDATE master_data.delegation_types SET
                        delegation_type_code = :code,
                        delegation_type_name = :name,
                        approval_allowed_flag = :approvalAllowedFlag,
                        action_allowed_flag = :actionAllowedFlag,
                        view_allowed_flag = :viewAllowedFlag,
                        temporary_only_flag = :temporaryOnlyFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case APPROVAL_ACTION_TYPES -> """
                    UPDATE master_data.approval_action_types SET
                        approval_action_type_code = :code,
                        approval_action_type_name = :name,
                        final_action_flag = :finalActionFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
        };
    }

    private GenericExecuteSpec bindResourceFields(GenericExecuteSpec spec, WorkflowAccessModels.Resource resource, WorkflowAccessModels.MasterUpsertRequest request) {
        return switch (resource) {
            case ROLES -> bindNullable(
                    bindNullable(spec, "roleType", valueOrNull(request.roleType()), String.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case PERMISSIONS -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "moduleCode", valueOrNull(request.moduleCode()), String.class),
                            "actionType",
                            valueOrNull(request.actionType()),
                            String.class),
                    "scopeType",
                    valueOrNull(request.scopeType()),
                    String.class).bind("description", valueOrNull(request.description()));
            case ROLE_PERMISSION_MAPPINGS -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(spec, "roleId", request.roleId(), UUID.class),
                                    "permissionId",
                                    request.permissionId(),
                                    UUID.class),
                            "allowFlag",
                            request.allowFlag(),
                            Boolean.class),
                    "dataScopeOverride",
                    valueOrNull(request.dataScopeOverride()),
                    String.class).bind("description", valueOrNull(request.description()));
            case WORKFLOW_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "moduleName", valueOrNull(request.moduleName()), String.class),
                            "initiationChannel",
                            valueOrNull(request.initiationChannel()),
                            String.class),
                    "approvalRequiredFlag",
                    request.approvalRequiredFlag(),
                    Boolean.class).bind("description", valueOrNull(request.description()));
            case APPROVAL_MATRICES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(
                                                            bindNullable(
                                                                    bindNullable(
                                                                            bindNullable(
                                                                                    bindNullable(
                                                                                            bindNullable(
                                                                                                    bindNullable(
                                                                                                            bindNullable(
                                                                                                                    bindNullable(spec, "workflowTypeId", request.workflowTypeId(), UUID.class),
                                                                                                                    "matrixName",
                                                                                                                    valueOrNull(request.matrixName()),
                                                                                                                    String.class),
                                                                                                            "legalEntityId",
                                                                                                            request.legalEntityId(),
                                                                                                            UUID.class),
                                                                                                    "branchId",
                                                                                                    request.branchId(),
                                                                                                    UUID.class),
                                                                                            "departmentId",
                                                                                            request.departmentId(),
                                                                                            UUID.class),
                                                                                    "employeeCategoryId",
                                                                                    request.employeeCategoryId(),
                                                                                    UUID.class),
                                                                            "workerTypeId",
                                                                            request.workerTypeId(),
                                                                            UUID.class),
                                                                    "serviceRequestTypeId",
                                                                    request.serviceRequestTypeId(),
                                                                    UUID.class),
                                                            "minAmount",
                                                            request.minAmount(),
                                                            BigDecimal.class),
                                                    "maxAmount",
                                                    request.maxAmount(),
                                                    BigDecimal.class),
                                            "levelNo",
                                            request.levelNo(),
                                            Integer.class),
                                    "approverSourceType",
                                    valueOrNull(request.approverSourceType()),
                                    String.class),
                            "approverRoleId",
                            request.approverRoleId(),
                            UUID.class),
                    "approverUserRef",
                    valueOrNull(request.approverUserRef()),
                    String.class)
                    .bindNull("approvalActionTypeId", UUID.class)
                    .bind("approvalActionTypeId", request.approvalActionTypeId())
                    .bindNull("escalationDays", Integer.class)
                    .bind("escalationDays", request.escalationDays())
                    .bind("delegationAllowedFlag", request.delegationAllowedFlag() != null && request.delegationAllowedFlag())
                    .bind("description", valueOrNull(request.description()));
            case NOTIFICATION_TEMPLATES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(spec, "eventCode", valueOrNull(request.eventCode()), String.class),
                                                    "channelType",
                                                    valueOrNull(request.channelType()),
                                                    String.class),
                                            "subjectTemplate",
                                            valueOrNull(request.subjectTemplate()),
                                            String.class),
                                    "bodyTemplate",
                                    valueOrNull(request.bodyTemplate()),
                                    String.class),
                            "languageCode",
                            valueOrNull(request.languageCode()),
                            String.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case SERVICE_REQUEST_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(spec, "category", valueOrNull(request.category()), String.class),
                                            "workflowTypeId",
                                            request.workflowTypeId(),
                                            UUID.class),
                                    "attachmentRequiredFlag",
                                    request.attachmentRequiredFlag(),
                                    Boolean.class),
                            "autoCloseAllowedFlag",
                            request.autoCloseAllowedFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case DELEGATION_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(spec, "approvalAllowedFlag", request.approvalAllowedFlag(), Boolean.class),
                                            "actionAllowedFlag",
                                            request.actionAllowedFlag(),
                                            Boolean.class),
                                    "viewAllowedFlag",
                                    request.viewAllowedFlag(),
                                    Boolean.class),
                            "temporaryOnlyFlag",
                            request.temporaryOnlyFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case APPROVAL_ACTION_TYPES -> bindNullable(
                    bindNullable(spec, "finalActionFlag", request.finalActionFlag(), Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
        };
    }

    private WorkflowAccessModels.MasterViewDto mapRow(Row row, WorkflowAccessModels.Resource resource) {
        String code;
        String name;
        if (resource == WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS) {
            UUID id = row.get("id", UUID.class);
            code = id == null ? null : id.toString();
            name = "Role-Permission Mapping";
        } else {
            code = get(row, resource.codeColumn(), String.class);
            name = get(row, resource.nameColumn(), String.class);
        }
        return new WorkflowAccessModels.MasterViewDto(
                row.get("id", UUID.class),
                get(row, "tenant_id", String.class),
                code,
                name,
                get(row, "role_type", String.class),
                get(row, "module_code", String.class),
                get(row, "action_type", String.class),
                get(row, "scope_type", String.class),
                get(row, "role_id", UUID.class),
                get(row, "permission_id", UUID.class),
                get(row, "allow_flag", Boolean.class),
                get(row, "data_scope_override", String.class),
                get(row, "module_name", String.class),
                get(row, "initiation_channel", String.class),
                get(row, "approval_required_flag", Boolean.class),
                get(row, "workflow_type_id", UUID.class),
                get(row, "matrix_name", String.class),
                get(row, "legal_entity_id", UUID.class),
                get(row, "branch_id", UUID.class),
                get(row, "department_id", UUID.class),
                get(row, "employee_category_id", UUID.class),
                get(row, "worker_type_id", UUID.class),
                get(row, "service_request_type_id", UUID.class),
                get(row, "min_amount", BigDecimal.class),
                get(row, "max_amount", BigDecimal.class),
                get(row, "level_no", Integer.class),
                get(row, "approver_source_type", String.class),
                get(row, "approver_role_id", UUID.class),
                get(row, "approver_user_ref", String.class),
                get(row, "approval_action_type_id", UUID.class),
                get(row, "escalation_days", Integer.class),
                get(row, "delegation_allowed_flag", Boolean.class),
                get(row, "event_code", String.class),
                get(row, "channel_type", String.class),
                get(row, "subject_template", String.class),
                get(row, "body_template", String.class),
                get(row, "language_code", String.class),
                get(row, "category", String.class),
                get(row, "attachment_required_flag", Boolean.class),
                get(row, "auto_close_allowed_flag", Boolean.class),
                get(row, "approval_allowed_flag", Boolean.class),
                get(row, "action_allowed_flag", Boolean.class),
                get(row, "view_allowed_flag", Boolean.class),
                get(row, "temporary_only_flag", Boolean.class),
                get(row, "final_action_flag", Boolean.class),
                get(row, "description", String.class),
                Boolean.TRUE.equals(get(row, "active", Boolean.class)),
                get(row, "created_at", Instant.class),
                get(row, "updated_at", Instant.class),
                get(row, "created_by", String.class),
                get(row, "updated_by", String.class));
    }

    private <T> T get(Row row, String column, Class<T> type) {
        try {
            return row.get(column, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String valueOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
