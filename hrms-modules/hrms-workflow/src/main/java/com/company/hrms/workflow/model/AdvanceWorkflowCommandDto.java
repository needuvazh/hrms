package com.company.hrms.workflow.model;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AdvanceWorkflowCommandDto {
    private final UUID workflowInstanceId;
    private final WorkflowAction action;
    private final String actor;
    private final String comments;
}
