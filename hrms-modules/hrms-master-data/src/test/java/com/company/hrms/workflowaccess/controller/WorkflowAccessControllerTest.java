package com.company.hrms.workflowaccess.controller;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import com.company.hrms.workflowaccess.service.WorkflowAccessModuleApi;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class WorkflowAccessControllerTest {

    private final WebTestClient webTestClient = WebTestClient.bindToController(new WorkflowAccessController(new StubWorkflowAccessModuleApi()))
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void listReturnsPagedResultPayload() {
        webTestClient.get()
                .uri("/api/workflow-access/roles?limit=2&offset=0")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.totalPages").isEqualTo(1);
    }

    static class StubWorkflowAccessModuleApi implements WorkflowAccessModuleApi {

        @Override
        public Mono<WorkflowAccessModels.MasterViewDto> create(
                WorkflowAccessModels.Resource resource,
                WorkflowAccessModels.MasterUpsertRequest request
        ) {
            return Mono.just(view(UUID.randomUUID(), request.code(), request.name()));
        }

        @Override
        public Mono<WorkflowAccessModels.MasterViewDto> update(
                WorkflowAccessModels.Resource resource,
                UUID id,
                WorkflowAccessModels.MasterUpsertRequest request
        ) {
            return Mono.just(view(id, request.code(), request.name()));
        }

        @Override
        public Mono<WorkflowAccessModels.MasterViewDto> get(WorkflowAccessModels.Resource resource, UUID id) {
            return Mono.just(view(id, "HR_ADMIN", "HR Admin"));
        }

        @Override
        public Mono<PagedResult<WorkflowAccessModels.MasterViewDto>> list(
                WorkflowAccessModels.Resource resource,
                WorkflowAccessModels.SearchQuery query
        ) {
            List<WorkflowAccessModels.MasterViewDto> items = List.of(
                    view(UUID.randomUUID(), "HR_ADMIN", "HR Admin"),
                    view(UUID.randomUUID(), "MANAGER", "Manager"));
            return Mono.just(new PagedResult<>(items, 0, query.limit(), items.size(), 1));
        }

        @Override
        public Mono<WorkflowAccessModels.MasterViewDto> updateStatus(
                WorkflowAccessModels.Resource resource,
                UUID id,
                WorkflowAccessModels.StatusUpdateCommand command
        ) {
            return Mono.just(view(id, "HR_ADMIN", "HR Admin"));
        }

        @Override
        public Flux<WorkflowAccessModels.OptionViewDto> options(
                WorkflowAccessModels.Resource resource,
                String q,
                int limit,
                boolean activeOnly
        ) {
            return Flux.just(new WorkflowAccessModels.OptionViewDto(UUID.randomUUID(), "HR_ADMIN", "HR Admin"));
        }

        private static WorkflowAccessModels.MasterViewDto view(UUID id, String code, String name) {
            return new WorkflowAccessModels.MasterViewDto(
                    id,
                    "default",
                    code,
                    name,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    true,
                    Instant.now(),
                    Instant.now(),
                    "system",
                    "system");
        }
    }
}
