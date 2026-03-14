package com.company.hrms.integrationhub.model;

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
public class IntegrationInvocationDto {
    private final IntegrationDefinitionDto definition;
    private final IntegrationEndpointDto endpoint;
    private final String operation;
    private final String payloadJson;
}
