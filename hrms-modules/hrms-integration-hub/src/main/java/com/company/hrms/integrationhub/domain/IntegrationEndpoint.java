package com.company.hrms.integrationhub.domain;

import java.time.Instant;
import java.util.UUID;

public record IntegrationEndpoint(
        UUID id,
        UUID definitionId,
        String tenantId,
        String endpointKey,
        String baseUrl,
        String authType,
        String configurationJson,
        IntegrationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
