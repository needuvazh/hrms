package com.company.hrms.platform.outbox.infrastructure;

import com.company.hrms.platform.outbox.domain.OutboxEventStatus;
import com.company.hrms.platform.outbox.domain.OutboxEventStore;
import com.company.hrms.platform.outbox.domain.OutboxStoredEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class R2dbcOutboxEventStore implements OutboxEventStore {

    private final DatabaseClient databaseClient;

    public R2dbcOutboxEventStore(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<OutboxStoredEvent> save(OutboxStoredEvent event) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO outbox.outbox_events(
                            id, tenant_id, aggregate_type, aggregate_id, event_type, payload,
                            status, attempts, last_error, occurred_at, available_at,
                            dispatched_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :aggregateType, :aggregateId, :eventType, :payload,
                            :status, :attempts, :lastError, :occurredAt, :availableAt,
                            :dispatchedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, aggregate_type, aggregate_id, event_type, payload,
                                  status, attempts, last_error, occurred_at, available_at,
                                  dispatched_at, created_at, updated_at
                        """)
                .bind("id", event.id())
                .bind("tenantId", event.tenantId())
                .bind("aggregateType", event.aggregateType())
                .bind("aggregateId", event.aggregateId())
                .bind("eventType", event.eventType())
                .bind("payload", event.payload())
                .bind("status", event.status().name())
                .bind("attempts", event.attempts())
                .bind("occurredAt", event.occurredAt())
                .bind("availableAt", event.availableAt())
                .bind("createdAt", event.createdAt())
                .bind("updatedAt", event.updatedAt());

        spec = bindNullable(spec, "lastError", event.lastError(), String.class);
        spec = bindNullable(spec, "dispatchedAt", event.dispatchedAt(), Instant.class);

        return spec.map((row, metadata) -> map(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("aggregate_type", String.class),
                        row.get("aggregate_id", String.class),
                        row.get("event_type", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("attempts", Integer.class),
                        row.get("last_error", String.class),
                        row.get("occurred_at", Instant.class),
                        row.get("available_at", Instant.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<OutboxStoredEvent> findPendingBatch(int limit) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, aggregate_type, aggregate_id, event_type, payload,
                               status, attempts, last_error, occurred_at, available_at,
                               dispatched_at, created_at, updated_at
                        FROM outbox.outbox_events
                        WHERE status IN ('PENDING', 'FAILED')
                          AND available_at <= NOW()
                        ORDER BY occurred_at ASC
                        LIMIT :limit
                        """)
                .bind("limit", limit)
                .map((row, metadata) -> map(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("aggregate_type", String.class),
                        row.get("aggregate_id", String.class),
                        row.get("event_type", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("attempts", Integer.class),
                        row.get("last_error", String.class),
                        row.get("occurred_at", Instant.class),
                        row.get("available_at", Instant.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<OutboxStoredEvent> update(OutboxStoredEvent event) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE outbox.outbox_events
                        SET status = :status,
                            attempts = :attempts,
                            last_error = :lastError,
                            dispatched_at = :dispatchedAt,
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id, tenant_id, aggregate_type, aggregate_id, event_type, payload,
                                  status, attempts, last_error, occurred_at, available_at,
                                  dispatched_at, created_at, updated_at
                        """)
                .bind("id", event.id())
                .bind("status", event.status().name())
                .bind("attempts", event.attempts())
                .bind("updatedAt", event.updatedAt());

        spec = bindNullable(spec, "lastError", event.lastError(), String.class);
        spec = bindNullable(spec, "dispatchedAt", event.dispatchedAt(), Instant.class);

        return spec.map((row, metadata) -> map(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("aggregate_type", String.class),
                        row.get("aggregate_id", String.class),
                        row.get("event_type", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("attempts", Integer.class),
                        row.get("last_error", String.class),
                        row.get("occurred_at", Instant.class),
                        row.get("available_at", Instant.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<OutboxStoredEvent> findById(UUID eventId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, aggregate_type, aggregate_id, event_type, payload,
                               status, attempts, last_error, occurred_at, available_at,
                               dispatched_at, created_at, updated_at
                        FROM outbox.outbox_events
                        WHERE id = :id
                        """)
                .bind("id", eventId)
                .map((row, metadata) -> map(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("aggregate_type", String.class),
                        row.get("aggregate_id", String.class),
                        row.get("event_type", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("attempts", Integer.class),
                        row.get("last_error", String.class),
                        row.get("occurred_at", Instant.class),
                        row.get("available_at", Instant.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    private OutboxStoredEvent map(
            UUID id,
            String tenantId,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            String status,
            Integer attempts,
            String lastError,
            Instant occurredAt,
            Instant availableAt,
            Instant dispatchedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new OutboxStoredEvent(
                id,
                tenantId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxEventStatus.valueOf(status),
                attempts == null ? 0 : attempts,
                lastError,
                occurredAt,
                availableAt,
                dispatchedAt,
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
