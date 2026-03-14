package com.company.hrms.platform.audit.infrastructure;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Testcontainers(disabledWithoutDocker = true)
class R2dbcAuditEventRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hrms")
            .withUsername("hrms")
            .withPassword("hrms");

    private R2dbcAuditEventRepository repository;
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        ConnectionFactory connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(HOST, postgres.getHost())
                .option(PORT, postgres.getFirstMappedPort())
                .option(DATABASE, postgres.getDatabaseName())
                .option(USER, postgres.getUsername())
                .option(PASSWORD, postgres.getPassword())
                .build());
        databaseClient = DatabaseClient.create(connectionFactory);
        repository = new R2dbcAuditEventRepository(databaseClient, new ObjectMapper());

        StepVerifier.create(databaseClient.sql("CREATE SCHEMA IF NOT EXISTS audit").then()).verifyComplete();

        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS audit.audit_event_log (
                            id BIGSERIAL PRIMARY KEY,
                            actor VARCHAR(128) NOT NULL,
                            tenant_id VARCHAR(64) NOT NULL,
                            action VARCHAR(128) NOT NULL,
                            target_type VARCHAR(128) NOT NULL,
                            target_id VARCHAR(128) NOT NULL,
                            module_name VARCHAR(64) NOT NULL,
                            entity_version BIGINT,
                            changed_fields JSONB NOT NULL DEFAULT '[]'::jsonb,
                            old_values JSONB NOT NULL DEFAULT '{}'::jsonb,
                            new_values JSONB NOT NULL DEFAULT '{}'::jsonb,
                            changed_by_actor_type VARCHAR(64) NOT NULL,
                            changed_by_actor_id VARCHAR(128) NOT NULL,
                            approved_by_actor_id VARCHAR(128),
                            change_reason TEXT,
                            source_service VARCHAR(128) NOT NULL,
                            source_event_id VARCHAR(128),
                            request_id VARCHAR(128),
                            transaction_id VARCHAR(128),
                            workflow_id VARCHAR(128),
                            correlation_id VARCHAR(128),
                            event_timestamp TIMESTAMPTZ NOT NULL,
                            metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
                            legal_hold_flag BOOLEAN NOT NULL DEFAULT FALSE,
                            ingested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """).then()).verifyComplete();

        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS audit.person_audit_history (
                            id BIGSERIAL PRIMARY KEY,
                            audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
                            tenant_id VARCHAR(64) NOT NULL,
                            person_id UUID NOT NULL,
                            action_type VARCHAR(64) NOT NULL,
                            effective_at TIMESTAMPTZ NOT NULL,
                            old_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
                            new_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """).then()).verifyComplete();

        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS audit.recruitment_audit_history (
                            id BIGSERIAL PRIMARY KEY,
                            audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
                            tenant_id VARCHAR(64) NOT NULL,
                            candidate_id UUID NOT NULL,
                            application_id UUID,
                            action_type VARCHAR(64) NOT NULL,
                            status_from VARCHAR(64),
                            status_to VARCHAR(64),
                            details JSONB NOT NULL DEFAULT '{}'::jsonb,
                            occurred_at TIMESTAMPTZ NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """).then()).verifyComplete();

        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS audit.employee_audit_history (
                            id BIGSERIAL PRIMARY KEY,
                            audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
                            tenant_id VARCHAR(64) NOT NULL,
                            employee_id UUID NOT NULL,
                            person_id UUID,
                            action_type VARCHAR(64) NOT NULL,
                            old_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
                            new_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
                            occurred_at TIMESTAMPTZ NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """).then()).verifyComplete();

        StepVerifier.create(databaseClient.sql("TRUNCATE TABLE audit.person_audit_history, audit.recruitment_audit_history, audit.employee_audit_history, audit.audit_event_log RESTART IDENTITY CASCADE").then()).verifyComplete();
    }

    @Test
    void writesCanonicalAndModuleSpecificAuditHistoryRows() {
        AuditEvent personEvent = AuditEvent.detailed(
                "admin",
                "default",
                "PERSON_CREATED",
                "PERSON",
                "51111111-1111-1111-1111-111111111111",
                "person",
                1L,
                List.of("firstName", "email"),
                Map.of(),
                Map.of("firstName", "Aisha", "email", "aisha@example.com"),
                "user",
                "admin",
                null,
                "New person onboarding",
                "hrms-person",
                "evt-1",
                "req-1",
                "tx-1",
                "wf-1",
                "corr-1",
                Map.of("region", "OM"),
                false);

        AuditEvent recruitmentEvent = AuditEvent.detailed(
                "recruiter",
                "default",
                "CANDIDATE_STATUS_UPDATED",
                "CANDIDATE",
                "61111111-1111-1111-1111-111111111111",
                "recruitment",
                2L,
                List.of("status"),
                Map.of("status", "INTERVIEW"),
                Map.of("status", "OFFER_ACCEPTED"),
                "user",
                "recruiter",
                "manager",
                "Interview passed",
                "hrms-recruitment",
                "evt-2",
                "req-2",
                "tx-2",
                "wf-2",
                "corr-2",
                Map.of("pipeline", "engineering"),
                false);

        AuditEvent employeeEvent = AuditEvent.detailed(
                "system",
                "default",
                "EMPLOYEE_CREATED",
                "EMPLOYEE",
                "41111111-1111-1111-1111-111111111111",
                "employee",
                1L,
                List.of("employeeCode", "personId"),
                Map.of(),
                Map.of("employeeCode", "EMP-1001", "personId", "51111111-1111-1111-1111-111111111111"),
                "system",
                "system",
                null,
                "CandidateDto hired",
                "hrms-employee",
                "evt-3",
                "req-3",
                "tx-3",
                "wf-3",
                "corr-3",
                Map.of("channel", "recruitment"),
                false);

        StepVerifier.create(repository.append(personEvent)).verifyComplete();
        StepVerifier.create(repository.append(recruitmentEvent)).verifyComplete();
        StepVerifier.create(repository.append(employeeEvent)).verifyComplete();

        StepVerifier.create(databaseClient.sql("SELECT COUNT(*) FROM audit.audit_event_log")
                        .map((row, metadata) -> row.get(0, Long.class))
                        .one())
                .assertNext(count -> assertEquals(3L, count))
                .verifyComplete();

        StepVerifier.create(databaseClient.sql("SELECT COUNT(*) FROM audit.person_audit_history")
                        .map((row, metadata) -> row.get(0, Long.class))
                        .one())
                .assertNext(count -> assertEquals(1L, count))
                .verifyComplete();

        StepVerifier.create(databaseClient.sql("SELECT status_from, status_to FROM audit.recruitment_audit_history")
                        .map((row, metadata) -> row.get("status_from", String.class) + ":" + row.get("status_to", String.class))
                        .one())
                .assertNext(status -> assertEquals("INTERVIEW:OFFER_ACCEPTED", status))
                .verifyComplete();

        StepVerifier.create(databaseClient.sql("SELECT person_id FROM audit.employee_audit_history")
                        .map((row, metadata) -> row.get("person_id", java.util.UUID.class).toString())
                        .one())
                .assertNext(personId -> assertEquals("51111111-1111-1111-1111-111111111111", personId))
                .verifyComplete();
    }
}
