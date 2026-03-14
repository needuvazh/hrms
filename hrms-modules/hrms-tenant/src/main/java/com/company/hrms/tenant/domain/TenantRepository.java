package com.company.hrms.tenant.domain;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface TenantRepository {

    Mono<Tenant> findByTenantCode(String tenantCode);

    Flux<Country> listCountries();

    Flux<TenantCountryConfig> listTenantCountries(String tenantCode);
}
