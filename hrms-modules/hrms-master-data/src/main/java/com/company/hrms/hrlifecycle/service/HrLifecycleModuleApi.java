package com.company.hrms.hrlifecycle.service;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import com.company.hrms.masterdata.reference.api.PagedResult;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HrLifecycleModuleApi {

    Mono<HrLifecycleModels.MasterViewDto> create(HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request);

    Mono<HrLifecycleModels.MasterViewDto> update(HrLifecycleModels.Resource resource, UUID id, HrLifecycleModels.MasterUpsertRequest request);

    Mono<HrLifecycleModels.MasterViewDto> get(HrLifecycleModels.Resource resource, UUID id);

    Mono<PagedResult<HrLifecycleModels.MasterViewDto>> list(HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query);

    Mono<HrLifecycleModels.MasterViewDto> updateStatus(HrLifecycleModels.Resource resource, UUID id, HrLifecycleModels.StatusUpdateCommand command);

    Flux<HrLifecycleModels.OptionViewDto> options(HrLifecycleModels.Resource resource, String q, int limit, boolean activeOnly);
}
