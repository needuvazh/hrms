package com.company.hrms.platform.outbox.api;

import java.time.Instant;

public record OutboxEvent(
        String tenantId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        Instant occurredAt
) {
}
