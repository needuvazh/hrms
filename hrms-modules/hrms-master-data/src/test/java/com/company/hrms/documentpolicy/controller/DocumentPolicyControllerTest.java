package com.company.hrms.documentpolicy.controller;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import com.company.hrms.documentpolicy.service.DocumentPolicyModuleApi;
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

class DocumentPolicyControllerTest {

    private final WebTestClient webTestClient = WebTestClient.bindToController(new DocumentPolicyController(new StubDocumentPolicyModuleApi()))
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void listReturnsPagedResultPayload() {
        webTestClient.get()
                .uri("/api/document-policy/document-categories?limit=2&offset=0")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.size").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.totalPages").isEqualTo(1);
    }

    static class StubDocumentPolicyModuleApi implements DocumentPolicyModuleApi {

        @Override
        public Mono<DocumentPolicyModels.MasterViewDto> create(
                DocumentPolicyModels.Resource resource,
                DocumentPolicyModels.MasterUpsertRequest request
        ) {
            return Mono.just(view(UUID.randomUUID(), request.code(), request.name()));
        }

        @Override
        public Mono<DocumentPolicyModels.MasterViewDto> update(
                DocumentPolicyModels.Resource resource,
                UUID id,
                DocumentPolicyModels.MasterUpsertRequest request
        ) {
            return Mono.just(view(id, request.code(), request.name()));
        }

        @Override
        public Mono<DocumentPolicyModels.MasterViewDto> get(DocumentPolicyModels.Resource resource, UUID id) {
            return Mono.just(view(id, "PASSPORT", "Passport"));
        }

        @Override
        public Mono<PagedResult<DocumentPolicyModels.MasterViewDto>> list(
                DocumentPolicyModels.Resource resource,
                DocumentPolicyModels.SearchQuery query
        ) {
            List<DocumentPolicyModels.MasterViewDto> items = List.of(
                    view(UUID.randomUUID(), "IDENTITY", "Identity Documents"),
                    view(UUID.randomUUID(), "IMMIGRATION", "Immigration Documents"));
            return Mono.just(new PagedResult<>(items, 0, query.limit(), items.size(), 1));
        }

        @Override
        public Mono<DocumentPolicyModels.MasterViewDto> updateStatus(
                DocumentPolicyModels.Resource resource,
                UUID id,
                DocumentPolicyModels.StatusUpdateCommand command
        ) {
            return Mono.just(view(id, "PASSPORT", "Passport"));
        }

        @Override
        public Flux<DocumentPolicyModels.OptionViewDto> options(
                DocumentPolicyModels.Resource resource,
                String q,
                int limit,
                boolean activeOnly
        ) {
            return Flux.just(new DocumentPolicyModels.OptionViewDto(UUID.randomUUID(), "PASSPORT", "Passport"));
        }

        private DocumentPolicyModels.MasterViewDto view(UUID id, String code, String name) {
            return new DocumentPolicyModels.MasterViewDto(
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
                    List.of(),
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
