package com.company.hrms.platform.featuretoggle.service;

import com.company.hrms.platform.featuretoggle.api.TenantFeatureFlag;
import com.company.hrms.platform.featuretoggle.api.TenantModuleSubscription;
import reactor.core.publisher.Mono;

public interface FeatureToggleRepository {

    Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey);

    Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey);

    default Mono<TenantModuleSubscription> upsertModuleSubscription(String tenantCode, String moduleKey, boolean enabled) {
        return Mono.error(new UnsupportedOperationException("Module subscription updates are not supported"));
    }

    default Mono<TenantFeatureFlag> upsertFeatureFlag(String tenantCode, String featureKey, boolean enabled) {
        return Mono.error(new UnsupportedOperationException("Feature flag updates are not supported"));
    }
}
