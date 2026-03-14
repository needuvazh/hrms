package com.company.hrms.contracts.workflow;

import java.util.UUID;

public record AdvanceWorkflowCommandDto(
        UUID workflowInstanceId,
        WorkflowAction action,
        String actor,
        String comments
) {
}
