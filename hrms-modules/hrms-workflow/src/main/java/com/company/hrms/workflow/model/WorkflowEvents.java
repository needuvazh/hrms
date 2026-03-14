package com.company.hrms.workflow.model;

import java.time.Instant;
import java.util.UUID;

public final class WorkflowEvents {

    private WorkflowEvents() {
    }

    public record WorkflowStartedEvent(UUID workflowInstanceId, String tenantId, String workflowKey, Instant occurredAt) {
    }

    public record WorkflowAdvancedEvent(UUID workflowInstanceId, String tenantId, String action, Instant occurredAt) {
    }
}
