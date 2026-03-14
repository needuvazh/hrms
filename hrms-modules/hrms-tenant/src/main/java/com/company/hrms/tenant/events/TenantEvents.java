package com.company.hrms.tenant.events;

import java.time.Instant;
import java.util.UUID;

public record TenantEvents(
        UUID tenantId,
        String eventType,
        Instant occurredAt
) {
}
