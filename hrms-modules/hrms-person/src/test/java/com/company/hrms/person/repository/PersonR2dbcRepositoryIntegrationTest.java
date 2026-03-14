package com.company.hrms.person.repository;

import com.company.hrms.person.model.*;

import com.company.hrms.person.model.PersonDto;
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
class PersonR2dbcRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hrms")
            .withUsername("hrms")
            .withPassword("hrms");

    private PersonR2dbcRepository repository;
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
        repository = new PersonR2dbcRepository(databaseClient);

        StepVerifier.create(databaseClient.sql("CREATE SCHEMA IF NOT EXISTS person").then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS person.persons (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(64) NOT NULL,
                            person_code VARCHAR(64) NOT NULL,
                            first_name VARCHAR(128) NOT NULL,
                            last_name VARCHAR(128),
                            email VARCHAR(255) NOT NULL,
                            mobile VARCHAR(32),
                            country_code VARCHAR(8) NOT NULL,
                            nationality_code VARCHAR(8),
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL,
                            CONSTRAINT uq_person_tenant_code UNIQUE (tenant_id, person_code),
                            CONSTRAINT uq_person_tenant_email UNIQUE (tenant_id, email)
                        )
                        """).then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("TRUNCATE TABLE person.persons").then()).verifyComplete();
    }

    @Test
    void persistsFindsAndFiltersByTenant() {
        Instant now = Instant.now();
        PersonDto tenantAPerson = new PersonDto(
                UUID.randomUUID(),
                "tenant-a",
                "PER-A",
                "Aisha",
                "Khan",
                "aisha@a.hrms",
                "+9681111111",
                "OM",
                "OM",
                now,
                now);
        PersonDto tenantBPerson = new PersonDto(
                UUID.randomUUID(),
                "tenant-b",
                "PER-B",
                "Bilal",
                "Ali",
                "bilal@b.hrms",
                "+9682222222",
                "OM",
                "OM",
                now,
                now);

        StepVerifier.create(repository.save(tenantAPerson)).expectNextCount(1).verifyComplete();
        StepVerifier.create(repository.save(tenantBPerson)).expectNextCount(1).verifyComplete();

        StepVerifier.create(repository.findById(tenantAPerson.id(), "tenant-a"))
                .assertNext(found -> assertEquals("tenant-a", found.tenantId()))
                .verifyComplete();

        StepVerifier.create(repository.findById(tenantAPerson.id(), "tenant-b"))
                .verifyComplete();

        StepVerifier.create(repository.search("aisha", 20, 0, "tenant-a"))
                .assertNext(found -> assertEquals("tenant-a", found.tenantId()))
                .verifyComplete();
    }
}
