package com.company.hrms.platform.outbox.service;

import com.company.hrms.platform.outbox.api.OutboxDispatchResult;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxEventHandler;
import com.company.hrms.platform.outbox.domain.OutboxEventStatus;
import com.company.hrms.platform.outbox.domain.OutboxEventStore;
import com.company.hrms.platform.outbox.domain.OutboxStoredEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutboxLifecycleTest {

    @Test
    void publisherPersistsAndDispatcherMarksDispatched() {
        InMemoryOutboxStore store = new InMemoryOutboxStore();
        PersistentOutboxPublisher publisher = new PersistentOutboxPublisher(store);
        DefaultOutboxDispatcher dispatcher = new DefaultOutboxDispatcher(store, List.of(new SuccessHandler()));

        StepVerifier.create(publisher.publish(new OutboxEvent(
                        "default",
                        "EMPLOYEE",
                        "emp-1",
                        "EmployeeCreated",
                        "{\"employeeId\":\"emp-1\"}",
                        Instant.now())))
                .verifyComplete();

        StepVerifier.create(dispatcher.dispatchPending(10))
                .assertNext(result -> assertEquals(OutboxEventStatus.DISPATCHED, result.status()))
                .verifyComplete();

        assertEquals(1, store.events.values().stream().filter(e -> e.status() == OutboxEventStatus.DISPATCHED).count());
    }

    @Test
    void dispatcherMarksFailedOnHandlerError() {
        InMemoryOutboxStore store = new InMemoryOutboxStore();
        PersistentOutboxPublisher publisher = new PersistentOutboxPublisher(store);
        DefaultOutboxDispatcher dispatcher = new DefaultOutboxDispatcher(store, List.of(new FailingHandler()));

        StepVerifier.create(publisher.publish(new OutboxEvent(
                        "default",
                        "LEAVE_REQUEST",
                        "leave-1",
                        "LeaveRequested",
                        "{\"leaveRequestId\":\"leave-1\"}",
                        Instant.now())))
                .verifyComplete();

        StepVerifier.create(dispatcher.dispatchPending(10))
                .assertNext(result -> {
                    assertEquals(OutboxEventStatus.FAILED, result.status());
                })
                .verifyComplete();

        assertEquals(1, store.events.values().stream().filter(e -> e.status() == OutboxEventStatus.FAILED).count());
    }

    static class SuccessHandler implements OutboxEventHandler {
        @Override
        public boolean supports(OutboxEvent event) {
            return true;
        }

        @Override
        public Mono<Void> handle(OutboxEvent event) {
            return Mono.empty();
        }
    }

    static class FailingHandler implements OutboxEventHandler {
        @Override
        public boolean supports(OutboxEvent event) {
            return true;
        }

        @Override
        public Mono<Void> handle(OutboxEvent event) {
            return Mono.error(new IllegalStateException("dispatch failed"));
        }
    }

    static class InMemoryOutboxStore implements OutboxEventStore {
        private final Map<UUID, OutboxStoredEvent> events = new ConcurrentHashMap<>();

        @Override
        public Mono<OutboxStoredEvent> save(OutboxStoredEvent event) {
            events.put(event.id(), event);
            return Mono.just(event);
        }

        @Override
        public Flux<OutboxStoredEvent> findPendingBatch(int limit) {
            List<OutboxStoredEvent> data = new ArrayList<>(events.values());
            return Flux.fromIterable(data)
                    .filter(event -> event.status() == OutboxEventStatus.PENDING || event.status() == OutboxEventStatus.FAILED)
                    .take(limit);
        }

        @Override
        public Mono<OutboxStoredEvent> update(OutboxStoredEvent event) {
            events.put(event.id(), event);
            return Mono.just(event);
        }

        @Override
        public Mono<OutboxStoredEvent> findById(UUID eventId) {
            return Mono.justOrEmpty(events.get(eventId));
        }
    }
}
