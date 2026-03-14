package com.company.hrms.masterdata.domain;

import reactor.core.publisher.Flux;

public interface MasterDataRepository {

    Flux<LookupValue> findByType(String lookupType, String tenantId);
}
