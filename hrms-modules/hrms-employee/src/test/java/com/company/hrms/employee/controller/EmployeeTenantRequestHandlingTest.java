package com.company.hrms.employee.controller;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.employee.service.impl.EmployeeApplicationService;
import com.company.hrms.employee.model.EmployeeDto;
import com.company.hrms.employee.repository.EmployeeRepository;
import com.company.hrms.platform.audit.api.NoopAuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.outbox.api.NoopOutboxPublisher;
import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class EmployeeTenantRequestHandlingTest {

    private final EmployeeRepository employeeRepository = new InMemoryTenantAwareEmployeeRepository();
    private final EmployeeApplicationService employeeApplicationService =
            new EmployeeApplicationService(
                    employeeRepository,
                    new DefaultTenantContextAccessor(),
                    new EnablementGuard(new ConfigurableFeatureToggleService(true, true)),
                    new NoopAuditEventPublisher(),
                    new NoopOutboxPublisher());
    private final EmployeeController employeeController = new EmployeeController(employeeApplicationService);

    private final WebTestClient webTestClient = WebTestClient.bindToController(employeeController)
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter(null))
            .build();

    @Test
    void sameEmployeeIdIsIsolatedAcrossTenants() {
        EmployeeViewDto createdEmployee = webTestClient.post()
                .uri("/api/v1/employees")
                .header("X-Tenant-Id", "tenant-a")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "employeeCode": "EMP-REQ-1",
                          "firstName": "Alice",
                          "lastName": "A",
                          "email": "alice.req@hrms.local",
                          "departmentCode": "ENG",
                          "jobTitle": "Engineer"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeViewDto.class)
                .returnResult()
                .getResponseBody();

        org.junit.jupiter.api.Assertions.assertNotNull(createdEmployee);
        String employeeId = createdEmployee.id().toString();

        webTestClient.get()
                .uri("/api/v1/employees/{id}", employeeId)
                .header("X-Tenant-Id", "tenant-a")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tenantId").isEqualTo("tenant-a");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", employeeId)
                .header("X-Tenant-Id", "tenant-b")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void requestWithoutTenantHeaderIsRejected() {
        webTestClient.post()
                .uri("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "employeeCode": "EMP-REQ-2",
                          "firstName": "No",
                          "lastName": "TenantDto",
                          "email": "no.tenant.req@hrms.local"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("TENANT_REQUIRED");
    }

    @Test
    void endpointBlockedWhenEmployeeModuleDisabled() {
        EmployeeApplicationService disabledService = new EmployeeApplicationService(
                employeeRepository,
                new DefaultTenantContextAccessor(),
                new EnablementGuard(new ConfigurableFeatureToggleService(false, true)),
                new NoopAuditEventPublisher(),
                new NoopOutboxPublisher());

        WebTestClient disabledClient = WebTestClient.bindToController(new EmployeeController(disabledService))
                .controllerAdvice(new GlobalExceptionHandler())
                .webFilter(new TenantContextWebFilter(null))
                .build();

        disabledClient.get()
                .uri("/api/v1/employees")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MODULE_DISABLED");
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

        @Override
        public Mono<EmployeeDto> save(EmployeeDto employee) {
            storage.put(employee.id(), employee);
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
    }
}
