package com.company.hrms.integrationhub.model;

import reactor.core.publisher.Mono;

public interface IntegrationAdapter {

    IntegrationProviderType providerType();

    Mono<IntegrationAdapterResultDto> execute(IntegrationInvocationDto invocation);
}
