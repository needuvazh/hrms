package com.company.hrms.workflow.api;

import java.time.Instant;
import java.util.UUID;

public record WorkflowStepView(
        UUID id,
        UUID workflowInstanceId,
        int stepOrder,
        String actor,
        WorkflowAction action,
        String comments,
        Instant actedAt,
        Instant createdAt
) {
}
