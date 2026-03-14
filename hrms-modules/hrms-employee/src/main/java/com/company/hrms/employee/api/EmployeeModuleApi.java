package com.company.hrms.employee.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeModuleApi {

    Mono<EmployeeView> createEmployee(CreateEmployeeCommand command);

    Mono<EmployeeView> getEmployee(UUID employeeId);

    Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query);
}
