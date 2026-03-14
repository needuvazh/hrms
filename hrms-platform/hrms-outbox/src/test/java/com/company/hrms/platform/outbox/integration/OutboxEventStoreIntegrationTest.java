package com.company.hrms.platform.outbox.integration;

import com.company.hrms.platform.outbox.domain.OutboxEventStatus;
import com.company.hrms.platform.outbox.domain.OutboxStoredEvent;
import com.company.hrms.platform.outbox.infrastructure.R2dbcOutboxEventStore;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
class OutboxEventStoreIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hrms")
            .withUsername("hrms")
            .withPassword("hrms");

    private R2dbcOutboxEventStore store;

    @BeforeEach
    void setUp() {
        String r2dbcUrl = "r2dbc:postgresql://%s:%d/%s".formatted(
                postgres.getHost(),
                postgres.getFirstMappedPort(),
                postgres.getDatabaseName());
        ConnectionFactory connectionFactory = ConnectionFactories.get(r2dbcUrl + "?user=" + postgres.getUsername() + "&password=" + postgres.getPassword());
        DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);

        this.store = new R2dbcOutboxEventStore(databaseClient);

        StepVerifier.create(databaseClient.sql("CREATE SCHEMA IF NOT EXISTS outbox").then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS outbox.outbox_events (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(64) NOT NULL,
                            aggregate_type VARCHAR(100) NOT NULL,
                            aggregate_id VARCHAR(100) NOT NULL,
                            event_type VARCHAR(120) NOT NULL,
                            payload TEXT NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            attempts INT NOT NULL DEFAULT 0,
                            last_error TEXT,
                            occurred_at TIMESTAMPTZ NOT NULL,
                            available_at TIMESTAMPTZ NOT NULL,
                            dispatched_at TIMESTAMPTZ,
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL
                        )
                        """).then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("TRUNCATE TABLE outbox.outbox_events").then()).verifyComplete();
    }

    @Test
    void persistsAndUpdatesOutboxEvent() {
        Instant now = Instant.now();
        OutboxStoredEvent event = new OutboxStoredEvent(
                UUID.randomUUID(),
                "default",
                "EMPLOYEE",
                "emp-1",
                "EmployeeCreated",
                "{\"employeeId\":\"emp-1\"}",
                OutboxEventStatus.PENDING,
                0,
                null,
                now,
                now,
                null,
                now,
                now);

        StepVerifier.create(store.save(event)).assertNext(saved -> assertEquals(OutboxEventStatus.PENDING, saved.status())).verifyComplete();

        OutboxStoredEvent dispatched = event.markDispatched(Instant.now());
        StepVerifier.create(store.update(dispatched))
                .assertNext(updated -> assertEquals(OutboxEventStatus.DISPATCHED, updated.status()))
                .verifyComplete();

        StepVerifier.create(store.findById(event.id()))
                .assertNext(found -> assertEquals(OutboxEventStatus.DISPATCHED, found.status()))
                .verifyComplete();
    }
}
