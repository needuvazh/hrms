package com.company.hrms.platform.featuretoggle.service;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.TenantFeatureFlag;
import com.company.hrms.platform.featuretoggle.api.TenantModuleSubscription;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultModuleEnablementAdminServiceTest {

    @Test
    void setModuleEnabledPersistsAndPublishesAuditEvent() {
        InMemoryFeatureToggleRepository repository = new InMemoryFeatureToggleRepository();
        RecordingAuditPublisher auditPublisher = new RecordingAuditPublisher();
        DefaultModuleEnablementAdminService service = new DefaultModuleEnablementAdminService(repository, auditPublisher);

        StepVerifier.create(service.setModuleEnabled("default", "employee", true, "platform-admin"))
                .expectNextMatches(subscription -> subscription.enabled() && "employee".equals(subscription.moduleKey()))
                .verifyComplete();

        assertEquals(Boolean.TRUE, repository.moduleState.get("default:employee"));
        assertEquals("MODULE_ENABLEMENT_CHANGED", auditPublisher.actions.getFirst());
    }

    @Test
    void setFeatureEnabledPersistsAndPublishesAuditEvent() {
        InMemoryFeatureToggleRepository repository = new InMemoryFeatureToggleRepository();
        RecordingAuditPublisher auditPublisher = new RecordingAuditPublisher();
        DefaultModuleEnablementAdminService service = new DefaultModuleEnablementAdminService(repository, auditPublisher);

        StepVerifier.create(service.setFeatureEnabled("default", "employee.export", false, "platform-admin"))
                .expectNextMatches(flag -> !flag.enabled() && "employee.export".equals(flag.featureKey()))
                .verifyComplete();

        assertEquals(Boolean.FALSE, repository.featureState.get("default:employee.export"));
        assertEquals("FEATURE_FLAG_CHANGED", auditPublisher.actions.getFirst());
    }

    static class InMemoryFeatureToggleRepository implements FeatureToggleRepository {

        private final Map<String, Boolean> moduleState = new ConcurrentHashMap<>();
        private final Map<String, Boolean> featureState = new ConcurrentHashMap<>();

        @Override
        public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
            return Mono.just(moduleState.getOrDefault(tenantCode + ":" + moduleKey, false));
        }

        @Override
        public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
            return Mono.just(featureState.getOrDefault(tenantCode + ":" + featureKey, false));
        }

        @Override
        public Mono<TenantModuleSubscription> upsertModuleSubscription(String tenantCode, String moduleKey, boolean enabled) {
            moduleState.put(tenantCode + ":" + moduleKey, enabled);
            return Mono.just(new TenantModuleSubscription(tenantCode, moduleKey, enabled));
        }

        @Override
        public Mono<TenantFeatureFlag> upsertFeatureFlag(String tenantCode, String featureKey, boolean enabled) {
            featureState.put(tenantCode + ":" + featureKey, enabled);
            return Mono.just(new TenantFeatureFlag(tenantCode, featureKey, enabled));
        }
    }

    static class RecordingAuditPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }
}
