package com.company.hrms.tenant.service;

import com.company.hrms.tenant.model.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantModuleApi {

    Mono<TenantViewDto> getTenantByCode(String tenantCode);

    Flux<CountryViewDto> listCountries();

    Flux<TenantCountryConfigViewDto> listTenantCountries(String tenantCode);
}
