package com.company.hrms.contracts.workflow;

public record StartWorkflowCommandDto(
        String workflowKey,
        String targetType,
        String targetId,
        String actor,
        String comments
) {
}
