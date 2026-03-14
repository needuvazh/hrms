package com.company.hrms.employee.infrastructure.client;

import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleClient;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebClientEmployeeModuleClient implements EmployeeModuleClient {

    private final WebClient webClient;
    private final TenantContextAccessor tenantContextAccessor;

    public WebClientEmployeeModuleClient(WebClient webClient, TenantContextAccessor tenantContextAccessor) {
        this.webClient = webClient;
        this.tenantContextAccessor = tenantContextAccessor;
    }

    @Override
    public Mono<EmployeeView> createEmployee(CreateEmployeeCommand command) {
        return tenantHeaderValue()
                .flatMap(tenantId -> webClient.post()
                        .uri("/api/v1/employees")
                        .header("X-Tenant-Id", tenantId)
                        .bodyValue(command)
                        .retrieve()
                        .bodyToMono(EmployeeView.class));
    }

    @Override
    public Mono<EmployeeView> getEmployee(UUID employeeId) {
        return tenantHeaderValue()
                .flatMap(tenantId -> webClient.get()
                        .uri("/api/v1/employees/{id}", employeeId)
                        .header("X-Tenant-Id", tenantId)
                        .retrieve()
                        .bodyToMono(EmployeeView.class));
    }

    @Override
    public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
        return tenantHeaderValue()
                .flatMapMany(tenantId -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v1/employees")
                                .queryParam("q", query.query())
                                .queryParam("limit", query.limit())
                                .queryParam("offset", query.offset())
                                .build())
                        .header("X-Tenant-Id", tenantId)
                        .retrieve()
                        .bodyToFlux(EmployeeView.class));
    }

    private Mono<String> tenantHeaderValue() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "TENANT_REQUIRED",
                        "Tenant is required for remote employee client")));
    }
}
