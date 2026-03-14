package com.company.hrms.masterdata.reference.infrastructure;

import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceMasterRow;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReferenceMasterRepository {

    Mono<ReferenceMasterRow> create(ReferenceResource resource, ReferenceMasterUpsertRequest request, String actor);

    Mono<ReferenceMasterRow> update(ReferenceResource resource, UUID id, ReferenceMasterUpsertRequest request, String actor);

    Mono<ReferenceMasterRow> findById(ReferenceResource resource, UUID id);

    Flux<ReferenceMasterRow> list(ReferenceResource resource, ReferenceSearchQuery query);

    Mono<Long> count(ReferenceResource resource, ReferenceSearchQuery query);

    Flux<ReferenceOptionViewDto> options(ReferenceResource resource, boolean activeOnly);

    Mono<Boolean> existsCode(ReferenceResource resource, String code, UUID excludeId);

    Mono<Boolean> existsName(ReferenceResource resource, String name, UUID excludeId);

    Mono<Void> updateStatus(ReferenceResource resource, UUID id, boolean active, String actor);

    Mono<Boolean> existsById(ReferenceResource resource, UUID id);

    Mono<Boolean> existsCurrencyCode(String currencyCode);

    Mono<Boolean> existsCountryCode(String countryCode);
}
