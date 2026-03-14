package com.company.hrms.auth.model;

import java.util.Set;
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
public class CurrentUserViewDto {
    private final UUID userId;
    private final String username;
    private final String tenantId;
    private final Set<String> roles;
    private final Set<String> permissions;
}
