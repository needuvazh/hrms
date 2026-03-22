package com.company.hrms.masterdata.reference.infrastructure;

import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceMasterRow;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReferenceResourceDao {

    Mono<ReferenceMasterRow> create(ReferenceMasterUpsertRequest request, String actor);

    Mono<ReferenceMasterRow> update(UUID id, ReferenceMasterUpsertRequest request, String actor);

    Mono<ReferenceMasterRow> findById(UUID id);

    Flux<ReferenceMasterRow> list(ReferenceSearchQuery query);

    Mono<Long> count(ReferenceSearchQuery query);

    Mono<Void> updateStatus(UUID id, boolean active, String actor);

    Flux<ReferenceOptionViewDto> options(boolean activeOnly);

    Mono<Boolean> existsById(UUID id);

    Mono<Boolean> existsCode(String code, UUID excludeId);

    Mono<Boolean> existsName(String name, UUID excludeId);

    Mono<Boolean> existsCurrencyCode(String currencyCode);

    Mono<Boolean> existsCountryCode(String countryCode);

    Mono<Boolean> existsSkillCategoryById(UUID id);

    Mono<String> resolveCurrencyCode(String currencyToken);
}
