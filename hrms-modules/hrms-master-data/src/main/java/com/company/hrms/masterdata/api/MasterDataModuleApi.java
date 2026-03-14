package com.company.hrms.masterdata.api;

import reactor.core.publisher.Flux;

public interface MasterDataModuleApi {

    Flux<LookupValueView> getLookupValues(String lookupType);
}
