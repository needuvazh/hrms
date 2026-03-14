package com.company.hrms.platform.outbox.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxEventStore {

    Mono<OutboxStoredEvent> save(OutboxStoredEvent event);

    Flux<OutboxStoredEvent> findPendingBatch(int limit);

    Mono<OutboxStoredEvent> update(OutboxStoredEvent event);

    Mono<OutboxStoredEvent> findById(UUID eventId);
}
