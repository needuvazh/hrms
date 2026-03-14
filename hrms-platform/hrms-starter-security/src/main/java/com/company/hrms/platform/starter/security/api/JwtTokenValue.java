package com.company.hrms.platform.starter.security.api;

import java.time.Instant;

public record JwtTokenValue(
        String tokenValue,
        Instant issuedAt,
        Instant expiresAt
) {
}
