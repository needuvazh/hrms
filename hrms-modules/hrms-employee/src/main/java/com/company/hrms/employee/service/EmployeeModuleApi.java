package com.company.hrms.employee.service;

import com.company.hrms.employee.model.*;
import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeModuleApi {

    Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command);

    Mono<EmployeeViewDto> getEmployee(UUID employeeId);

    Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query);
}
