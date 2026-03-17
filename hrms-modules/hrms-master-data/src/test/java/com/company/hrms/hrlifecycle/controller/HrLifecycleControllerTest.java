package com.company.hrms.hrlifecycle.controller;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import com.company.hrms.hrlifecycle.service.HrLifecycleModuleApi;
import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class HrLifecycleControllerTest {

    private final WebTestClient webTestClient = WebTestClient.bindToController(new HrLifecycleController(new StubHrLifecycleModuleApi()))
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void listReturnsPagedResultPayload() {
        webTestClient.get()
                .uri("/api/hr-lifecycle/event-types?limit=2&offset=0")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.totalPages").isEqualTo(1);
    }

    static class StubHrLifecycleModuleApi implements HrLifecycleModuleApi {

        @Override
        public Mono<HrLifecycleModels.MasterViewDto> create(HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request) {
            return Mono.just(view(UUID.randomUUID(), request.code(), request.name()));
        }

        @Override
        public Mono<HrLifecycleModels.MasterViewDto> update(
                HrLifecycleModels.Resource resource,
                UUID id,
                HrLifecycleModels.MasterUpsertRequest request
        ) {
            return Mono.just(view(id, request.code(), request.name()));
        }

        @Override
        public Mono<HrLifecycleModels.MasterViewDto> get(HrLifecycleModels.Resource resource, UUID id) {
            return Mono.just(view(id, "JOIN", "Join"));
        }

        @Override
        public Mono<PagedResult<HrLifecycleModels.MasterViewDto>> list(
                HrLifecycleModels.Resource resource,
                HrLifecycleModels.SearchQuery query
        ) {
            List<HrLifecycleModels.MasterViewDto> items = List.of(
                    view(UUID.randomUUID(), "JOIN", "Join"),
                    view(UUID.randomUUID(), "TRANSFER", "Transfer"));
            return Mono.just(new PagedResult<>(items, 0, query.limit(), items.size(), 1));
        }

        @Override
        public Mono<HrLifecycleModels.MasterViewDto> updateStatus(
                HrLifecycleModels.Resource resource,
                UUID id,
                HrLifecycleModels.StatusUpdateCommand command
        ) {
            return Mono.just(view(id, "JOIN", "Join"));
        }

        @Override
        public Flux<HrLifecycleModels.OptionViewDto> options(HrLifecycleModels.Resource resource, String q, int limit, boolean activeOnly) {
            return Flux.just(new HrLifecycleModels.OptionViewDto(UUID.randomUUID(), "JOIN", "Join"));
        }

        private static HrLifecycleModels.MasterViewDto view(UUID id, String code, String name) {
            return new HrLifecycleModels.MasterViewDto(
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
                    true,
                    Instant.now(),
                    Instant.now(),
                    "system",
                    "system");
        }
    }
}
