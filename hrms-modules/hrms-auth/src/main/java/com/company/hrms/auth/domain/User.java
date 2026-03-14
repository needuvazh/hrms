package com.company.hrms.auth.domain;

import java.util.UUID;

public record User(
        UUID id,
        String tenantId,
        String username,
        String email,
        String passwordHash,
        boolean active
) {
}
