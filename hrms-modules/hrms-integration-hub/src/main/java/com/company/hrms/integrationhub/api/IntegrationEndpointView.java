package com.company.hrms.integrationhub.api;

import com.company.hrms.integrationhub.domain.IntegrationStatus;
import java.time.Instant;
import java.util.UUID;

public record IntegrationEndpointView(
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
