package com.company.hrms.person.repository;

import com.company.hrms.person.model.*;

import com.company.hrms.person.model.PersonDto;
import com.company.hrms.person.repository.PersonRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PersonR2dbcRepository implements PersonRepository {

    private final DatabaseClient databaseClient;

    public PersonR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<PersonDto> save(PersonDto person) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO person.persons(
                            id, tenant_id, person_code, first_name, last_name, email, mobile, country_code, nationality_code, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :personCode, :firstName, :lastName, :email, :mobile, :countryCode, :nationalityCode, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, person_code, first_name, last_name, email, mobile, country_code, nationality_code, created_at, updated_at
                        """)
                .bind("id", person.id())
                .bind("tenantId", person.tenantId())
                .bind("personCode", person.personCode())
                .bind("firstName", person.firstName())
                .bind("lastName", person.lastName())
                .bind("email", person.email())
                .bind("countryCode", person.countryCode())
                .bind("createdAt", person.createdAt())
                .bind("updatedAt", person.updatedAt());

        spec = bindNullable(spec, "mobile", person.mobile(), String.class);
        spec = bindNullable(spec, "nationalityCode", person.nationalityCode(), String.class);

        return spec
                .map((row, metadata) -> mapPerson(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("mobile", String.class),
                        row.get("country_code", String.class),
                        row.get("nationality_code", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PersonDto> findById(UUID personId, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, person_code, first_name, last_name, email, mobile, country_code, nationality_code, created_at, updated_at
                        FROM person.persons
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        """)
                .bind("id", personId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapPerson(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("mobile", String.class),
                        row.get("country_code", String.class),
                        row.get("nationality_code", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<PersonDto> search(String searchQuery, int limit, int offset, String tenantId) {
        String likeQuery = StringUtils.hasText(searchQuery) ? "%" + searchQuery.trim() + "%" : "%";

        return databaseClient.sql("""
                        SELECT id, tenant_id, person_code, first_name, last_name, email, mobile, country_code, nationality_code, created_at, updated_at
                        FROM person.persons
                        WHERE tenant_id = :tenantId
                          AND (
                            lower(first_name) LIKE lower(:searchQuery)
                            OR lower(last_name) LIKE lower(:searchQuery)
                            OR lower(email) LIKE lower(:searchQuery)
                            OR lower(person_code) LIKE lower(:searchQuery)
                          )
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("tenantId", tenantId)
                .bind("searchQuery", likeQuery)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, metadata) -> mapPerson(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("person_code", String.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("mobile", String.class),
                        row.get("country_code", String.class),
                        row.get("nationality_code", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private PersonDto mapPerson(
            UUID id,
            String tenantId,
            String personCode,
            String firstName,
            String lastName,
            String email,
            String mobile,
            String countryCode,
            String nationalityCode,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new PersonDto(id, tenantId, personCode, firstName, lastName, email, mobile, countryCode, nationalityCode, createdAt, updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
