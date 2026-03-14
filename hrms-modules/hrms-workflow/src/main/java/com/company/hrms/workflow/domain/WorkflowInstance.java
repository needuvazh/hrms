package com.company.hrms.workflow.domain;

import java.time.Instant;
import java.util.UUID;

public record WorkflowInstance(
        UUID id,
        String tenantId,
        UUID workflowDefinitionId,
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

    public WorkflowInstance transition(ApprovalStatus newStatus, String actor, Instant now) {
        return new WorkflowInstance(
                id,
                tenantId,
                workflowDefinitionId,
                workflowKey,
                targetType,
                targetId,
                newStatus,
                requestedBy,
                actor,
                submittedAt,
                now,
                createdAt,
                now);
    }
}
