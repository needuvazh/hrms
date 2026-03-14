package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.*;

import com.company.hrms.employee.model.EmployeeDto;
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
class EmployeeR2dbcRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hrms")
            .withUsername("hrms")
            .withPassword("hrms");

    private EmployeeR2dbcRepository repository;
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
        repository = new EmployeeR2dbcRepository(databaseClient);

        StepVerifier.create(databaseClient.sql("CREATE SCHEMA IF NOT EXISTS employee").then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS employee.employees (
                            id UUID PRIMARY KEY,
                            tenant_id VARCHAR(64) NOT NULL,
                            employee_code VARCHAR(50) NOT NULL,
                            person_id UUID,
                            first_name VARCHAR(100) NOT NULL,
                            last_name VARCHAR(100) NOT NULL,
                            email VARCHAR(200) NOT NULL,
                            department_code VARCHAR(50),
                            job_title VARCHAR(120),
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL
                        )
                        """).then()).verifyComplete();
        StepVerifier.create(databaseClient.sql("TRUNCATE TABLE employee.employees").then()).verifyComplete();
    }

    @Test
    void tenantIsolationIsEnforcedForReadAndSearch() {
        Instant now = Instant.now();
        EmployeeDto tenantAEmployee = new EmployeeDto(
                UUID.randomUUID(),
                "tenant-a",
                "EMP-A",
                "Alice",
                "A",
                "alice@a.hrms",
                "ENG",
                "Engineer",
                now,
                now);
        EmployeeDto tenantBEmployee = new EmployeeDto(
                UUID.randomUUID(),
                "tenant-b",
                "EMP-B",
                "Bob",
                "B",
                "bob@b.hrms",
                "HR",
                "Manager",
                now,
                now);

        StepVerifier.create(repository.save(tenantAEmployee)).expectNextCount(1).verifyComplete();
        StepVerifier.create(repository.save(tenantBEmployee)).expectNextCount(1).verifyComplete();

        StepVerifier.create(repository.findById(tenantAEmployee.id(), "tenant-a"))
                .assertNext(found -> assertEquals("tenant-a", found.tenantId()))
                .verifyComplete();

        StepVerifier.create(repository.findById(tenantAEmployee.id(), "tenant-b"))
                .verifyComplete();

        StepVerifier.create(repository.search("", 20, 0, "tenant-a"))
                .assertNext(found -> assertEquals("tenant-a", found.tenantId()))
                .verifyComplete();
    }
}
