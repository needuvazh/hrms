package com.company.hrms.integrationhub.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IntegrationHubModuleApi {

    Mono<IntegrationDefinitionView> registerDefinition(RegisterIntegrationDefinitionCommand command);

    Mono<IntegrationEndpointView> registerEndpoint(RegisterIntegrationEndpointCommand command);

    Mono<IntegrationExecutionView> execute(ExecuteIntegrationCommand command);

    Flux<IntegrationExecutionView> recentExecutions(UUID definitionId, int limit);
}
