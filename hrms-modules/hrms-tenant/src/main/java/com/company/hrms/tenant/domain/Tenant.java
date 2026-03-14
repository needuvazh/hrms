package com.company.hrms.tenant.domain;

import java.util.UUID;

public record Tenant(
        UUID id,
        String tenantCode,
        String tenantName,
        boolean active
) {
}
