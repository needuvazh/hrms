package com.company.hrms.hrlifecycle.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import com.company.hrms.hrlifecycle.repository.HrLifecycleRepository;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class HrLifecycleApplicationServiceTest {

    private final HrLifecycleRepository repository = mock(HrLifecycleRepository.class);
    private final TenantContextAccessor tenantContextAccessor = mock(TenantContextAccessor.class);
    private final EnablementGuard enablementGuard = mock(EnablementGuard.class);
    private final AuditEventPublisher auditEventPublisher = mock(AuditEventPublisher.class);

    private final HrLifecycleApplicationService service = new HrLifecycleApplicationService(
            repository,
            tenantContextAccessor,
            enablementGuard,
            auditEventPublisher);

    HrLifecycleApplicationServiceTest() {
        when(enablementGuard.requireModuleEnabled("master-data")).thenReturn(Mono.empty());
        when(tenantContextAccessor.currentTenantId()).thenReturn(Mono.just("default"));
        when(auditEventPublisher.publish(any())).thenReturn(Mono.empty());
        when(repository.codeExists(eq("default"), any(), any(), nullable(UUID.class))).thenReturn(Mono.just(false));
    }

    @Test
    void rejectsInvalidCalendarYear() {
        HrLifecycleModels.MasterUpsertRequest request = new HrLifecycleModels.MasterUpsertRequest(
                "OMAN_PUBLIC_1800",
                "Oman Public",
                "OM",
                1800,
                "PUBLIC",
                true,
                true,
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
                "desc",
                true);

        StepVerifier.create(service.create(HrLifecycleModels.Resource.HOLIDAY_CALENDARS, request))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    assertEquals("INVALID_CALENDAR_YEAR", ((HrmsException) error).getErrorCode());
                })
                .verify();
    }

    @Test
    void rejectsNonFlexibleShiftWithoutTimes() {
        HrLifecycleModels.MasterUpsertRequest request = new HrLifecycleModels.MasterUpsertRequest(
                "GENERAL_SHIFT",
                "General Shift",
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
                "FIXED",
                null,
                null,
                60,
                false,
                10,
                10,
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
                "desc",
                true);

        StepVerifier.create(service.create(HrLifecycleModels.Resource.SHIFTS, request))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    assertEquals("SHIFT_TIME_REQUIRED", ((HrmsException) error).getErrorCode());
                })
                .verify();
    }

    @Test
    void returnsPagedResultForList() {
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(repository.list(eq("default"), eq(HrLifecycleModels.Resource.EVENT_TYPES), any()))
                .thenReturn(Flux.just(view(id1, "JOIN", "Join"), view(id2, "TRANSFER", "Transfer")));
        when(repository.count(eq("default"), eq(HrLifecycleModels.Resource.EVENT_TYPES), any())).thenReturn(Mono.just(5L));

        HrLifecycleModels.SearchQuery query = new HrLifecycleModels.SearchQuery(
                null, true, 2, 2, "updated_at,desc",
                null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null,
                null, null);

        StepVerifier.create(service.list(HrLifecycleModels.Resource.EVENT_TYPES, query))
                .assertNext(page -> {
                    assertEquals(2, page.items().size());
                    assertEquals(1, page.page());
                    assertEquals(2, page.size());
                    assertEquals(5L, page.totalElements());
                    assertEquals(3, page.totalPages());
                })
                .verifyComplete();
    }

    private HrLifecycleModels.MasterViewDto view(UUID id, String code, String name) {
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
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
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
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "system",
                "system");
    }
}
