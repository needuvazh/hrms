package com.company.hrms.masterdata.service;

import com.company.hrms.masterdata.model.*;

import reactor.core.publisher.Flux;

public interface MasterDataModuleApi {

    Flux<LookupValueViewDto> getLookupValues(String lookupType);
}
