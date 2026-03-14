package com.company.hrms.integrationhub.model;

import com.company.hrms.integrationhub.model.IntegrationStatus;
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
public class IntegrationExecutionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID definitionId;
    private final UUID endpointId;
    private final String operation;
    private final String payloadJson;
    private final IntegrationStatus status;
    private final String externalReference;
    private final String errorMessage;
    private final Instant attemptedAt;
    private final Instant completedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
