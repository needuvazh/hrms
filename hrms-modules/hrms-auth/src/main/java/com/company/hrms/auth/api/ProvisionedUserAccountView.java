package com.company.hrms.auth.api;

import java.util.UUID;

public record ProvisionedUserAccountView(
        UUID userId,
        String tenantId,
        String username,
        String email,
        String roleCode
) {
}
