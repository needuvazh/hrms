package com.company.hrms.integrationhub.model;

import com.company.hrms.integrationhub.model.IntegrationProviderType;
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
public class RegisterIntegrationDefinitionCommandDto {
    private final String integrationKey;
    private final IntegrationProviderType providerType;
    private final String displayName;
    private final boolean enabled;
}
