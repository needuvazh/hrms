package com.company.hrms.workflowaccess.repository;

import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowAccessRepository {

    Mono<WorkflowAccessModels.MasterViewDto> create(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            WorkflowAccessModels.MasterUpsertRequest request,
            String actor
    );

    Mono<WorkflowAccessModels.MasterViewDto> update(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            UUID id,
            WorkflowAccessModels.MasterUpsertRequest request,
            String actor
    );

    Mono<WorkflowAccessModels.MasterViewDto> get(String tenantId, WorkflowAccessModels.Resource resource, UUID id);

    Flux<WorkflowAccessModels.MasterViewDto> list(String tenantId, WorkflowAccessModels.Resource resource, WorkflowAccessModels.SearchQuery query);

    Mono<WorkflowAccessModels.MasterViewDto> updateStatus(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    );

    Flux<WorkflowAccessModels.OptionViewDto> options(
            String tenantId,
            WorkflowAccessModels.Resource resource,
            String q,
            int limit,
            boolean activeOnly
    );

    Mono<Boolean> codeExists(String tenantId, WorkflowAccessModels.Resource resource, String code, UUID excludeId);

    Mono<Boolean> existsById(String tenantId, String tableName, UUID id, boolean tenantOwned);
}
