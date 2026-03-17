package com.company.hrms.documentpolicy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import com.company.hrms.documentpolicy.repository.DocumentPolicyRepository;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class DocumentPolicyApplicationServiceTest {

    private final DocumentPolicyRepository repository = mock(DocumentPolicyRepository.class);
    private final TenantContextAccessor tenantContextAccessor = mock(TenantContextAccessor.class);
    private final EnablementGuard enablementGuard = mock(EnablementGuard.class);
    private final AuditEventPublisher auditEventPublisher = mock(AuditEventPublisher.class);

    private final DocumentPolicyApplicationService service = new DocumentPolicyApplicationService(
            repository,
            tenantContextAccessor,
            enablementGuard,
            auditEventPublisher);

    DocumentPolicyApplicationServiceTest() {
        when(enablementGuard.requireModuleEnabled("master-data")).thenReturn(Mono.empty());
        when(tenantContextAccessor.currentTenantId()).thenReturn(Mono.just("default"));
        when(auditEventPublisher.publish(any())).thenReturn(Mono.empty());
        when(repository.codeExists(eq("default"), any(), any(), nullable(UUID.class))).thenReturn(Mono.just(false));
    }

    @Test
    void rejectsDuplicateExpiryAlertDays() {
        DocumentPolicyModels.MasterUpsertRequest request = new DocumentPolicyModels.MasterUpsertRequest(
                "PASSPORT_EXPIRY",
                "Passport expiry",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                UUID.randomUUID(),
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
                true,
                List.of(90, 30, 30),
                0,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true);

        StepVerifier.create(service.create(DocumentPolicyModels.Resource.DOCUMENT_EXPIRY_RULES, request))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException exception = (HrmsException) error;
                    assertEquals("DUPLICATE_ALERT_DAYS", exception.getErrorCode());
                })
                .verify();
    }

    @Test
    void returnsPagedListWithTotalMetadata() {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        when(repository.list(eq("default"), eq(DocumentPolicyModels.Resource.DOCUMENT_CATEGORIES), any()))
                .thenReturn(Flux.just(view(id1, "IDENTITY", "Identity Documents"), view(id2, "IMMIGRATION", "Immigration Documents")));
        when(repository.count(eq("default"), eq(DocumentPolicyModels.Resource.DOCUMENT_CATEGORIES), any()))
                .thenReturn(Mono.just(5L));

        DocumentPolicyModels.SearchQuery query = new DocumentPolicyModels.SearchQuery(
                null,
                true,
                2,
                2,
                "updated_at,desc",
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
                null);

        StepVerifier.create(service.list(DocumentPolicyModels.Resource.DOCUMENT_CATEGORIES, query))
                .assertNext(page -> {
                    assertEquals(2, page.items().size());
                    assertEquals(1, page.page());
                    assertEquals(2, page.size());
                    assertEquals(5L, page.totalElements());
                    assertEquals(3, page.totalPages());
                })
                .verifyComplete();
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
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "system",
                "system");
    }
}
