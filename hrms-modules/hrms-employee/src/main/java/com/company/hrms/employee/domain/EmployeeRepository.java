package com.company.hrms.employee.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {

    Mono<Employee> save(Employee employee);

    Mono<Employee> findById(UUID employeeId, String tenantId);

    Flux<Employee> search(String searchQuery, int limit, int offset, String tenantId);
}
