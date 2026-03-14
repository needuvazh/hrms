package com.company.hrms.integrationhub.service;

import com.company.hrms.integrationhub.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IntegrationHubModuleApi {

    Mono<IntegrationDefinitionViewDto> registerDefinition(RegisterIntegrationDefinitionCommandDto command);

    Mono<IntegrationEndpointViewDto> registerEndpoint(RegisterIntegrationEndpointCommandDto command);

    Mono<IntegrationExecutionViewDto> execute(ExecuteIntegrationCommandDto command);

    Flux<IntegrationExecutionViewDto> recentExecutions(UUID definitionId, int limit);
}
