package com.company.hrms.platform.featuretoggle.api;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

public class EnablementGuard {

    private final FeatureToggleService featureToggleService;

    public EnablementGuard(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public Mono<Void> requireModuleEnabled(String moduleKey) {
        return featureToggleService.currentTenantHasModule(moduleKey)
                .flatMap(enabled -> enabled
                        ? Mono.empty()
                        : Mono.error(new HrmsException(
                        HttpStatus.FORBIDDEN,
                        "MODULE_DISABLED",
                        "Module is disabled for tenant: " + moduleKey)));
    }

    public Mono<Void> requireFeatureEnabled(String featureKey) {
        return featureToggleService.currentTenantHasFeature(featureKey)
                .flatMap(enabled -> enabled
                        ? Mono.empty()
                        : Mono.error(new HrmsException(
                        HttpStatus.FORBIDDEN,
                        "FEATURE_DISABLED",
                        "Feature is disabled for tenant: " + featureKey)));
    }
}
