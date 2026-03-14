package com.company.hrms.integrationhub.api;

import com.company.hrms.integrationhub.domain.IntegrationProviderType;
import com.company.hrms.integrationhub.domain.IntegrationStatus;
import java.time.Instant;
import java.util.UUID;

public record IntegrationDefinitionView(
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
