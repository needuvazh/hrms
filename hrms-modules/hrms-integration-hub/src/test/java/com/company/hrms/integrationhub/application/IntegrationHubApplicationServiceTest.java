package com.company.hrms.integrationhub.application;

import com.company.hrms.integrationhub.api.ExecuteIntegrationCommand;
import com.company.hrms.integrationhub.api.RegisterIntegrationDefinitionCommand;
import com.company.hrms.integrationhub.api.RegisterIntegrationEndpointCommand;
import com.company.hrms.integrationhub.domain.IntegrationAdapter;
import com.company.hrms.integrationhub.domain.IntegrationAdapterRegistry;
import com.company.hrms.integrationhub.domain.IntegrationAdapterResult;
import com.company.hrms.integrationhub.domain.IntegrationDefinition;
import com.company.hrms.integrationhub.domain.IntegrationEndpoint;
import com.company.hrms.integrationhub.domain.IntegrationExecution;
import com.company.hrms.integrationhub.domain.IntegrationHubRepository;
import com.company.hrms.integrationhub.domain.IntegrationInvocation;
import com.company.hrms.integrationhub.domain.IntegrationProviderType;
import com.company.hrms.integrationhub.domain.IntegrationStatus;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrationHubApplicationServiceTest {

    private final InMemoryIntegrationHubRepository repository = new InMemoryIntegrationHubRepository();
    private final CapturingAuditEventPublisher auditEventPublisher = new CapturingAuditEventPublisher();

    private final IntegrationHubApplicationService service = new IntegrationHubApplicationService(
            repository,
            new IntegrationAdapterRegistry(List.of(new StubBiometricAdapter())),
            new DefaultTenantContextAccessor(),
            auditEventPublisher);

    @Test
    void executesBiometricIntegrationAndRecordsAudit() {
        StepVerifier.create(service.registerDefinition(new RegisterIntegrationDefinitionCommand(
                                "bio_default",
                                IntegrationProviderType.BIOMETRIC_DEVICE,
                                "Biometric Device",
                                true))
                        .flatMap(definition -> service.registerEndpoint(new RegisterIntegrationEndpointCommand(
                                definition.id(),
                                "bio_endpoint",
                                "stub://biometric/default",
                                "NONE",
                                "{}",
                                true)).flatMap(endpoint -> service.execute(new ExecuteIntegrationCommand(
                                definition.id(),
                                endpoint.id(),
                                "PULL_PUNCH_EVENTS",
                                "{\"batch\":1}",
                                "integration-admin"))))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(execution -> {
                    assertEquals(IntegrationStatus.SUCCESS, execution.status());
                    assertEquals("default", execution.tenantId());
                })
                .verifyComplete();

        assertEquals(2, auditEventPublisher.events.size());
        assertEquals("INTEGRATION_EXECUTION_ATTEMPT", auditEventPublisher.events.get(0).action());
        assertEquals("INTEGRATION_EXECUTION_SUCCESS", auditEventPublisher.events.get(1).action());
    }

    @Test
    void keepsExecutionLookupTenantScoped() {
        UUID definitionId = UUID.randomUUID();
        UUID endpointId = UUID.randomUUID();
        Instant now = Instant.now();

        repository.definitions.put(definitionId, new IntegrationDefinition(
                definitionId,
                "tenant-a",
                "LDAP_SYNC",
                IntegrationProviderType.LDAP_SSO,
                "LDAP",
                IntegrationStatus.ACTIVE,
                now,
                now));

        repository.endpoints.put(endpointId, new IntegrationEndpoint(
                endpointId,
                definitionId,
                "tenant-a",
                "LDAP_ENDPOINT",
                "stub://ldap",
                "BASIC",
                "{}",
                IntegrationStatus.ACTIVE,
                now,
                now));

        repository.executions.put(UUID.randomUUID(), new IntegrationExecution(
                UUID.randomUUID(),
                "tenant-a",
                definitionId,
                endpointId,
                "SYNC_USERS",
                "{}",
                IntegrationStatus.SUCCESS,
                "ok-1",
                null,
                now,
                now,
                now,
                now));

        StepVerifier.create(service.recentExecutions(definitionId, 20)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .expectNextCount(0)
                .verifyComplete();
    }

    private static class StubBiometricAdapter implements IntegrationAdapter {
        @Override
        public IntegrationProviderType providerType() {
            return IntegrationProviderType.BIOMETRIC_DEVICE;
        }

        @Override
        public Mono<IntegrationAdapterResult> execute(IntegrationInvocation invocation) {
            return Mono.just(IntegrationAdapterResult.success("stub-ref"));
        }
    }

    private static class CapturingAuditEventPublisher implements AuditEventPublisher {
        private final List<AuditEvent> events = new ArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            events.add(event);
            return Mono.empty();
        }
    }

    private static class InMemoryIntegrationHubRepository implements IntegrationHubRepository {
        private final Map<UUID, IntegrationDefinition> definitions = new ConcurrentHashMap<>();
        private final Map<UUID, IntegrationEndpoint> endpoints = new ConcurrentHashMap<>();
        private final Map<UUID, IntegrationExecution> executions = new ConcurrentHashMap<>();

        @Override
        public Mono<IntegrationDefinition> saveDefinition(IntegrationDefinition definition) {
            definitions.put(definition.id(), definition);
            return Mono.just(definition);
        }

        @Override
        public Mono<IntegrationDefinition> findDefinitionById(String tenantId, UUID definitionId) {
            IntegrationDefinition definition = definitions.get(definitionId);
            if (definition == null || !tenantId.equals(definition.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(definition);
        }

        @Override
        public Mono<IntegrationEndpoint> saveEndpoint(IntegrationEndpoint endpoint) {
            endpoints.put(endpoint.id(), endpoint);
            return Mono.just(endpoint);
        }

        @Override
        public Mono<IntegrationEndpoint> findEndpointById(String tenantId, UUID endpointId) {
            IntegrationEndpoint endpoint = endpoints.get(endpointId);
            if (endpoint == null || !tenantId.equals(endpoint.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(endpoint);
        }

        @Override
        public Mono<IntegrationExecution> saveExecution(IntegrationExecution execution) {
            executions.put(execution.id(), execution);
            return Mono.just(execution);
        }

        @Override
        public Mono<IntegrationExecution> updateExecution(IntegrationExecution execution) {
            executions.put(execution.id(), execution);
            return Mono.just(execution);
        }

        @Override
        public Flux<IntegrationExecution> findExecutionsByDefinition(String tenantId, UUID definitionId, int limit) {
            return Flux.fromIterable(executions.values())
                    .filter(execution -> tenantId.equals(execution.tenantId()))
                    .filter(execution -> definitionId.equals(execution.definitionId()))
                    .take(limit);
        }
    }
}
