package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.employee.model.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleClient;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
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
    public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
        return tenantHeaderValue()
                .flatMap(tenantId -> webClient.post()
                        .uri("/api/v1/employees")
                        .header("X-Tenant-Id", tenantId)
                        .bodyValue(command)
                        .retrieve()
                        .bodyToMono(EmployeeViewDto.class));
    }

    @Override
    public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
        return tenantHeaderValue()
                .flatMap(tenantId -> webClient.get()
                        .uri("/api/v1/employees/{id}", employeeId)
                        .header("X-Tenant-Id", tenantId)
                        .retrieve()
                        .bodyToMono(EmployeeViewDto.class));
    }

    @Override
    public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
        return tenantHeaderValue()
                .flatMapMany(tenantId -> webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/v1/employees")
                                .queryParam("q", query.query())
                                .queryParam("limit", query.limit())
                                .queryParam("offset", query.offset())
                                .build())
                        .header("X-Tenant-Id", tenantId)
                        .retrieve()
                        .bodyToFlux(EmployeeViewDto.class));
    }

    private Mono<String> tenantHeaderValue() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "TENANT_REQUIRED",
                        "Tenant is required for remote employee client")));
    }
}
