package com.company.hrms.workflow.api;

public record StartWorkflowCommand(
        String workflowKey,
        String targetType,
        String targetId,
        String actor,
        String comments
) {
}
