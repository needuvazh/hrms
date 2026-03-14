package com.company.hrms.integrationhub.domain;

import java.time.Instant;
import java.util.UUID;

public record IntegrationExecution(
        UUID id,
        String tenantId,
        UUID definitionId,
        UUID endpointId,
        String operation,
        String payloadJson,
        IntegrationStatus status,
        String externalReference,
        String errorMessage,
        Instant attemptedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public IntegrationExecution complete(IntegrationStatus finalStatus, String externalReference, String errorMessage, Instant at) {
        return new IntegrationExecution(
                id,
                tenantId,
                definitionId,
                endpointId,
                operation,
                payloadJson,
                finalStatus,
                externalReference,
                errorMessage,
                attemptedAt,
                at,
                createdAt,
                at);
    }
}
