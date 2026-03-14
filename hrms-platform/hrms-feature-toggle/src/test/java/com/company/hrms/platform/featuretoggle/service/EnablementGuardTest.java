package com.company.hrms.platform.featuretoggle.service;

import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EnablementGuardTest {

    @Test
    void rejectsWhenModuleDisabled() {
        FeatureToggleService featureToggleService = new FeatureToggleService() {
            @Override
            public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
                return Mono.just(false);
            }

            @Override
            public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
                return Mono.just(false);
            }

            @Override
            public Mono<Boolean> currentTenantHasModule(String moduleKey) {
                return Mono.just(false);
            }

            @Override
            public Mono<Boolean> currentTenantHasFeature(String featureKey) {
                return Mono.just(false);
            }
        };

        EnablementGuard guard = new EnablementGuard(featureToggleService);

        StepVerifier.create(guard.requireModuleEnabled("employee"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException exception = (HrmsException) error;
                    assertEquals("MODULE_DISABLED", exception.getErrorCode());
                })
                .verify();
    }

    @Test
    void passesWhenFeatureEnabled() {
        FeatureToggleService featureToggleService = new FeatureToggleService() {
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
        };

        EnablementGuard guard = new EnablementGuard(featureToggleService);

        StepVerifier.create(guard.requireFeatureEnabled("employee.search")).verifyComplete();
    }
}
