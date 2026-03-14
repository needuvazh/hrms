package com.company.hrms.integrationhub.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IntegrationHubRepository {

    Mono<IntegrationDefinition> saveDefinition(IntegrationDefinition definition);

    Mono<IntegrationDefinition> findDefinitionById(String tenantId, UUID definitionId);

    Mono<IntegrationEndpoint> saveEndpoint(IntegrationEndpoint endpoint);

    Mono<IntegrationEndpoint> findEndpointById(String tenantId, UUID endpointId);

    Mono<IntegrationExecution> saveExecution(IntegrationExecution execution);

    Mono<IntegrationExecution> updateExecution(IntegrationExecution execution);

    Flux<IntegrationExecution> findExecutionsByDefinition(String tenantId, UUID definitionId, int limit);
}
