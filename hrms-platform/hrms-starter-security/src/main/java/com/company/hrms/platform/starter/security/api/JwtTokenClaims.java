package com.company.hrms.platform.starter.security.api;

import java.util.Set;
import java.util.UUID;

public record JwtTokenClaims(
        UUID userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) {
}
