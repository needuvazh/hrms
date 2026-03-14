package com.company.hrms.integrationhub.service.impl;

import com.company.hrms.integrationhub.model.*;
import com.company.hrms.integrationhub.repository.*;
import com.company.hrms.integrationhub.service.*;

import com.company.hrms.integrationhub.model.ExecuteIntegrationCommandDto;
import com.company.hrms.integrationhub.model.RegisterIntegrationDefinitionCommandDto;
import com.company.hrms.integrationhub.model.RegisterIntegrationEndpointCommandDto;
import com.company.hrms.integrationhub.model.IntegrationAdapter;
import com.company.hrms.integrationhub.model.IntegrationAdapterRegistry;
import com.company.hrms.integrationhub.model.IntegrationAdapterResultDto;
import com.company.hrms.integrationhub.model.IntegrationDefinitionDto;
import com.company.hrms.integrationhub.model.IntegrationEndpointDto;
import com.company.hrms.integrationhub.model.IntegrationExecutionDto;
import com.company.hrms.integrationhub.repository.IntegrationHubRepository;
import com.company.hrms.integrationhub.model.IntegrationInvocationDto;
import com.company.hrms.integrationhub.model.IntegrationProviderType;
import com.company.hrms.integrationhub.model.IntegrationStatus;
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
        StepVerifier.create(service.registerDefinition(new RegisterIntegrationDefinitionCommandDto(
                                "bio_default",
                                IntegrationProviderType.BIOMETRIC_DEVICE,
                                "Biometric Device",
                                true))
                        .flatMap(definition -> service.registerEndpoint(new RegisterIntegrationEndpointCommandDto(
                                definition.id(),
                                "bio_endpoint",
                                "stub://biometric/default",
                                "NONE",
                                "{}",
                                true)).flatMap(endpoint -> service.execute(new ExecuteIntegrationCommandDto(
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

        repository.definitions.put(definitionId, new IntegrationDefinitionDto(
                definitionId,
                "tenant-a",
                "LDAP_SYNC",
                IntegrationProviderType.LDAP_SSO,
                "LDAP",
                IntegrationStatus.ACTIVE,
                now,
                now));

        repository.endpoints.put(endpointId, new IntegrationEndpointDto(
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

        repository.executions.put(UUID.randomUUID(), new IntegrationExecutionDto(
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
        public Mono<IntegrationAdapterResultDto> execute(IntegrationInvocationDto invocation) {
            return Mono.just(IntegrationAdapterResultDto.success("stub-ref"));
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
        private final Map<UUID, IntegrationDefinitionDto> definitions = new ConcurrentHashMap<>();
        private final Map<UUID, IntegrationEndpointDto> endpoints = new ConcurrentHashMap<>();
        private final Map<UUID, IntegrationExecutionDto> executions = new ConcurrentHashMap<>();

        @Override
        public Mono<IntegrationDefinitionDto> saveDefinition(IntegrationDefinitionDto definition) {
            definitions.put(definition.id(), definition);
            return Mono.just(definition);
        }

        @Override
        public Mono<IntegrationDefinitionDto> findDefinitionById(String tenantId, UUID definitionId) {
            IntegrationDefinitionDto definition = definitions.get(definitionId);
            if (definition == null || !tenantId.equals(definition.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(definition);
        }

        @Override
        public Mono<IntegrationEndpointDto> saveEndpoint(IntegrationEndpointDto endpoint) {
            endpoints.put(endpoint.id(), endpoint);
            return Mono.just(endpoint);
        }

        @Override
        public Mono<IntegrationEndpointDto> findEndpointById(String tenantId, UUID endpointId) {
            IntegrationEndpointDto endpoint = endpoints.get(endpointId);
            if (endpoint == null || !tenantId.equals(endpoint.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(endpoint);
        }

        @Override
        public Mono<IntegrationExecutionDto> saveExecution(IntegrationExecutionDto execution) {
            executions.put(execution.id(), execution);
            return Mono.just(execution);
        }

        @Override
        public Mono<IntegrationExecutionDto> updateExecution(IntegrationExecutionDto execution) {
            executions.put(execution.id(), execution);
            return Mono.just(execution);
        }

        @Override
        public Flux<IntegrationExecutionDto> findExecutionsByDefinition(String tenantId, UUID definitionId, int limit) {
            return Flux.fromIterable(executions.values())
                    .filter(execution -> tenantId.equals(execution.tenantId()))
                    .filter(execution -> definitionId.equals(execution.definitionId()))
                    .take(limit);
        }
    }
}
