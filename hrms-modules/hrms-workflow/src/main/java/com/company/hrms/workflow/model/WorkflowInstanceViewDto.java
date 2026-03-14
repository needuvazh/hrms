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
public class WorkflowInstanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final String workflowKey;
    private final String targetType;
    private final String targetId;
    private final ApprovalStatus status;
    private final String requestedBy;
    private final String decidedBy;
    private final Instant submittedAt;
    private final Instant decidedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
