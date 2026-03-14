package com.company.hrms.employee.infrastructure.persistence;

import com.company.hrms.employee.domain.Employee;
import com.company.hrms.employee.domain.EmployeeRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class EmployeeR2dbcRepository implements EmployeeRepository {

    private final DatabaseClient databaseClient;

    public EmployeeR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Employee> save(Employee employee) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO employee.employees(
                            id, tenant_id, employee_code, person_id, first_name, last_name, email, department_code, job_title, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :employeeCode, :personId, :firstName, :lastName, :email, :departmentCode, :jobTitle, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, employee_code, person_id, first_name, last_name, email, department_code, job_title, created_at, updated_at
                        """)
                .bind("id", employee.id())
                .bind("tenantId", employee.tenantId())
                .bind("employeeCode", employee.employeeCode())
                .bind("firstName", employee.firstName())
                .bind("lastName", employee.lastName())
                .bind("email", employee.email())
                .bind("createdAt", employee.createdAt())
                .bind("updatedAt", employee.updatedAt());

        spec = bindNullable(spec, "personId", employee.personId(), UUID.class);
        spec = bindNullable(spec, "departmentCode", employee.departmentCode(), String.class);
        spec = bindNullable(spec, "jobTitle", employee.jobTitle(), String.class);

        return spec
                .map((row, metadata) -> mapEmployee(row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_code", String.class),
                        row.get("person_id", UUID.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("department_code", String.class),
                        row.get("job_title", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Employee> findById(UUID employeeId, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_code, person_id, first_name, last_name, email, department_code, job_title, created_at, updated_at
                        FROM employee.employees
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        """)
                .bind("id", employeeId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapEmployee(row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_code", String.class),
                        row.get("person_id", UUID.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("department_code", String.class),
                        row.get("job_title", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<Employee> search(String searchQuery, int limit, int offset, String tenantId) {
        String likeQuery = StringUtils.hasText(searchQuery) ? "%" + searchQuery.trim() + "%" : "%";

        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_code, person_id, first_name, last_name, email, department_code, job_title, created_at, updated_at
                        FROM employee.employees
                        WHERE tenant_id = :tenantId
                          AND (
                            lower(first_name) LIKE lower(:searchQuery)
                            OR lower(last_name) LIKE lower(:searchQuery)
                            OR lower(email) LIKE lower(:searchQuery)
                            OR lower(employee_code) LIKE lower(:searchQuery)
                          )
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("tenantId", tenantId)
                .bind("searchQuery", likeQuery)
                .bind("limit", limit)
                .bind("offset", offset)
                .map((row, metadata) -> mapEmployee(row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_code", String.class),
                        row.get("person_id", UUID.class),
                        row.get("first_name", String.class),
                        row.get("last_name", String.class),
                        row.get("email", String.class),
                        row.get("department_code", String.class),
                        row.get("job_title", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private Employee mapEmployee(
            UUID id,
            String tenantId,
            String employeeCode,
            UUID personId,
            String firstName,
            String lastName,
            String email,
            String departmentCode,
            String jobTitle,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Employee(id, tenantId, employeeCode, personId, firstName, lastName, email, departmentCode, jobTitle, createdAt, updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
