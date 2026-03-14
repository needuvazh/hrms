package com.company.hrms.tenant.application;

import com.company.hrms.tenant.api.CountryView;
import com.company.hrms.tenant.api.TenantModuleApi;
import com.company.hrms.tenant.api.TenantCountryConfigView;
import com.company.hrms.tenant.api.TenantView;
import com.company.hrms.tenant.domain.Country;
import com.company.hrms.tenant.domain.Tenant;
import com.company.hrms.tenant.domain.TenantCountryConfig;
import com.company.hrms.tenant.domain.TenantRepository;
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
    public Mono<TenantView> getTenantByCode(String tenantCode) {
        return tenantRepository.findByTenantCode(tenantCode).map(this::toView);
    }

    @Override
    public Flux<CountryView> listCountries() {
        return tenantRepository.listCountries().map(this::toCountryView);
    }

    @Override
    public Flux<TenantCountryConfigView> listTenantCountries(String tenantCode) {
        return tenantRepository.listTenantCountries(tenantCode).map(this::toTenantCountryConfigView);
    }

    private TenantView toView(Tenant tenant) {
        return new TenantView(tenant.id(), tenant.tenantCode(), tenant.tenantName(), tenant.active());
    }

    private CountryView toCountryView(Country country) {
        return new CountryView(
                country.countryCode(),
                country.countryName(),
                country.currencyCode(),
                country.timezone(),
                country.locale(),
                country.active());
    }

    private TenantCountryConfigView toTenantCountryConfigView(TenantCountryConfig config) {
        return new TenantCountryConfigView(
                config.tenantCode(),
                config.countryCode(),
                config.primaryCountry(),
                config.complianceProfile(),
                config.effectiveFrom(),
                config.effectiveTo(),
                config.active());
    }
}
