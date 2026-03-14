package com.company.hrms.platform.featuretoggle.api;

import reactor.core.publisher.Mono;

public interface FeatureToggleService {

    Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey);

    Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey);

    Mono<Boolean> currentTenantHasModule(String moduleKey);

    Mono<Boolean> currentTenantHasFeature(String featureKey);
}
