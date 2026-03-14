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
public class WorkflowDefinitionDto {
    private final UUID id;
    private final String tenantId;
    private final String workflowKey;
    private final String name;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
