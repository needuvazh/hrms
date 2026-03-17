package com.company.hrms.workflowaccess.controller;

import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import com.company.hrms.workflowaccess.service.WorkflowAccessModuleApi;
import com.company.hrms.masterdata.reference.api.PagedResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/workflow-access")
public class WorkflowAccessController {

    private final WorkflowAccessModuleApi moduleApi;

    public WorkflowAccessController(WorkflowAccessModuleApi moduleApi) {
        this.moduleApi = moduleApi;
    }

    @PostMapping("/{resource}")
    public Mono<WorkflowAccessModels.MasterViewDto> create(@PathVariable("resource") String resource, @Valid @RequestBody MasterRequest request) {
        return moduleApi.create(resolve(resource), request.toModel());
    }

    @PutMapping("/{resource}/{id}")
    public Mono<WorkflowAccessModels.MasterViewDto> update(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody MasterRequest request
    ) {
        return moduleApi.update(resolve(resource), id, request.toModel());
    }

    @GetMapping("/{resource}/{id}")
    public Mono<WorkflowAccessModels.MasterViewDto> get(@PathVariable("resource") String resource, @PathVariable("id") UUID id) {
        return moduleApi.get(resolve(resource), id);
    }

    @GetMapping("/{resource}")
    public Mono<PagedResult<WorkflowAccessModels.MasterViewDto>> list(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "roleType", required = false) String roleType,
            @RequestParam(name = "moduleCode", required = false) String moduleCode,
            @RequestParam(name = "actionType", required = false) String actionType,
            @RequestParam(name = "scopeType", required = false) String scopeType,
            @RequestParam(name = "roleId", required = false) UUID roleId,
            @RequestParam(name = "permissionId", required = false) UUID permissionId,
            @RequestParam(name = "allowFlag", required = false) Boolean allowFlag,
            @RequestParam(name = "dataScopeOverride", required = false) String dataScopeOverride,
            @RequestParam(name = "moduleName", required = false) String moduleName,
            @RequestParam(name = "initiationChannel", required = false) String initiationChannel,
            @RequestParam(name = "approvalRequiredFlag", required = false) Boolean approvalRequiredFlag,
            @RequestParam(name = "workflowTypeId", required = false) UUID workflowTypeId,
            @RequestParam(name = "legalEntityId", required = false) UUID legalEntityId,
            @RequestParam(name = "branchId", required = false) UUID branchId,
            @RequestParam(name = "departmentId", required = false) UUID departmentId,
            @RequestParam(name = "employeeCategoryId", required = false) UUID employeeCategoryId,
            @RequestParam(name = "workerTypeId", required = false) UUID workerTypeId,
            @RequestParam(name = "serviceRequestTypeId", required = false) UUID serviceRequestTypeId,
            @RequestParam(name = "approverSourceType", required = false) String approverSourceType,
            @RequestParam(name = "levelNo", required = false) Integer levelNo,
            @RequestParam(name = "delegationAllowedFlag", required = false) Boolean delegationAllowedFlag,
            @RequestParam(name = "eventCode", required = false) String eventCode,
            @RequestParam(name = "channelType", required = false) String channelType,
            @RequestParam(name = "languageCode", required = false) String languageCode,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "attachmentRequiredFlag", required = false) Boolean attachmentRequiredFlag,
            @RequestParam(name = "autoCloseAllowedFlag", required = false) Boolean autoCloseAllowedFlag,
            @RequestParam(name = "approvalAllowedFlag", required = false) Boolean approvalAllowedFlag,
            @RequestParam(name = "actionAllowedFlag", required = false) Boolean actionAllowedFlag,
            @RequestParam(name = "viewAllowedFlag", required = false) Boolean viewAllowedFlag,
            @RequestParam(name = "temporaryOnlyFlag", required = false) Boolean temporaryOnlyFlag,
            @RequestParam(name = "finalActionFlag", required = false) Boolean finalActionFlag
    ) {
        return moduleApi.list(resolve(resource), new WorkflowAccessModels.SearchQuery(
                q, active, limit, offset, sort, roleType, moduleCode, actionType, scopeType, roleId, permissionId, allowFlag, dataScopeOverride,
                moduleName, initiationChannel, approvalRequiredFlag, workflowTypeId, legalEntityId, branchId, departmentId, employeeCategoryId,
                workerTypeId, serviceRequestTypeId, approverSourceType, levelNo, delegationAllowedFlag, eventCode, channelType, languageCode,
                category, attachmentRequiredFlag, autoCloseAllowedFlag, approvalAllowedFlag, actionAllowedFlag, viewAllowedFlag, temporaryOnlyFlag,
                finalActionFlag));
    }

    @PatchMapping("/{resource}/{id}/status")
    public Mono<WorkflowAccessModels.MasterViewDto> updateStatus(
            @PathVariable("resource") String resource,
            @PathVariable("id") UUID id,
            @Valid @RequestBody StatusRequest request
    ) {
        return moduleApi.updateStatus(resolve(resource), id, new WorkflowAccessModels.StatusUpdateCommand(request.active()));
    }

    @GetMapping("/{resource}/options")
    public Flux<WorkflowAccessModels.OptionViewDto> options(
            @PathVariable("resource") String resource,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") @Min(1) @Max(500) int limit,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return moduleApi.options(resolve(resource), q, limit, activeOnly);
    }

    private WorkflowAccessModels.Resource resolve(String resource) {
        return WorkflowAccessModels.Resource.fromPath(resource);
    }

    public record StatusRequest(@NotNull Boolean active) {
    }

    public record MasterRequest(
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
        WorkflowAccessModels.MasterUpsertRequest toModel() {
            return new WorkflowAccessModels.MasterUpsertRequest(
                    code,
                    name,
                    roleType,
                    moduleCode,
                    actionType,
                    scopeType,
                    roleId,
                    permissionId,
                    allowFlag,
                    dataScopeOverride,
                    moduleName,
                    initiationChannel,
                    approvalRequiredFlag,
                    workflowTypeId,
                    matrixName,
                    legalEntityId,
                    branchId,
                    departmentId,
                    employeeCategoryId,
                    workerTypeId,
                    serviceRequestTypeId,
                    minAmount,
                    maxAmount,
                    levelNo,
                    approverSourceType,
                    approverRoleId,
                    approverUserRef,
                    approvalActionTypeId,
                    escalationDays,
                    delegationAllowedFlag,
                    eventCode,
                    channelType,
                    subjectTemplate,
                    bodyTemplate,
                    languageCode,
                    category,
                    attachmentRequiredFlag,
                    autoCloseAllowedFlag,
                    approvalAllowedFlag,
                    actionAllowedFlag,
                    viewAllowedFlag,
                    temporaryOnlyFlag,
                    finalActionFlag,
                    description,
                    active);
        }
    }
}
