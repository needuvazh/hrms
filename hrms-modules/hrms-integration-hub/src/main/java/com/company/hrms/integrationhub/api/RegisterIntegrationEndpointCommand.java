package com.company.hrms.integrationhub.api;

import java.util.UUID;

public record RegisterIntegrationEndpointCommand(
        UUID definitionId,
        String endpointKey,
        String baseUrl,
        String authType,
        String configurationJson,
        boolean enabled
) {
}
