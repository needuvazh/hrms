package com.company.hrms.platform.featuretoggle.api;

import reactor.core.publisher.Mono;

public interface ModuleEnablementAdminApi {

    Mono<TenantModuleSubscription> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled, String actor);

    Mono<TenantFeatureFlag> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled, String actor);
}
