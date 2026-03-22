package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReferenceControllerService {
    Mono<ReferenceMasterViewDto> create(ReferenceMasterUpsertRequest request);
    Mono<ReferenceMasterViewDto> update(UUID id, ReferenceMasterUpsertRequest request);
    Mono<ReferenceMasterViewDto> getById(UUID id);
    Mono<PagedResult<ReferenceMasterViewDto>> list(ReferenceSearchQuery query);
    Mono<Void> updateStatus(UUID id, boolean active);
    Flux<ReferenceOptionViewDto> options(boolean activeOnly);
}
