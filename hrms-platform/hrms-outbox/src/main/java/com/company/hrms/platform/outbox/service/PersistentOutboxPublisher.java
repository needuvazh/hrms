package com.company.hrms.platform.outbox.service;

import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.outbox.domain.OutboxEventStatus;
import com.company.hrms.platform.outbox.domain.OutboxEventStore;
import com.company.hrms.platform.outbox.domain.OutboxStoredEvent;
import java.time.Instant;
import java.util.UUID;
import reactor.core.publisher.Mono;

public class PersistentOutboxPublisher implements OutboxPublisher {

    private final OutboxEventStore outboxEventStore;

    public PersistentOutboxPublisher(OutboxEventStore outboxEventStore) {
        this.outboxEventStore = outboxEventStore;
    }

    @Override
    public Mono<Void> publish(OutboxEvent event) {
        Instant now = Instant.now();
        OutboxStoredEvent storedEvent = new OutboxStoredEvent(
                UUID.randomUUID(),
                event.tenantId(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.payload(),
                OutboxEventStatus.PENDING,
                0,
                null,
                event.occurredAt() == null ? now : event.occurredAt(),
                now,
                null,
                now,
                now);
        return outboxEventStore.save(storedEvent).then();
    }
}
