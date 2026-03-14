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
public class RegisterIntegrationEndpointCommandDto {
    private final UUID definitionId;
    private final String endpointKey;
    private final String baseUrl;
    private final String authType;
    private final String configurationJson;
    private final boolean enabled;
}
