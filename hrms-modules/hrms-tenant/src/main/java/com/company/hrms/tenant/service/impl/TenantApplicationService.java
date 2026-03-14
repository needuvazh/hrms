package com.company.hrms.tenant.service.impl;

import com.company.hrms.tenant.model.*;
import com.company.hrms.tenant.repository.*;
import com.company.hrms.tenant.service.*;

import com.company.hrms.tenant.model.CountryViewDto;
import com.company.hrms.tenant.service.TenantModuleApi;
import com.company.hrms.tenant.model.TenantCountryConfigViewDto;
import com.company.hrms.tenant.model.TenantViewDto;
import com.company.hrms.tenant.model.CountryDto;
import com.company.hrms.tenant.model.TenantDto;
import com.company.hrms.tenant.model.TenantCountryConfigDto;
import com.company.hrms.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TenantApplicationService implements TenantModuleApi {

    private final TenantRepository tenantRepository;

    public TenantApplicationService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Mono<TenantViewDto> getTenantByCode(String tenantCode) {
        return tenantRepository.findByTenantCode(tenantCode).map(this::toView);
    }

    @Override
    public Flux<CountryViewDto> listCountries() {
        return tenantRepository.listCountries().map(this::toCountryView);
    }

    @Override
    public Flux<TenantCountryConfigViewDto> listTenantCountries(String tenantCode) {
        return tenantRepository.listTenantCountries(tenantCode).map(this::toTenantCountryConfigView);
    }

    private TenantViewDto toView(TenantDto tenant) {
        return new TenantViewDto(tenant.id(), tenant.tenantCode(), tenant.tenantName(), tenant.active());
    }

    private CountryViewDto toCountryView(CountryDto country) {
        return new CountryViewDto(
                country.countryCode(),
                country.countryName(),
                country.currencyCode(),
                country.timezone(),
                country.locale(),
                country.active());
    }

    private TenantCountryConfigViewDto toTenantCountryConfigView(TenantCountryConfigDto config) {
        return new TenantCountryConfigViewDto(
                config.tenantCode(),
                config.countryCode(),
                config.primaryCountry(),
                config.complianceProfile(),
                config.effectiveFrom(),
                config.effectiveTo(),
                config.active());
    }
}
