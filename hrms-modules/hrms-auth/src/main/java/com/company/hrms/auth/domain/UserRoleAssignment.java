package com.company.hrms.auth.domain;

import java.util.UUID;

public record UserRoleAssignment(
        UUID userId,
        UUID roleId,
        String tenantId
) {
}
