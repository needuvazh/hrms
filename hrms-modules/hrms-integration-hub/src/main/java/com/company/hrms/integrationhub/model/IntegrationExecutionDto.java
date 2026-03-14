package com.company.hrms.integrationhub.model;

import java.time.Instant;
import java.util.UUID;

public record IntegrationExecutionDto(
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

    public IntegrationExecutionDto complete(IntegrationStatus finalStatus, String externalReference, String errorMessage, Instant at) {
        return new IntegrationExecutionDto(
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
