package com.company.hrms.auth.api;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AuthTokenView(
        String accessToken,
        String tokenType,
        Instant issuedAt,
        Instant expiresAt,
        UUID userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) {
}
