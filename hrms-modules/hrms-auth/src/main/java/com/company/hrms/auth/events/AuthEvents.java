package com.company.hrms.auth.events;

import java.time.Instant;
import java.util.UUID;

public record AuthEvents(
        UUID userId,
        String tenantId,
        String eventType,
        Instant occurredAt
) {
}
