package com.company.hrms.platform.featuretoggle.service;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.ModuleEnablementAdminApi;
import com.company.hrms.platform.featuretoggle.api.TenantFeatureFlag;
import com.company.hrms.platform.featuretoggle.api.TenantModuleSubscription;
import java.util.Map;
import reactor.core.publisher.Mono;

public class DefaultModuleEnablementAdminService implements ModuleEnablementAdminApi {

    private final FeatureToggleRepository featureToggleRepository;
    private final AuditEventPublisher auditEventPublisher;

    public DefaultModuleEnablementAdminService(
            FeatureToggleRepository featureToggleRepository,
            AuditEventPublisher auditEventPublisher
    ) {
        this.featureToggleRepository = featureToggleRepository;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<TenantModuleSubscription> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled, String actor) {
        return featureToggleRepository.upsertModuleSubscription(tenantCode, moduleKey, enabled)
                .flatMap(subscription -> auditEventPublisher.publish(AuditEvent.of(
                                actor,
                                tenantCode,
                                "MODULE_ENABLEMENT_CHANGED",
                                "MODULE_SUBSCRIPTION",
                                moduleKey,
                                Map.of("enabled", Boolean.toString(enabled))))
                        .thenReturn(subscription));
    }

    @Override
    public Mono<TenantFeatureFlag> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled, String actor) {
        return featureToggleRepository.upsertFeatureFlag(tenantCode, featureKey, enabled)
                .flatMap(flag -> auditEventPublisher.publish(AuditEvent.of(
                                actor,
                                tenantCode,
                                "FEATURE_FLAG_CHANGED",
                                "FEATURE_FLAG",
                                featureKey,
                                Map.of("enabled", Boolean.toString(enabled))))
                        .thenReturn(flag));
    }
}
