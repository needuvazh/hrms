package com.company.hrms.integrationhub.service.impl;

import com.company.hrms.integrationhub.model.*;
import com.company.hrms.integrationhub.repository.*;
import com.company.hrms.integrationhub.service.*;

import com.company.hrms.integrationhub.model.ExecuteIntegrationCommandDto;
import com.company.hrms.integrationhub.model.IntegrationDefinitionViewDto;
import com.company.hrms.integrationhub.model.IntegrationEndpointViewDto;
import com.company.hrms.integrationhub.model.IntegrationExecutionViewDto;
import com.company.hrms.integrationhub.service.IntegrationHubModuleApi;
import com.company.hrms.integrationhub.model.RegisterIntegrationDefinitionCommandDto;
import com.company.hrms.integrationhub.model.RegisterIntegrationEndpointCommandDto;
import com.company.hrms.integrationhub.model.IntegrationAdapterRegistry;
import com.company.hrms.integrationhub.model.IntegrationDefinitionDto;
import com.company.hrms.integrationhub.model.IntegrationEndpointDto;
import com.company.hrms.integrationhub.model.IntegrationExecutionDto;
import com.company.hrms.integrationhub.repository.IntegrationHubRepository;
import com.company.hrms.integrationhub.model.IntegrationInvocationDto;
import com.company.hrms.integrationhub.model.IntegrationStatus;
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
    public Mono<IntegrationDefinitionViewDto> registerDefinition(RegisterIntegrationDefinitionCommandDto command) {
        validateDefinition(command);

        return requireTenant().flatMap(tenantId -> {
            Instant now = Instant.now();
            IntegrationDefinitionDto definition = new IntegrationDefinitionDto(
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
    public Mono<IntegrationEndpointViewDto> registerEndpoint(RegisterIntegrationEndpointCommandDto command) {
        validateEndpoint(command);

        return requireTenant().flatMap(tenantId -> integrationHubRepository.findDefinitionById(tenantId, command.definitionId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_DEFINITION_NOT_FOUND", "Integration definition not found")))
                .flatMap(definition -> {
                    Instant now = Instant.now();
                    IntegrationEndpointDto endpoint = new IntegrationEndpointDto(
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
    public Mono<IntegrationExecutionViewDto> execute(ExecuteIntegrationCommandDto command) {
        validateExecution(command);

        return requireTenant().flatMap(tenantId -> integrationHubRepository.findDefinitionById(tenantId, command.definitionId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_DEFINITION_NOT_FOUND", "Integration definition not found")))
                .zipWith(integrationHubRepository.findEndpointById(tenantId, command.endpointId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "INTEGRATION_ENDPOINT_NOT_FOUND", "Integration endpoint not found"))))
                .flatMap(tuple -> {
                    IntegrationDefinitionDto definition = tuple.getT1();
                    IntegrationEndpointDto endpoint = tuple.getT2();

                    Instant now = Instant.now();
                    IntegrationExecutionDto started = new IntegrationExecutionDto(
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
    public Flux<IntegrationExecutionViewDto> recentExecutions(UUID definitionId, int limit) {
        if (definitionId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "DEFINITION_ID_REQUIRED", "Definition id is required"));
        }

        int resolvedLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        return requireTenant().flatMapMany(tenantId -> integrationHubRepository
                .findExecutionsByDefinition(tenantId, definitionId, resolvedLimit)
                .map(this::toView));
    }

    private Mono<IntegrationExecutionViewDto> executeAdapter(
            IntegrationDefinitionDto definition,
            IntegrationEndpointDto endpoint,
            IntegrationExecutionDto execution,
            String triggeredBy
    ) {
        return integrationAdapterRegistry.adapterFor(definition.providerType())
                .execute(new IntegrationInvocationDto(definition, endpoint, execution.operation(), execution.payloadJson()))
                .flatMap(result -> {
                    IntegrationExecutionDto completed = execution.complete(
                            result.status(),
                            result.externalReference(),
                            result.errorMessage(),
                            Instant.now());
                    return integrationHubRepository.updateExecution(completed)
                            .flatMap(updated -> publishAudit(
                                    "INTEGRATION_EXECUTION_" + updated.status().name(),
                                    updated.tenantId(),
                                    new ExecuteIntegrationCommandDto(
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
                    IntegrationExecutionDto failed = execution.complete(IntegrationStatus.FAILED, null, error.getMessage(), Instant.now());
                    return integrationHubRepository.updateExecution(failed)
                            .flatMap(updated -> publishAudit(
                                    "INTEGRATION_EXECUTION_FAILED",
                                    updated.tenantId(),
                                    new ExecuteIntegrationCommandDto(
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
            ExecuteIntegrationCommandDto command,
            IntegrationDefinitionDto definition,
            IntegrationEndpointDto endpoint,
            IntegrationExecutionDto execution
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

    private void validateDefinition(RegisterIntegrationDefinitionCommandDto command) {
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

    private void validateEndpoint(RegisterIntegrationEndpointCommandDto command) {
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

    private void validateExecution(ExecuteIntegrationCommandDto command) {
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

    private IntegrationDefinitionViewDto toView(IntegrationDefinitionDto definition) {
        return new IntegrationDefinitionViewDto(
                definition.id(),
                definition.tenantId(),
                definition.integrationKey(),
                definition.providerType(),
                definition.displayName(),
                definition.status(),
                definition.createdAt(),
                definition.updatedAt());
    }

    private IntegrationEndpointViewDto toView(IntegrationEndpointDto endpoint) {
        return new IntegrationEndpointViewDto(
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

    private IntegrationExecutionViewDto toView(IntegrationExecutionDto execution) {
        return new IntegrationExecutionViewDto(
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
