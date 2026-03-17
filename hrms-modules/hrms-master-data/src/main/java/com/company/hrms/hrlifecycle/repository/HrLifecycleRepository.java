package com.company.hrms.hrlifecycle.repository;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HrLifecycleRepository {

    Mono<HrLifecycleModels.MasterViewDto> create(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request, String actor);

    Mono<HrLifecycleModels.MasterViewDto> update(
            String tenantId,
            HrLifecycleModels.Resource resource,
            UUID id,
            HrLifecycleModels.MasterUpsertRequest request,
            String actor
    );

    Mono<HrLifecycleModels.MasterViewDto> get(String tenantId, HrLifecycleModels.Resource resource, UUID id);

    Flux<HrLifecycleModels.MasterViewDto> list(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query);

    Mono<Long> count(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query);

    Mono<HrLifecycleModels.MasterViewDto> updateStatus(String tenantId, HrLifecycleModels.Resource resource, UUID id, boolean active, String actor);

    Flux<HrLifecycleModels.OptionViewDto> options(String tenantId, HrLifecycleModels.Resource resource, String q, int limit, boolean activeOnly);

    Mono<Boolean> codeExists(String tenantId, HrLifecycleModels.Resource resource, String code, UUID excludeId);

    Mono<Boolean> existsReferenceCode(String tenantId, String tableName, String codeColumn, String code, boolean tenantOwned);
}
