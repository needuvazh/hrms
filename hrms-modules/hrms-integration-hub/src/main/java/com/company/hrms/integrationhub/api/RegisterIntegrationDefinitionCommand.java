package com.company.hrms.integrationhub.api;

import com.company.hrms.integrationhub.domain.IntegrationProviderType;

public record RegisterIntegrationDefinitionCommand(
        String integrationKey,
        IntegrationProviderType providerType,
        String displayName,
        boolean enabled
) {
}
