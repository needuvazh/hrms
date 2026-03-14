package com.company.hrms.platform.featuretoggle.api;

import reactor.core.publisher.Mono;

public class AlwaysEnabledFeatureToggleService implements FeatureToggleService {

    @Override
    public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> currentTenantHasModule(String moduleKey) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> currentTenantHasFeature(String featureKey) {
        return Mono.just(true);
    }
}
