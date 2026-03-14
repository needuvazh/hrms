package com.company.hrms.integrationhub.model;

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
public class ExecuteIntegrationCommandDto {
    private final UUID definitionId;
    private final UUID endpointId;
    private final String operation;
    private final String payloadJson;
    private final String triggeredBy;
}
