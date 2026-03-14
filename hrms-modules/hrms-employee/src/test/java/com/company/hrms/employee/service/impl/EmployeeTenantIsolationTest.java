package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeDto;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EmployeeTenantIsolationTest {

    private final InMemoryTenantAwareEmployeeRepository employeeRepository = new InMemoryTenantAwareEmployeeRepository();
    private final RecordingAuditEventPublisher auditEventPublisher = new RecordingAuditEventPublisher();
    private final RecordingOutboxPublisher outboxPublisher = new RecordingOutboxPublisher();
    private final EmployeeApplicationService employeeApplicationService = new EmployeeApplicationService(
            employeeRepository,
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new ConfigurableFeatureToggleService(true, true)),
            auditEventPublisher,
            outboxPublisher);

    @Test
    void createAndGetUsesTenantContextIsolation() {
        CreateEmployeeCommandDto command = new CreateEmployeeCommandDto(
                "EMP-1001",
                "Alice",
                "Johnson",
                "alice@tenant-a.hrms",
                "ENG",
                "Engineer");

        StepVerifier.create(employeeApplicationService.createEmployee(command)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextCount(1)
                .verifyComplete();

        UUID createdEmployeeId = employeeRepository.lastCreatedId;

        StepVerifier.create(employeeApplicationService.getEmployee(createdEmployeeId)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view -> "tenant-a".equals(view.tenantId()) && "alice@tenant-a.hrms".equals(view.email()))
                .verifyComplete();

        org.junit.jupiter.api.Assertions.assertTrue(
                auditEventPublisher.actions.contains("EMPLOYEE_CREATED")
                        && auditEventPublisher.actions.contains("EMPLOYEE_READ"));
        org.junit.jupiter.api.Assertions.assertTrue(outboxPublisher.eventTypes.contains("EmployeeCreated"));

        StepVerifier.create(employeeApplicationService.getEmployee(createdEmployeeId)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("EMPLOYEE_NOT_FOUND", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void searchReturnsOnlyCurrentTenantEmployees() {
        employeeRepository.seed(new EmployeeDto(
                UUID.randomUUID(),
                "tenant-a",
                "EMP-A",
                "Ann",
                "A",
                "ann@a.hrms",
                "ENG",
                "Engineer",
                Instant.now(),
                Instant.now()));

        employeeRepository.seed(new EmployeeDto(
                UUID.randomUUID(),
                "tenant-b",
                "EMP-B",
                "Bob",
                "B",
                "bob@b.hrms",
                "HR",
                "Manager",
                Instant.now(),
                Instant.now()));

        StepVerifier.create(employeeApplicationService.searchEmployees(new EmployeeSearchQueryDto("", 100, 0))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view -> "tenant-a".equals(view.tenantId()))
                .verifyComplete();

        StepVerifier.create(employeeApplicationService.searchEmployees(new EmployeeSearchQueryDto("", 100, 0))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .expectNextMatches(view -> "tenant-b".equals(view.tenantId()))
                .verifyComplete();
    }

    @Test
    void createFailsWhenTenantMissing() {
        CreateEmployeeCommandDto command = new CreateEmployeeCommandDto(
                "EMP-1002",
                "NoTenant",
                "UserDto",
                "no.tenant@hrms.local",
                "ENG",
                "Engineer");

        StepVerifier.create(employeeApplicationService.createEmployee(command))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("TENANT_REQUIRED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void createFailsWhenEmployeeModuleDisabled() {
        EmployeeApplicationService disabledModuleService = new EmployeeApplicationService(
                employeeRepository,
                new DefaultTenantContextAccessor(),
                new EnablementGuard(new ConfigurableFeatureToggleService(false, true)),
                new RecordingAuditEventPublisher(),
                new RecordingOutboxPublisher());

        StepVerifier.create(disabledModuleService.createEmployee(new CreateEmployeeCommandDto(
                                "EMP-1003", "Disabled", "UserDto", "disabled@hrms.local", "ENG", "Engineer"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("MODULE_DISABLED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void searchFailsWhenFeatureFlagDisabled() {
        EmployeeApplicationService disabledFeatureService = new EmployeeApplicationService(
                employeeRepository,
                new DefaultTenantContextAccessor(),
                new EnablementGuard(new ConfigurableFeatureToggleService(true, false)),
                new RecordingAuditEventPublisher(),
                new RecordingOutboxPublisher());

        StepVerifier.create(disabledFeatureService.searchEmployees(new EmployeeSearchQueryDto("", 50, 0))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("FEATURE_DISABLED", ex.getErrorCode());
                })
                .verify();
    }

    static class RecordingAuditEventPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }

    static class RecordingOutboxPublisher implements OutboxPublisher {
        private final CopyOnWriteArrayList<String> eventTypes = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(OutboxEvent event) {
            eventTypes.add(event.eventType());
            return Mono.empty();
        }
    }

    static class ConfigurableFeatureToggleService implements FeatureToggleService {

        private final boolean moduleEnabled;
        private final boolean featureEnabled;

        ConfigurableFeatureToggleService(boolean moduleEnabled, boolean featureEnabled) {
            this.moduleEnabled = moduleEnabled;
            this.featureEnabled = featureEnabled;
        }

        @Override
        public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
            return Mono.just(moduleEnabled);
        }

        @Override
        public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
            return Mono.just(featureEnabled);
        }

        @Override
        public Mono<Boolean> currentTenantHasModule(String moduleKey) {
            return Mono.just(moduleEnabled);
        }

        @Override
        public Mono<Boolean> currentTenantHasFeature(String featureKey) {
            return Mono.just(featureEnabled);
        }
    }

    static class InMemoryTenantAwareEmployeeRepository implements EmployeeRepository {

        private final Map<UUID, EmployeeDto> storage = new ConcurrentHashMap<>();
        private volatile UUID lastCreatedId;

        @Override
        public Mono<EmployeeDto> save(EmployeeDto employee) {
            storage.put(employee.id(), employee);
            lastCreatedId = employee.id();
            return Mono.just(employee);
        }

        @Override
        public Mono<EmployeeDto> findById(UUID employeeId, String tenantId) {
            EmployeeDto employee = storage.get(employeeId);
            if (employee == null || !tenantId.equals(employee.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(employee);
        }

        @Override
        public Flux<EmployeeDto> search(String searchQuery, int limit, int offset, String tenantId) {
            return Flux.fromIterable(storage.values())
                    .filter(employee -> tenantId.equals(employee.tenantId()))
                    .skip(offset)
                    .take(limit);
        }

        void seed(EmployeeDto employee) {
            storage.put(employee.id(), employee);
        }
    }
}
