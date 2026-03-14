package com.company.hrms.workflow.domain;

import java.time.Instant;
import java.util.UUID;

public record WorkflowStep(
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
