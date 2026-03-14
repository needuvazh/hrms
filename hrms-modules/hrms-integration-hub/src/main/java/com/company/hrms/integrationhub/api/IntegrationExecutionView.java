package com.company.hrms.integrationhub.api;

import com.company.hrms.integrationhub.domain.IntegrationStatus;
import java.time.Instant;
import java.util.UUID;

public record IntegrationExecutionView(
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
}
