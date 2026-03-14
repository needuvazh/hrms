package com.company.hrms.masterdata.service.impl;

import com.company.hrms.masterdata.model.*;
import com.company.hrms.masterdata.repository.*;
import com.company.hrms.masterdata.service.*;

import com.company.hrms.masterdata.model.LookupValueViewDto;
import com.company.hrms.masterdata.service.MasterDataModuleApi;
import com.company.hrms.masterdata.repository.MasterDataRepository;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MasterDataApplicationService implements MasterDataModuleApi {

    private final MasterDataRepository masterDataRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public MasterDataApplicationService(MasterDataRepository masterDataRepository, TenantContextAccessor tenantContextAccessor) {
        this.masterDataRepository = masterDataRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Flux<LookupValueViewDto> getLookupValues(String lookupType) {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                .flatMapMany(tenantId -> masterDataRepository.findByType(lookupType, tenantId)
                        .map(value -> new LookupValueViewDto(
                                value.lookupType(),
                                value.lookupCode(),
                                value.lookupLabel(),
                                value.sortOrder())));
    }
}
