package com.company.hrms.masterdata.repository;

import com.company.hrms.masterdata.model.*;

import reactor.core.publisher.Flux;

public interface MasterDataRepository {

    Flux<LookupValueDto> findByType(String lookupType, String tenantId);
}
