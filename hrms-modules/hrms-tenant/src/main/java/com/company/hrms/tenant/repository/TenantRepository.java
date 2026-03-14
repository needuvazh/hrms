package com.company.hrms.tenant.repository;

import com.company.hrms.tenant.model.*;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface TenantRepository {

    Mono<TenantDto> findByTenantCode(String tenantCode);

    Flux<CountryDto> listCountries();

    Flux<TenantCountryConfigDto> listTenantCountries(String tenantCode);
}
