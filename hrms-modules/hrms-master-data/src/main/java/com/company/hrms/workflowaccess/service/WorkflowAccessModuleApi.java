package com.company.hrms.workflowaccess.service;

import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import com.company.hrms.masterdata.reference.api.PagedResult;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowAccessModuleApi {

    Mono<WorkflowAccessModels.MasterViewDto> create(WorkflowAccessModels.Resource resource, WorkflowAccessModels.MasterUpsertRequest request);

    Mono<WorkflowAccessModels.MasterViewDto> update(WorkflowAccessModels.Resource resource, UUID id, WorkflowAccessModels.MasterUpsertRequest request);

    Mono<WorkflowAccessModels.MasterViewDto> get(WorkflowAccessModels.Resource resource, UUID id);

    Mono<PagedResult<WorkflowAccessModels.MasterViewDto>> list(WorkflowAccessModels.Resource resource, WorkflowAccessModels.SearchQuery query);

    Mono<WorkflowAccessModels.MasterViewDto> updateStatus(WorkflowAccessModels.Resource resource, UUID id, WorkflowAccessModels.StatusUpdateCommand command);

    Flux<WorkflowAccessModels.OptionViewDto> options(WorkflowAccessModels.Resource resource, String q, int limit, boolean activeOnly);
}
