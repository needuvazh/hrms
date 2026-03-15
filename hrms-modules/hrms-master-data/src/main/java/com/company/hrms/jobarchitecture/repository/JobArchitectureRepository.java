package com.company.hrms.jobarchitecture.repository;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JobArchitectureRepository {

    Mono<JobArchitectureModels.MasterViewDto> create(String tenantId, JobArchitectureModels.Resource resource, JobArchitectureModels.MasterUpsertRequest request, String actor);

    Mono<JobArchitectureModels.MasterViewDto> update(String tenantId, JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.MasterUpsertRequest request, String actor);

    Mono<JobArchitectureModels.MasterViewDto> get(String tenantId, JobArchitectureModels.Resource resource, UUID id);

    Flux<JobArchitectureModels.MasterViewDto> list(String tenantId, JobArchitectureModels.Resource resource, JobArchitectureModels.SearchQuery query);

    Mono<JobArchitectureModels.MasterViewDto> updateStatus(String tenantId, JobArchitectureModels.Resource resource, UUID id, boolean active, String actor);

    Flux<JobArchitectureModels.OptionViewDto> options(String tenantId, JobArchitectureModels.Resource resource, String q, int limit);

    Mono<Boolean> codeExists(String tenantId, JobArchitectureModels.Resource resource, String code, UUID excludeId);

    Mono<Boolean> existsById(String tenantId, String tableName, UUID id);

    Mono<UUID> findParentPosition(String tenantId, UUID id);
}
