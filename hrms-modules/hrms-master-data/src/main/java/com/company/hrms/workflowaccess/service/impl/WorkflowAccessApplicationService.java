package com.company.hrms.workflowaccess.service.impl;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import com.company.hrms.workflowaccess.repository.WorkflowAccessRepository;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WorkflowAccessApplicationService implements com.company.hrms.workflowaccess.service.WorkflowAccessModuleApi {

    private static final String ACTOR = "system";

    private final WorkflowAccessRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public WorkflowAccessApplicationService(
            WorkflowAccessRepository repository,
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
    public Mono<WorkflowAccessModels.MasterViewDto> create(WorkflowAccessModels.Resource resource, WorkflowAccessModels.MasterUpsertRequest request) {
        return withTenant(resource)
                .flatMap(tenantId -> validate(tenantId, resource, request)
                        .then(requireCode(resource, request.code()))
                        .then(codeExists(tenantId, resource, request, null))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.create(tenantId, resource, normalize(request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "WORKFLOW_ACCESS_CREATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> update(
            WorkflowAccessModels.Resource resource,
            UUID id,
            WorkflowAccessModels.MasterUpsertRequest request
    ) {
        return withTenant(resource)
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(validate(tenantId, resource, request))
                        .then(requireCode(resource, request.code()))
                        .then(codeExists(tenantId, resource, request, id))
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "CODE_EXISTS", "Code already exists"))
                                : repository.update(tenantId, resource, id, normalize(request), ACTOR))
                        .onErrorMap(DataIntegrityViolationException.class,
                                ex -> new HrmsException(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION", "Data constraint violation"))
                        .flatMap(saved -> publishAudit(tenantId, "WORKFLOW_ACCESS_UPDATED", resource.path(), saved.id(), Map.of("code", saved.code()))
                                .thenReturn(saved)));
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> get(WorkflowAccessModels.Resource resource, UUID id) {
        return withTenant(resource).flatMap(tenantId -> repository.get(tenantId, resource, id).switchIfEmpty(notFound(resource)));
    }

    @Override
    public Mono<PagedResult<WorkflowAccessModels.MasterViewDto>> list(
            WorkflowAccessModels.Resource resource,
            WorkflowAccessModels.SearchQuery query
    ) {
        WorkflowAccessModels.SearchQuery normalized = normalizeQuery(query);
        return withTenant(resource).flatMap(tenantId -> repository.list(tenantId, resource, normalized)
                .collectList()
                .zipWith(repository.count(tenantId, resource, normalized))
                .map(tuple -> {
                    int page = normalized.limit() == 0 ? 0 : normalized.offset() / normalized.limit();
                    int totalPages = tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / normalized.limit());
                    return new PagedResult<>(
                            tuple.getT1(),
                            page,
                            normalized.limit(),
                            tuple.getT2(),
                            totalPages);
                }));
    }

    @Override
    public Mono<WorkflowAccessModels.MasterViewDto> updateStatus(
            WorkflowAccessModels.Resource resource,
            UUID id,
            WorkflowAccessModels.StatusUpdateCommand command
    ) {
        return withTenant(resource)
                .flatMap(tenantId -> repository.get(tenantId, resource, id)
                        .switchIfEmpty(notFound(resource))
                        .then(repository.updateStatus(tenantId, resource, id, command.active(), ACTOR))
                        .flatMap(saved -> publishAudit(
                                        tenantId,
                                        "WORKFLOW_ACCESS_STATUS_UPDATED",
                                        resource.path(),
                                        saved.id(),
                                        Map.of("active", command.active()))
                                .thenReturn(saved)));
    }

    @Override
    public Flux<WorkflowAccessModels.OptionViewDto> options(WorkflowAccessModels.Resource resource, String q, int limit, boolean activeOnly) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return withTenant(resource).flatMapMany(tenantId -> repository.options(tenantId, resource, q, safeLimit, activeOnly));
    }

    private Mono<Void> validate(String tenantId, WorkflowAccessModels.Resource resource, WorkflowAccessModels.MasterUpsertRequest request) {
        if (resource != WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS && !StringUtils.hasText(request.name())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "NAME_REQUIRED", "Name is required"));
        }
        return switch (resource) {
            case ROLES -> validateRole(request);
            case PERMISSIONS -> validatePermission(request);
            case ROLE_PERMISSION_MAPPINGS -> validateRolePermissionMapping(tenantId, request);
            case WORKFLOW_TYPES -> validateWorkflowType(request);
            case APPROVAL_MATRICES -> validateApprovalMatrix(tenantId, request);
            case NOTIFICATION_TEMPLATES -> validateNotificationTemplate(request);
            case SERVICE_REQUEST_TYPES -> validateServiceRequestType(tenantId, request);
            case DELEGATION_TYPES -> validateDelegationType(request);
            case APPROVAL_ACTION_TYPES -> Mono.empty();
        };
    }

    private Mono<Void> validateRole(WorkflowAccessModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.roleType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ROLE_TYPE_REQUIRED", "Role type is required"));
        }
        return validateEnum(request.roleType(), WorkflowAccessModels.RoleType.class, "INVALID_ROLE_TYPE");
    }

    private Mono<Void> validatePermission(WorkflowAccessModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.moduleCode())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "MODULE_CODE_REQUIRED", "Module code is required"));
        }
        if (!StringUtils.hasText(request.actionType()) || !StringUtils.hasText(request.scopeType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ACTION_SCOPE_REQUIRED", "Action type and scope type are required"));
        }
        return validateEnum(request.actionType(), WorkflowAccessModels.PermissionActionType.class, "INVALID_ACTION_TYPE")
                .then(validateEnum(request.scopeType(), WorkflowAccessModels.ScopeType.class, "INVALID_SCOPE_TYPE"));
    }

    private Mono<Void> validateRolePermissionMapping(String tenantId, WorkflowAccessModels.MasterUpsertRequest request) {
        if (request.roleId() == null || request.permissionId() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ROLE_PERMISSION_REQUIRED", "roleId and permissionId are required"));
        }
        return Mono.when(
                requireExists(tenantId, "master_data.roles", request.roleId(), true, "ROLE_NOT_FOUND"),
                requireExists(tenantId, "master_data.permissions", request.permissionId(), false, "PERMISSION_NOT_FOUND"))
                .then(validateOptionalScopeOverride(request.dataScopeOverride()));
    }

    private Mono<Void> validateOptionalScopeOverride(String value) {
        if (!StringUtils.hasText(value)) {
            return Mono.empty();
        }
        return validateEnum(value, WorkflowAccessModels.ScopeType.class, "INVALID_DATA_SCOPE_OVERRIDE");
    }

    private Mono<Void> validateWorkflowType(WorkflowAccessModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.initiationChannel())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INITIATION_CHANNEL_REQUIRED", "Initiation channel is required"));
        }
        return validateEnum(request.initiationChannel(), WorkflowAccessModels.InitiationChannel.class, "INVALID_INITIATION_CHANNEL");
    }

    private Mono<Void> validateApprovalMatrix(String tenantId, WorkflowAccessModels.MasterUpsertRequest request) {
        if (request.workflowTypeId() == null || request.levelNo() == null || request.levelNo() < 1) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_LEVEL_OR_WORKFLOW", "workflowTypeId and levelNo >= 1 are required"));
        }
        if (!StringUtils.hasText(request.approverSourceType())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "APPROVER_SOURCE_REQUIRED", "Approver source type is required"));
        }
        if (request.escalationDays() != null && request.escalationDays() < 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_ESCALATION_DAYS", "Escalation days must be >= 0"));
        }
        if (request.minAmount() != null && request.maxAmount() != null && request.minAmount().compareTo(request.maxAmount()) > 0) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT_RANGE", "minAmount cannot exceed maxAmount"));
        }
        return validateEnum(request.approverSourceType(), WorkflowAccessModels.ApproverSourceType.class, "INVALID_APPROVER_SOURCE")
                .then(validateApproverSourceRules(request))
                .then(Mono.when(
                        requireExists(tenantId, "master_data.workflow_types", request.workflowTypeId(), false, "WORKFLOW_TYPE_NOT_FOUND"),
                        optionalExists(tenantId, "organization.legal_entities", request.legalEntityId(), true, "LEGAL_ENTITY_NOT_FOUND"),
                        optionalExists(tenantId, "organization.branches", request.branchId(), true, "BRANCH_NOT_FOUND"),
                        optionalExists(tenantId, "organization.departments", request.departmentId(), true, "DEPARTMENT_NOT_FOUND"),
                        optionalExists(tenantId, "job_architecture.employee_categories", request.employeeCategoryId(), true, "EMPLOYEE_CATEGORY_NOT_FOUND"),
                        optionalExists(tenantId, "job_architecture.worker_types", request.workerTypeId(), true, "WORKER_TYPE_NOT_FOUND"),
                        optionalExists(tenantId, "master_data.service_request_types", request.serviceRequestTypeId(), true, "SERVICE_REQUEST_TYPE_NOT_FOUND"),
                        optionalExists(tenantId, "master_data.roles", request.approverRoleId(), true, "APPROVER_ROLE_NOT_FOUND"),
                        optionalExists(tenantId, "master_data.approval_action_types", request.approvalActionTypeId(), false, "APPROVAL_ACTION_TYPE_NOT_FOUND")));
    }

    private Mono<Void> validateApproverSourceRules(WorkflowAccessModels.MasterUpsertRequest request) {
        String source = request.approverSourceType().trim().toUpperCase();
        if ("ROLE".equals(source) && request.approverRoleId() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "APPROVER_ROLE_REQUIRED", "approverRoleId is required when approverSourceType=ROLE"));
        }
        if ("USER".equals(source) && !StringUtils.hasText(request.approverUserRef())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "APPROVER_USER_REQUIRED", "approverUserRef is required when approverSourceType=USER"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateNotificationTemplate(WorkflowAccessModels.MasterUpsertRequest request) {
        if (!StringUtils.hasText(request.eventCode()) || !StringUtils.hasText(request.channelType()) || !StringUtils.hasText(request.bodyTemplate())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TEMPLATE_FIELDS_REQUIRED", "eventCode, channelType and bodyTemplate are required"));
        }
        return validateEnum(request.channelType(), WorkflowAccessModels.ChannelType.class, "INVALID_CHANNEL_TYPE")
                .then(Mono.defer(() -> {
                    if ("EMAIL".equalsIgnoreCase(request.channelType()) && !StringUtils.hasText(request.subjectTemplate())) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SUBJECT_REQUIRED", "subjectTemplate is required for EMAIL channel"));
                    }
                    return Mono.empty();
                }));
    }

    private Mono<Void> validateServiceRequestType(String tenantId, WorkflowAccessModels.MasterUpsertRequest request) {
        if (request.workflowTypeId() == null) {
            return Mono.empty();
        }
        return requireExists(tenantId, "master_data.workflow_types", request.workflowTypeId(), false, "WORKFLOW_TYPE_NOT_FOUND");
    }

    private Mono<Void> validateDelegationType(WorkflowAccessModels.MasterUpsertRequest request) {
        if (Boolean.FALSE.equals(request.approvalAllowedFlag())
                && Boolean.FALSE.equals(request.actionAllowedFlag())
                && Boolean.FALSE.equals(request.viewAllowedFlag())) {
            return Mono.error(new HrmsException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DELEGATION_FLAGS",
                    "At least one of approvalAllowedFlag/actionAllowedFlag/viewAllowedFlag must be true"));
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

    private Mono<Void> requireCode(WorkflowAccessModels.Resource resource, String code) {
        if (resource == WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS) {
            return Mono.empty();
        }
        if (!StringUtils.hasText(code)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "CODE_REQUIRED", "Code is required"));
        }
        return Mono.empty();
    }

    private Mono<Boolean> codeExists(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            WorkflowAccessModels.MasterUpsertRequest request,
            UUID excludeId
    ) {
        if (resource == WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS) {
            return Mono.just(false);
        }
        return repository.codeExists(tenantId, resource, request.code().trim(), excludeId);
    }

    private Mono<Void> requireExists(String tenantId, String tableName, UUID id, boolean tenantOwned, String errorCode) {
        return repository.existsById(tenantId, tableName, id, tenantOwned)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, errorCode, errorCode.replace('_', ' ').toLowerCase())));
    }

    private Mono<Void> optionalExists(String tenantId, String tableName, UUID id, boolean tenantOwned, String errorCode) {
        if (id == null) {
            return Mono.empty();
        }
        return requireExists(tenantId, tableName, id, tenantOwned, errorCode);
    }

    private WorkflowAccessModels.MasterUpsertRequest normalize(WorkflowAccessModels.MasterUpsertRequest request) {
        return new WorkflowAccessModels.MasterUpsertRequest(
                trimToNull(request.code()),
                trimToNull(request.name()),
                uppercaseOrNull(request.roleType()),
                trimToNull(request.moduleCode()),
                uppercaseOrNull(request.actionType()),
                uppercaseOrNull(request.scopeType()),
                request.roleId(),
                request.permissionId(),
                request.allowFlag(),
                uppercaseOrNull(request.dataScopeOverride()),
                trimToNull(request.moduleName()),
                uppercaseOrNull(request.initiationChannel()),
                request.approvalRequiredFlag(),
                request.workflowTypeId(),
                trimToNull(request.matrixName()),
                request.legalEntityId(),
                request.branchId(),
                request.departmentId(),
                request.employeeCategoryId(),
                request.workerTypeId(),
                request.serviceRequestTypeId(),
                request.minAmount(),
                request.maxAmount(),
                request.levelNo(),
                uppercaseOrNull(request.approverSourceType()),
                request.approverRoleId(),
                trimToNull(request.approverUserRef()),
                request.approvalActionTypeId(),
                request.escalationDays(),
                request.delegationAllowedFlag(),
                trimToNull(request.eventCode()),
                uppercaseOrNull(request.channelType()),
                trimToNull(request.subjectTemplate()),
                trimToNull(request.bodyTemplate()),
                trimToNull(request.languageCode()),
                trimToNull(request.category()),
                request.attachmentRequiredFlag(),
                request.autoCloseAllowedFlag(),
                request.approvalAllowedFlag(),
                request.actionAllowedFlag(),
                request.viewAllowedFlag(),
                request.temporaryOnlyFlag(),
                request.finalActionFlag(),
                trimToNull(request.description()),
                request.active());
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String uppercaseOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private Mono<String> withTenant(WorkflowAccessModels.Resource resource) {
        return enablementGuard.requireModuleEnabled("master-data")
                .then(resource.tenantOwned()
                        ? tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        : Mono.just("platform"));
    }

    private WorkflowAccessModels.SearchQuery normalizeQuery(WorkflowAccessModels.SearchQuery query) {
        if (query == null) {
            return new WorkflowAccessModels.SearchQuery(
                    null, null, 50, 0, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null);
        }
        return new WorkflowAccessModels.SearchQuery(
                query.q(),
                query.active(),
                Math.min(Math.max(query.limit(), 1), 500),
                Math.max(0, query.offset()),
                query.sort(),
                query.roleType(),
                query.moduleCode(),
                query.actionType(),
                query.scopeType(),
                query.roleId(),
                query.permissionId(),
                query.allowFlag(),
                query.dataScopeOverride(),
                query.moduleName(),
                query.initiationChannel(),
                query.approvalRequiredFlag(),
                query.workflowTypeId(),
                query.legalEntityId(),
                query.branchId(),
                query.departmentId(),
                query.employeeCategoryId(),
                query.workerTypeId(),
                query.serviceRequestTypeId(),
                query.approverSourceType(),
                query.levelNo(),
                query.delegationAllowedFlag(),
                query.eventCode(),
                query.channelType(),
                query.languageCode(),
                query.category(),
                query.attachmentRequiredFlag(),
                query.autoCloseAllowedFlag(),
                query.approvalAllowedFlag(),
                query.actionAllowedFlag(),
                query.viewAllowedFlag(),
                query.temporaryOnlyFlag(),
                query.finalActionFlag());
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, UUID targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(ACTOR, tenantId, action, targetType, targetId.toString(), metadata));
    }

    private <T> Mono<T> notFound(WorkflowAccessModels.Resource resource) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "NOT_FOUND", resource.path() + " record not found"));
    }
}
