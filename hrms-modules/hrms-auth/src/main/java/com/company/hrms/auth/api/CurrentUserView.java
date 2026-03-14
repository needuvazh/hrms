package com.company.hrms.auth.api;

import java.util.Set;
import java.util.UUID;

public record CurrentUserView(
        UUID userId,
        String username,
        String tenantId,
        Set<String> roles,
        Set<String> permissions
) {
}
