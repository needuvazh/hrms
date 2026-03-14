package com.company.hrms.tenant.api;

import java.util.UUID;

public record TenantView(
        UUID id,
        String tenantCode,
        String tenantName,
        boolean active
) {
}
