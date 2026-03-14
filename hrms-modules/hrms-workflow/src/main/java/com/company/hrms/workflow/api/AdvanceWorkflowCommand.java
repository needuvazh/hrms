package com.company.hrms.workflow.api;

import java.util.UUID;

public record AdvanceWorkflowCommand(
        UUID workflowInstanceId,
        WorkflowAction action,
        String actor,
        String comments
) {
}
