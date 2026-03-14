package com.company.hrms.workflow.domain;

import java.time.Instant;
import java.util.UUID;

public record WorkflowDefinition(
        UUID id,
        String tenantId,
        String workflowKey,
        String name,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
