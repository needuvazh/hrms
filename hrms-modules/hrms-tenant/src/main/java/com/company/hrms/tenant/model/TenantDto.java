package com.company.hrms.tenant.model;

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
public class TenantDto {
    private final UUID id;
    private final String tenantCode;
    private final String tenantName;
    private final boolean active;
}
