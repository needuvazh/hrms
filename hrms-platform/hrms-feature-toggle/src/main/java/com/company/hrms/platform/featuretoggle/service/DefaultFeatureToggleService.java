package com.company.hrms.platform.featuretoggle.service;

import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import reactor.core.publisher.Mono;

public class DefaultFeatureToggleService implements FeatureToggleService {

    private final FeatureToggleRepository featureToggleRepository;
    private final TenantContextAccessor tenantContextAccessor;

    public DefaultFeatureToggleService(
            FeatureToggleRepository featureToggleRepository,
            TenantContextAccessor tenantContextAccessor
    ) {
        this.featureToggleRepository = featureToggleRepository;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
        return featureToggleRepository.isModuleEnabled(tenantCode, moduleKey);
    }

    @Override
    public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
        return featureToggleRepository.isFeatureEnabled(tenantCode, featureKey);
    }

    @Override
    public Mono<Boolean> currentTenantHasModule(String moduleKey) {
        return tenantContextAccessor.currentTenantId()
                .flatMap(tenantCode -> featureToggleRepository.isModuleEnabled(tenantCode, moduleKey))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> currentTenantHasFeature(String featureKey) {
        return tenantContextAccessor.currentTenantId()
                .flatMap(tenantCode -> featureToggleRepository.isFeatureEnabled(tenantCode, featureKey))
                .defaultIfEmpty(false);
    }
}
