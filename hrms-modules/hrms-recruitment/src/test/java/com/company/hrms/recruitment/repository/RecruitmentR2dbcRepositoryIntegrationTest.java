package com.company.hrms.recruitment.repository;

import com.company.hrms.recruitment.model.*;

import com.company.hrms.recruitment.model.CandidateStatus;
import com.company.hrms.recruitment.model.CandidateDto;
import io.r2dbc.spi.ConnectionFactoryOptions;
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
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Testcontainers(disabledWithoutDocker = true)
class RecruitmentR2dbcRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hrms")
            .withUsername("hrms")
            .withPassword("hrms");

    private RecruitmentR2dbcRepository repository;
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
        repository = new RecruitmentR2dbcRepository(databaseClient);

        StepVerifier.create(databaseClient.sql("CREATE SCHEMA IF NOT EXISTS recruitment").then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS recruitment.candidates (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(64) NOT NULL,
                            person_id UUID NOT NULL,
                            candidate_code VARCHAR(64) NOT NULL,
                            first_name VARCHAR(128) NOT NULL,
                            last_name VARCHAR(128),
                            email VARCHAR(255) NOT NULL,
                            job_posting_code VARCHAR(64),
                            status VARCHAR(32) NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL,
                            CONSTRAINT uq_recruitment_candidates_code UNIQUE (tenant_id, candidate_code)
                        )
                        """).then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS recruitment.candidate_status_history (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(64) NOT NULL,
                            candidate_id UUID NOT NULL,
                            status VARCHAR(32) NOT NULL,
                            reason TEXT,
                            changed_at TIMESTAMPTZ NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """).then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("TRUNCATE TABLE recruitment.candidate_status_history, recruitment.candidates").then()).verifyComplete();
    }

    @Test
    void savesUpdatesAndWritesStatusHistory() {
        Instant now = Instant.now();
        CandidateDto candidate = new CandidateDto(
                UUID.randomUUID(),
                "default",
                UUID.randomUUID(),
                "CAN-1001",
                "Aisha",
                "Khan",
                "aisha@example.com",
                "ENG-001",
                CandidateStatus.APPLIED,
                now,
                now);

        StepVerifier.create(repository.save(candidate))
                .assertNext(saved -> assertEquals(CandidateStatus.APPLIED, saved.status()))
                .verifyComplete();

        StepVerifier.create(repository.updateStatus(candidate.id(), CandidateStatus.OFFER_ACCEPTED, "Interview passed", "default"))
                .assertNext(updated -> assertEquals(CandidateStatus.OFFER_ACCEPTED, updated.status()))
                .verifyComplete();

        StepVerifier.create(databaseClient.sql("SELECT COUNT(*) FROM recruitment.candidate_status_history WHERE candidate_id = :candidateId")
                        .bind("candidateId", candidate.id())
                        .map((row, metadata) -> row.get(0, Long.class))
                        .one())
                .assertNext(count -> assertEquals(1L, count))
                .verifyComplete();
    }
}
