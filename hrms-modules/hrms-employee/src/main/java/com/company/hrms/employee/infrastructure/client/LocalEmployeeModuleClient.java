package com.company.hrms.employee.infrastructure.client;

import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeModuleClient;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalEmployeeModuleClient implements EmployeeModuleClient {

    private final EmployeeModuleApi delegate;

    public LocalEmployeeModuleClient(EmployeeModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<EmployeeView> createEmployee(CreateEmployeeCommand command) {
        return delegate.createEmployee(command);
    }

    @Override
    public Mono<EmployeeView> getEmployee(UUID employeeId) {
        return delegate.getEmployee(employeeId);
    }

    @Override
    public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
        return delegate.searchEmployees(query);
    }
}
