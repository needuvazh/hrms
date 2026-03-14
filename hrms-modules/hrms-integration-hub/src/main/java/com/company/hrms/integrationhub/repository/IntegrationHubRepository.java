package com.company.hrms.integrationhub.repository;

import com.company.hrms.integrationhub.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IntegrationHubRepository {

    Mono<IntegrationDefinitionDto> saveDefinition(IntegrationDefinitionDto definition);

    Mono<IntegrationDefinitionDto> findDefinitionById(String tenantId, UUID definitionId);

    Mono<IntegrationEndpointDto> saveEndpoint(IntegrationEndpointDto endpoint);

    Mono<IntegrationEndpointDto> findEndpointById(String tenantId, UUID endpointId);

    Mono<IntegrationExecutionDto> saveExecution(IntegrationExecutionDto execution);

    Mono<IntegrationExecutionDto> updateExecution(IntegrationExecutionDto execution);

    Flux<IntegrationExecutionDto> findExecutionsByDefinition(String tenantId, UUID definitionId, int limit);
}
