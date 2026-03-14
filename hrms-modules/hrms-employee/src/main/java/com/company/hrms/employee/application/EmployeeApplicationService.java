package com.company.hrms.employee.application;

import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.employee.domain.Employee;
import com.company.hrms.employee.domain.EmployeeRepository;
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
    public Mono<EmployeeView> createEmployee(CreateEmployeeCommand command) {
        return enablementGuard.requireModuleEnabled("employee")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMap(tenantId -> {
                            validate(command);
                            Instant now = Instant.now();
                            Employee employee = new Employee(
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
                                                    "Employee onboarding completed",
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
    public Mono<EmployeeView> getEmployee(UUID employeeId) {
        return enablementGuard.requireModuleEnabled("employee")
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMap(tenantId -> employeeRepository.findById(employeeId, tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found")))
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
    public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
        int limit = query.limit() > 0 ? query.limit() : DEFAULT_LIMIT;
        int offset = Math.max(query.offset(), 0);

        return enablementGuard.requireModuleEnabled("employee")
                .then(enablementGuard.requireFeatureEnabled("employee.search"))
                .thenMany(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                        .flatMapMany(tenantId -> employeeRepository.search(query.query(), limit, offset, tenantId)
                                .map(this::toView)));
    }

    private void validate(CreateEmployeeCommand command) {
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
    }

    private EmployeeView toView(Employee employee) {
        return new EmployeeView(
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

    private String employeeCreatedPayload(Employee employee) {
        String personId = employee.personId() == null ? "" : employee.personId().toString();
        return "{\"employeeId\":\"%s\",\"employeeCode\":\"%s\",\"email\":\"%s\",\"personId\":\"%s\"}"
                .formatted(employee.id(), employee.employeeCode(), employee.email(), personId);
    }

    private Map<String, Object> employeeSnapshot(Employee employee) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("employeeCode", employee.employeeCode());
        snapshot.put("personId", employee.personId());
        snapshot.put("firstName", employee.firstName());
        snapshot.put("lastName", employee.lastName());
        snapshot.put("email", employee.email());
        snapshot.put("departmentCode", employee.departmentCode());
        snapshot.put("jobTitle", employee.jobTitle());
        return snapshot;
    }
}
