package com.company.hrms.integrationhub.domain;

import java.time.Instant;
import java.util.UUID;

public record IntegrationDefinition(
        UUID id,
        String tenantId,
        String integrationKey,
        IntegrationProviderType providerType,
        String displayName,
        IntegrationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
