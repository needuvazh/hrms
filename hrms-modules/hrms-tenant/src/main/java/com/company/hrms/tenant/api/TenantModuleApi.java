package com.company.hrms.tenant.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantModuleApi {

    Mono<TenantView> getTenantByCode(String tenantCode);

    Flux<CountryView> listCountries();

    Flux<TenantCountryConfigView> listTenantCountries(String tenantCode);
}
