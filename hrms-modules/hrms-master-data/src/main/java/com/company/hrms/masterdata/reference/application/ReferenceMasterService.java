package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReferenceMasterService {

    Mono<ReferenceMasterViewDto> create(ReferenceResource resource, ReferenceMasterUpsertRequest request);

    Mono<ReferenceMasterViewDto> update(ReferenceResource resource, UUID id, ReferenceMasterUpsertRequest request);

    Mono<ReferenceMasterViewDto> getById(ReferenceResource resource, UUID id);

    Mono<PagedResult<ReferenceMasterViewDto>> list(ReferenceResource resource, ReferenceSearchQuery query);

    Mono<Void> updateStatus(ReferenceResource resource, UUID id, boolean active);

    Flux<ReferenceOptionViewDto> options(ReferenceResource resource, boolean activeOnly);
}
