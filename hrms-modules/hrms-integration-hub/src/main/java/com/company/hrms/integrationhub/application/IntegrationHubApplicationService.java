package com.company.hrms.integrationhub.application;

import com.company.hrms.integrationhub.api.ExecuteIntegrationCommand;
import com.company.hrms.integrationhub.api.IntegrationDefinitionView;
import com.company.hrms.integrationhub.api.IntegrationEndpointView;
import com.company.hrms.integrationhub.api.IntegrationExecutionView;
import com.company.hrms.integrationhub.api.IntegrationHubModuleApi;
import com.company.hrms.integrationhub.api.RegisterIntegrationDefinitionCommand;
import com.company.hrms.integrationhub.api.RegisterIntegrationEndpointCommand;
import com.company.hrms.integrationhub.domain.IntegrationAdapterRegistry;
import com.company.hrms.integrationhub.domain.IntegrationDefinition;
import com.company.hrms.integrationhub.domain.IntegrationEndpoint;
import com.company.hrms.integrationhub.domain.IntegrationExecution;
import com.company.hrms.integrationhub.domain.IntegrationHubRepository;
import com.company.hrms.integrationhub.domain.IntegrationInvocation;
import com.company.hrms.integrationhub.domain.IntegrationStatus;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IntegrationHubApplicationService implements IntegrationHubModuleApi {

    private static final Logger log = LoggerFactory.getLogger(IntegrationHubApplicationService.class);
    private static final int DEFAULT_LIMIT = 50;

    private final IntegrationHubRepository integrationHubRepository;
    private final IntegrationAdapterRegistry integrationAdapterRegistry;
    private final TenantContextAccessor tenantContextAccessor;
    private final AuditEventPublisher auditEventPublisher;

    public IntegrationHubApplicationService(
            IntegrationHubRepository integrationHubRepository,
            IntegrationAdapterRegistry integrationAdapterRegistry,
            TenantContextAccessor tenantContextAccessor,
            AuditEventPublisher auditEventPublisher
    ) {
        this.integrationHubRepository = integrationHubRepository;
        this.integrationAdapterRegistry = integrationAdapterRegistry;
        this.tenantContextAccessor = tenantContextAccessor;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<IntegrationDefinitionView> registerDefinition(RegisterIntegrationDefinitionCommand command) {
        validateDefinition(command);

        return requireTenant().flatMap(tenantId -> {
            Instant now = Instant.now();
            IntegrationDefinition definition = new IntegrationDefinition(
                    UUID.randomUUID(),
                    tenantId,
                    command.integrationKey().trim().toUpperCase(),
                    command.providerType(),
                    command.displayName().trim(),
                    command.enabled() ? IntegrationStatus.ACTIVE : IntegrationStatus.INACTIVE,
                    now,
                    now);
            return integrationHubRepository.saveDefinition(definition).map(this::toView);
        });
    }

    @Override
    public Mono<IntegrationEndpointView> registerEndpoint(RegisterIntegrationEndpointCommand command) {
        validateEndpoint(command);

        return requireTenant().flatMap(tenantId -> integrationHubRepository.findDefinitionById(tenantId, command.definitionId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_DEFINITION_NOT_FOUND", "Integration definition not found")))
                .flatMap(definition -> {
                    Instant now = Instant.now();
                    IntegrationEndpoint endpoint = new IntegrationEndpoint(
                            UUID.randomUUID(),
                            definition.id(),
                            tenantId,
                            command.endpointKey().trim().toUpperCase(),
                            command.baseUrl().trim(),
                            command.authType(),
                            command.configurationJson(),
                            command.enabled() ? IntegrationStatus.ACTIVE : IntegrationStatus.INACTIVE,
                            now,
                            now);
                    return integrationHubRepository.saveEndpoint(endpoint).map(this::toView);
                }));
    }

    @Override
    public Mono<IntegrationExecutionView> execute(ExecuteIntegrationCommand command) {
        validateExecution(command);

        return requireTenant().flatMap(tenantId -> integrationHubRepository.findDefinitionById(tenantId, command.definitionId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_DEFINITION_NOT_FOUND", "Integration definition not found")))
                .zipWith(integrationHubRepository.findEndpointById(tenantId, command.endpointId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_ENDPOINT_NOT_FOUND", "Integration endpoint not found"))))
                .flatMap(tuple -> {
                    IntegrationDefinition definition = tuple.getT1();
                    IntegrationEndpoint endpoint = tuple.getT2();

                    Instant now = Instant.now();
                    IntegrationExecution started = new IntegrationExecution(
                            UUID.randomUUID(),
                            tenantId,
                            definition.id(),
                            endpoint.id(),
                            command.operation().trim(),
                            command.payloadJson(),
                            IntegrationStatus.PENDING,
                            null,
                            null,
                            now,
                            null,
                            now,
                            now);

                    return integrationHubRepository.saveExecution(started)
                            .flatMap(execution -> publishAudit("INTEGRATION_EXECUTION_ATTEMPT", tenantId, command, definition, endpoint, execution)
                                    .then(executeAdapter(definition, endpoint, execution, command.triggeredBy())));
                }));
    }

    @Override
    public Flux<IntegrationExecutionView> recentExecutions(UUID definitionId, int limit) {
        if (definitionId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "DEFINITION_ID_REQUIRED", "Definition id is required"));
        }

        int resolvedLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        return requireTenant().flatMapMany(tenantId -> integrationHubRepository
                .findExecutionsByDefinition(tenantId, definitionId, resolvedLimit)
                .map(this::toView));
    }

    private Mono<IntegrationExecutionView> executeAdapter(
            IntegrationDefinition definition,
            IntegrationEndpoint endpoint,
            IntegrationExecution execution,
            String triggeredBy
    ) {
        return integrationAdapterRegistry.adapterFor(definition.providerType())
                .execute(new IntegrationInvocation(definition, endpoint, execution.operation(), execution.payloadJson()))
                .flatMap(result -> {
                    IntegrationExecution completed = execution.complete(
                            result.status(),
                            result.externalReference(),
                            result.errorMessage(),
                            Instant.now());
                    return integrationHubRepository.updateExecution(completed)
                            .flatMap(updated -> publishAudit(
                                    "INTEGRATION_EXECUTION_" + updated.status().name(),
                                    updated.tenantId(),
                                    new ExecuteIntegrationCommand(
                                            updated.definitionId(),
                                            updated.endpointId(),
                                            updated.operation(),
                                            updated.payloadJson(),
                                            triggeredBy),
                                    definition,
                                    endpoint,
                                    updated)
                                    .thenReturn(updated))
                            .doOnNext(updated -> log.info(
                                    "Integration execution completed tenant={} definition={} endpoint={} status={} externalRef={}",
                                    updated.tenantId(),
                                    updated.definitionId(),
                                    updated.endpointId(),
                                    updated.status(),
                                    updated.externalReference()));
                })
                .onErrorResume(error -> {
                    IntegrationExecution failed = execution.complete(IntegrationStatus.FAILED, null, error.getMessage(), Instant.now());
                    return integrationHubRepository.updateExecution(failed)
                            .flatMap(updated -> publishAudit(
                                    "INTEGRATION_EXECUTION_FAILED",
                                    updated.tenantId(),
                                    new ExecuteIntegrationCommand(
                                            updated.definitionId(),
                                            updated.endpointId(),
                                            updated.operation(),
                                            updated.payloadJson(),
                                            triggeredBy),
                                    definition,
                                    endpoint,
                                    updated)
                                    .thenReturn(updated));
                })
                .map(this::toView);
    }

    private Mono<Void> publishAudit(
            String action,
            String tenantId,
            ExecuteIntegrationCommand command,
            IntegrationDefinition definition,
            IntegrationEndpoint endpoint,
            IntegrationExecution execution
    ) {
        return auditEventPublisher.publish(AuditEvent.of(
                StringUtils.hasText(command.triggeredBy()) ? command.triggeredBy() : "system",
                tenantId,
                action,
                "INTEGRATION_EXECUTION",
                execution.id().toString(),
                Map.of(
                        "definitionId", definition.id().toString(),
                        "providerType", definition.providerType().name(),
                        "endpointId", endpoint.id().toString(),
                        "operation", execution.operation(),
                        "status", execution.status().name())));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateDefinition(RegisterIntegrationDefinitionCommand command) {
        if (!StringUtils.hasText(command.integrationKey())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INTEGRATION_KEY_REQUIRED", "Integration key is required");
        }
        if (command.providerType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PROVIDER_TYPE_REQUIRED", "Provider type is required");
        }
        if (!StringUtils.hasText(command.displayName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DISPLAY_NAME_REQUIRED", "Display name is required");
        }
    }

    private void validateEndpoint(RegisterIntegrationEndpointCommand command) {
        if (command.definitionId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DEFINITION_ID_REQUIRED", "Definition id is required");
        }
        if (!StringUtils.hasText(command.endpointKey())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENDPOINT_KEY_REQUIRED", "Endpoint key is required");
        }
        if (!StringUtils.hasText(command.baseUrl())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "BASE_URL_REQUIRED", "Base URL is required");
        }
    }

    private void validateExecution(ExecuteIntegrationCommand command) {
        if (command.definitionId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DEFINITION_ID_REQUIRED", "Definition id is required");
        }
        if (command.endpointId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ENDPOINT_ID_REQUIRED", "Endpoint id is required");
        }
        if (!StringUtils.hasText(command.operation())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "OPERATION_REQUIRED", "Operation is required");
        }
    }

    private IntegrationDefinitionView toView(IntegrationDefinition definition) {
        return new IntegrationDefinitionView(
                definition.id(),
                definition.tenantId(),
                definition.integrationKey(),
                definition.providerType(),
                definition.displayName(),
                definition.status(),
                definition.createdAt(),
                definition.updatedAt());
    }

    private IntegrationEndpointView toView(IntegrationEndpoint endpoint) {
        return new IntegrationEndpointView(
                endpoint.id(),
                endpoint.definitionId(),
                endpoint.tenantId(),
                endpoint.endpointKey(),
                endpoint.baseUrl(),
                endpoint.authType(),
                endpoint.configurationJson(),
                endpoint.status(),
                endpoint.createdAt(),
                endpoint.updatedAt());
    }

    private IntegrationExecutionView toView(IntegrationExecution execution) {
        return new IntegrationExecutionView(
                execution.id(),
                execution.tenantId(),
                execution.definitionId(),
                execution.endpointId(),
                execution.operation(),
                execution.payloadJson(),
                execution.status(),
                execution.externalReference(),
                execution.errorMessage(),
                execution.attemptedAt(),
                execution.completedAt(),
                execution.createdAt(),
                execution.updatedAt());
    }
}
