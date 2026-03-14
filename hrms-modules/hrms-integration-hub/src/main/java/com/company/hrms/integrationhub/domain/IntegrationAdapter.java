package com.company.hrms.integrationhub.domain;

import reactor.core.publisher.Mono;

public interface IntegrationAdapter {

    IntegrationProviderType providerType();

    Mono<IntegrationAdapterResult> execute(IntegrationInvocation invocation);
}
