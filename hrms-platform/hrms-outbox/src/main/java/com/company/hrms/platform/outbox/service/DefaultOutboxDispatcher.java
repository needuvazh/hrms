package com.company.hrms.platform.outbox.service;

import com.company.hrms.platform.outbox.api.OutboxDispatchResult;
import com.company.hrms.platform.outbox.api.OutboxDispatcher;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxEventHandler;
import com.company.hrms.platform.outbox.domain.OutboxEventStore;
import com.company.hrms.platform.outbox.domain.OutboxStoredEvent;
import java.time.Instant;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultOutboxDispatcher implements OutboxDispatcher {

    private final OutboxEventStore outboxEventStore;
    private final List<OutboxEventHandler> outboxEventHandlers;

    public DefaultOutboxDispatcher(OutboxEventStore outboxEventStore, List<OutboxEventHandler> outboxEventHandlers) {
        this.outboxEventStore = outboxEventStore;
        this.outboxEventHandlers = outboxEventHandlers;
    }

    @Override
    public Flux<OutboxDispatchResult> dispatchPending(int limit) {
        int batchSize = limit > 0 ? limit : 100;
        return outboxEventStore.findPendingBatch(batchSize)
                .concatMap(this::dispatchSingle);
    }

    private Mono<OutboxDispatchResult> dispatchSingle(OutboxStoredEvent storedEvent) {
        OutboxEvent event = new OutboxEvent(
                storedEvent.tenantId(),
                storedEvent.aggregateType(),
                storedEvent.aggregateId(),
                storedEvent.eventType(),
                storedEvent.payload(),
                storedEvent.occurredAt());

        return Flux.fromIterable(outboxEventHandlers)
                .filter(handler -> handler.supports(event))
                .switchIfEmpty(Mono.error(new IllegalStateException("No outbox handler registered for event type " + event.eventType())))
                .concatMap(handler -> handler.handle(event))
                .then(Mono.defer(() -> outboxEventStore.update(storedEvent.markDispatched(Instant.now()))))
                .map(updated -> new OutboxDispatchResult(updated.id(), updated.status(), null))
                .onErrorResume(error -> outboxEventStore.update(storedEvent.markFailed(error.getMessage(), Instant.now()))
                        .map(updated -> new OutboxDispatchResult(updated.id(), updated.status(), updated.lastError())));
    }
}
