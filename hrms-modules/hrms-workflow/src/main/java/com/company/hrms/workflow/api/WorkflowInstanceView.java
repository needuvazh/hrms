package com.company.hrms.workflow.api;

import java.time.Instant;
import java.util.UUID;

public record WorkflowInstanceView(
        UUID id,
        String tenantId,
        String workflowKey,
        String targetType,
        String targetId,
        ApprovalStatus status,
        String requestedBy,
        String decidedBy,
        Instant submittedAt,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
