package com.company.hrms.workflow.model;

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
public class StartWorkflowCommandDto {
    private final String workflowKey;
    private final String targetType;
    private final String targetId;
    private final String actor;
    private final String comments;
}
