package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.employee.model.EmployeeDto;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Primary
public class EmployeeApplicationService implements EmployeeModuleApi {

    private static final int DEFAULT_LIMIT = 50;

    private final EmployeeRepository employeeRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;
    private final OutboxPublisher outboxPublisher;

    public EmployeeApplicationService(
            EmployeeRepository employeeRepository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher,
            OutboxPublisher outboxPublisher
    ) {
        this.employeeRepository = employeeRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
        return enablementGuard.requireModuleEnabled("employee")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMap(tenantId -> {
                            validate(command);
                            Instant now = Instant.now();
                            EmployeeDto employee = new EmployeeDto(
                                    UUID.randomUUID(),
                                    tenantId,
                                    command.employeeCode(),
                                    command.personId(),
                                    command.firstName(),
                                    command.lastName(),
                                    command.email(),
                                    command.departmentCode(),
                                    command.jobTitle(),
                                    now,
                                    now);
                            return employeeRepository.save(employee)
                                    .flatMap(saved -> auditEventPublisher.publish(AuditEvent.detailed(
                                                    "system",
                                                    tenantId,
                                                    "EMPLOYEE_CREATED",
                                                    "EMPLOYEE",
                                                    saved.id().toString(),
                                                    "employee",
                                                    1L,
                                                    List.of("employeeCode", "personId", "firstName", "lastName", "email", "departmentCode", "jobTitle"),
                                                    Map.of(),
                                                    employeeSnapshot(saved),
                                                    "system",
                                                    "system",
                                                    null,
                                                    "EmployeeDto onboarding completed",
                                                    "hrms-employee",
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    Map.of("email", saved.email()),
                                                    false))
                                            .then(outboxPublisher.publish(new OutboxEvent(
                                                    tenantId,
                                                    "EMPLOYEE",
                                                    saved.id().toString(),
                                                    "EmployeeCreated",
                                                    employeeCreatedPayload(saved),
                                                    now)))
                                            .thenReturn(saved))
                                    .map(this::toView);
                        }));
    }

    @Override
    public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
        return enablementGuard.requireModuleEnabled("employee")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMap(tenantId -> employeeRepository.findById(employeeId, tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "EmployeeDto not found")))
                                .flatMap(employee -> auditEventPublisher.publish(AuditEvent.of(
                                                "system",
                                                tenantId,
                                                "EMPLOYEE_READ",
                                                "EMPLOYEE",
                                                employee.id().toString(),
                                                Map.of("employeeCode", employee.employeeCode())))
                                        .thenReturn(employee))
                                .map(this::toView)));
    }

    @Override
    public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
        int limit = query.limit() > 0 ? query.limit() : DEFAULT_LIMIT;
        int offset = Math.max(query.offset(), 0);

        return enablementGuard.requireModuleEnabled("employee")
                .then(enablementGuard.requireFeatureEnabled("employee.search"))
                .thenMany(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMapMany(tenantId -> employeeRepository.search(query.query(), limit, offset, tenantId)
                                .map(this::toView)));
    }

    private void validate(CreateEmployeeCommandDto command) {
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
    }

    private EmployeeViewDto toView(EmployeeDto employee) {
        return new EmployeeViewDto(
                employee.id(),
                employee.tenantId(),
                employee.employeeCode(),
                employee.personId(),
                employee.firstName(),
                employee.lastName(),
                employee.email(),
                employee.departmentCode(),
                employee.jobTitle(),
                employee.createdAt(),
                employee.updatedAt());
    }

    private String employeeCreatedPayload(EmployeeDto employee) {
        String personId = employee.personId() == null ? "" : employee.personId().toString();
        return "{\"employeeId\":\"%s\",\"employeeCode\":\"%s\",\"email\":\"%s\",\"personId\":\"%s\"}"
                .formatted(employee.id(), employee.employeeCode(), employee.email(), personId);
    }

    private Map<String, Object> employeeSnapshot(EmployeeDto employee) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        putIfNotNull(snapshot, "employeeCode", employee.employeeCode());
        putIfNotNull(snapshot, "personId", employee.personId());
        putIfNotNull(snapshot, "firstName", employee.firstName());
        putIfNotNull(snapshot, "lastName", employee.lastName());
        putIfNotNull(snapshot, "email", employee.email());
        putIfNotNull(snapshot, "departmentCode", employee.departmentCode());
        putIfNotNull(snapshot, "jobTitle", employee.jobTitle());
        return snapshot;
    }

    private void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }
}
