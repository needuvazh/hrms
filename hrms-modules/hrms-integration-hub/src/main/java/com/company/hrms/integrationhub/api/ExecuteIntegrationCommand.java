package com.company.hrms.integrationhub.api;

import java.util.UUID;

public record ExecuteIntegrationCommand(
        UUID definitionId,
        UUID endpointId,
        String operation,
        String payloadJson,
        String triggeredBy
) {
}
