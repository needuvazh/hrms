package com.company.hrms.jobarchitecture.service;

import com.company.hrms.jobarchitecture.model.JobArchitectureModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JobArchitectureModuleApi {

    Mono<JobArchitectureModels.MasterViewDto> create(JobArchitectureModels.Resource resource, JobArchitectureModels.MasterUpsertRequest request);

    Mono<JobArchitectureModels.MasterViewDto> update(JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.MasterUpsertRequest request);

    Mono<JobArchitectureModels.MasterViewDto> get(JobArchitectureModels.Resource resource, UUID id);

    Flux<JobArchitectureModels.MasterViewDto> list(JobArchitectureModels.Resource resource, JobArchitectureModels.SearchQuery query);

    Mono<JobArchitectureModels.MasterViewDto> updateStatus(JobArchitectureModels.Resource resource, UUID id, JobArchitectureModels.StatusUpdateCommand command);

    Flux<JobArchitectureModels.OptionViewDto> options(JobArchitectureModels.Resource resource, String q, int limit);
}
