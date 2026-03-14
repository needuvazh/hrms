package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.employee.model.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.service.EmployeeModuleClient;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalEmployeeModuleClient implements EmployeeModuleClient {

    private final EmployeeModuleApi delegate;

    public LocalEmployeeModuleClient(EmployeeModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
        return delegate.createEmployee(command);
    }

    @Override
    public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
        return delegate.getEmployee(employeeId);
    }

    @Override
    public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
        return delegate.searchEmployees(query);
    }
}
