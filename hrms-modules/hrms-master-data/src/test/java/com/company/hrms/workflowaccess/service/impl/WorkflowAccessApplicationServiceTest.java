package com.company.hrms.workflowaccess.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.workflowaccess.model.WorkflowAccessModels;
import com.company.hrms.workflowaccess.repository.WorkflowAccessRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WorkflowAccessApplicationServiceTest {

    private final WorkflowAccessRepository repository = mock(WorkflowAccessRepository.class);
    private final TenantContextAccessor tenantContextAccessor = mock(TenantContextAccessor.class);
    private final EnablementGuard enablementGuard = mock(EnablementGuard.class);
    private final AuditEventPublisher auditEventPublisher = mock(AuditEventPublisher.class);

    private final WorkflowAccessApplicationService service = new WorkflowAccessApplicationService(
            repository,
            tenantContextAccessor,
            enablementGuard,
            auditEventPublisher);

    WorkflowAccessApplicationServiceTest() {
        when(enablementGuard.requireModuleEnabled("master-data")).thenReturn(Mono.empty());
        when(tenantContextAccessor.currentTenantId()).thenReturn(Mono.just("default"));
        when(auditEventPublisher.publish(any())).thenReturn(Mono.empty());
        when(repository.codeExists(eq("default"), any(), any(), nullable(UUID.class))).thenReturn(Mono.just(false));
    }

    @Test
    void createsRolePermissionMappingWithoutCodeAndName() {
        UUID roleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID permissionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID mappingId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        when(repository.existsById("default", "master_data.roles", roleId, true)).thenReturn(Mono.just(true));
        when(repository.existsById("default", "master_data.permissions", permissionId, false)).thenReturn(Mono.just(true));
        when(repository.create(eq("default"), eq(WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS), any(), eq("system")))
                .thenReturn(Mono.just(view(mappingId, mappingId.toString(), "Role-Permission Mapping")));

        StepVerifier.create(service.create(WorkflowAccessModels.Resource.ROLE_PERMISSION_MAPPINGS, mappingRequest(roleId, permissionId)))
                .assertNext(saved -> {
                    assertEquals(mappingId, saved.id());
                    assertEquals("default", saved.tenantId());
                })
                .verifyComplete();
    }

    @Test
    void returnsPagedListWithTotalMetadata() {
        UUID id1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID id2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        when(repository.list(eq("default"), eq(WorkflowAccessModels.Resource.ROLES), any()))
                .thenReturn(Flux.just(view(id1, "HR_ADMIN", "HR Admin"), view(id2, "MANAGER", "Manager")));
        when(repository.count(eq("default"), eq(WorkflowAccessModels.Resource.ROLES), any())).thenReturn(Mono.just(5L));

        WorkflowAccessModels.SearchQuery query = new WorkflowAccessModels.SearchQuery(
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
                null);

        StepVerifier.create(service.list(WorkflowAccessModels.Resource.ROLES, query))
                .assertNext(page -> {
                    assertEquals(2, page.items().size());
                    assertEquals(1, page.page());
                    assertEquals(2, page.size());
                    assertEquals(5L, page.totalElements());
                    assertEquals(3, page.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void rejectsEmailTemplateWithoutSubject() {
        WorkflowAccessModels.MasterUpsertRequest request = new WorkflowAccessModels.MasterUpsertRequest(
                "LEAVE_APPROVED_EMAIL",
                "Leave Approved",
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
                "LEAVE_APPROVED",
                "EMAIL",
                null,
                "Your leave is approved",
                "en",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "template",
                true);

        StepVerifier.create(service.create(WorkflowAccessModels.Resource.NOTIFICATION_TEMPLATES, request))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException exception = (HrmsException) error;
                    assertEquals("SUBJECT_REQUIRED", exception.getErrorCode());
                })
                .verify();
    }

    private WorkflowAccessModels.MasterUpsertRequest mappingRequest(UUID roleId, UUID permissionId) {
        return new WorkflowAccessModels.MasterUpsertRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                roleId,
                permissionId,
                true,
                "ALL",
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
                "mapping",
                true);
    }

    private WorkflowAccessModels.MasterViewDto view(UUID id, String code, String name) {
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
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"),
                "system",
                "system");
    }
}
