package com.company.hrms.auth.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AuthenticatedUserViewDto {
    private final UUID id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean superAdmin;
    private final boolean canViewAllTenants;
    private final Set<String> roles;
    private final Set<String> permissions;
    private final Set<String> scopes;
}
