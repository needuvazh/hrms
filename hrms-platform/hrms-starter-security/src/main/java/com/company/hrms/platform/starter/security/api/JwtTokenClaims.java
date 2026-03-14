package com.company.hrms.platform.starter.security.api;

import java.util.Set;
import java.util.UUID;

public record JwtTokenClaims(
        UUID userId,
        String username,
        String email,
        String firstName,
        String lastName,
        String tenantId,
        boolean superAdmin,
        boolean canViewAllTenants,
        Set<String> roles,
        Set<String> permissions,
        Set<String> scopes
) {
}
