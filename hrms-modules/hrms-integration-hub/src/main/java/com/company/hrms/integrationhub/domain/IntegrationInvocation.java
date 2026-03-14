package com.company.hrms.integrationhub.domain;

public record IntegrationInvocation(
        IntegrationDefinition definition,
        IntegrationEndpoint endpoint,
        String operation,
        String payloadJson
) {
}
