package com.company.hrms.workflow.model;

import java.time.Instant;
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
public class WorkflowStepDto {
    private final UUID id;
    private final UUID workflowInstanceId;
    private final int stepOrder;
    private final String actor;
    private final WorkflowAction action;
    private final String comments;
    private final Instant actedAt;
    private final Instant createdAt;
}
