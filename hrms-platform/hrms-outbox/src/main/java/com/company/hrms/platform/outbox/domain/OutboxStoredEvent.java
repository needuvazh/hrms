package com.company.hrms.platform.outbox.domain;

import java.time.Instant;
import java.util.UUID;

public record OutboxStoredEvent(
        UUID id,
        String tenantId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        OutboxEventStatus status,
        int attempts,
        String lastError,
        Instant occurredAt,
        Instant availableAt,
        Instant dispatchedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public OutboxStoredEvent markDispatched(Instant at) {
        return new OutboxStoredEvent(
                id,
                tenantId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.DISPATCHED,
                attempts + 1,
                null,
                occurredAt,
                availableAt,
                at,
                createdAt,
                at);
    }

    public OutboxStoredEvent markFailed(String errorMessage, Instant at) {
        return new OutboxStoredEvent(
                id,
                tenantId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.FAILED,
                attempts + 1,
                errorMessage,
                occurredAt,
                availableAt,
                dispatchedAt,
                createdAt,
                at);
    }
}
