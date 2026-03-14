package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository {

    Mono<EmployeeDto> save(EmployeeDto employee);

    Mono<EmployeeDto> findById(UUID employeeId, String tenantId);

    Flux<EmployeeDto> search(String searchQuery, int limit, int offset, String tenantId);
}
