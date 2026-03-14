package com.company.hrms.workflow.service.impl;

import com.company.hrms.workflow.model.*;
import com.company.hrms.workflow.repository.*;
import com.company.hrms.workflow.service.*;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.contracts.workflow.AdvanceWorkflowCommandDto;
import com.company.hrms.contracts.workflow.StartWorkflowCommandDto;
import com.company.hrms.workflow.model.ApprovalStatus;
import com.company.hrms.contracts.workflow.WorkflowAction;
import com.company.hrms.workflow.model.WorkflowDefinitionDto;
import com.company.hrms.workflow.model.WorkflowInstanceDto;
import com.company.hrms.workflow.repository.WorkflowRepository;
import com.company.hrms.workflow.model.WorkflowStepDto;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class WorkflowApplicationServiceTest {

    private final InMemoryWorkflowRepository repository = new InMemoryWorkflowRepository();
    private final WorkflowApplicationService workflowApplicationService = new WorkflowApplicationService(
            repository,
            new DefaultTenantContextAccessor(),
            new SimpleMeterRegistry());

    @Test
    void startAndApproveWorkflow() {
        StepVerifier.create(workflowApplicationService.startWorkflow(new StartWorkflowCommandDto(
                                "leave.approval",
                                "LEAVE_REQUEST",
                                "REQ-101",
                                "emp-101",
                                "apply leave"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> {
                    assertEquals(ApprovalStatus.SUBMITTED, view.status());
                    assertEquals("leave.approval", view.workflowKey());
                })
                .verifyComplete();

        UUID workflowInstanceId = repository.lastInstanceId;

        StepVerifier.create(workflowApplicationService.advanceWorkflow(new AdvanceWorkflowCommandDto(
                                workflowInstanceId,
                                WorkflowAction.APPROVE,
                                "manager-1",
                                "approved"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> assertEquals(ApprovalStatus.APPROVED, view.status()))
                .verifyComplete();
    }

    @Test
    void rejectWhenDefinitionMissing() {
        StepVerifier.create(workflowApplicationService.startWorkflow(new StartWorkflowCommandDto(
                                "missing.workflow",
                                "LEAVE_REQUEST",
                                "REQ-404",
                                "emp-404",
                                "missing"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("WORKFLOW_DEFINITION_NOT_FOUND", ex.getErrorCode());
                })
                .verify();
    }

    static class InMemoryWorkflowRepository implements WorkflowRepository {
        private final Map<UUID, WorkflowInstanceDto> instances = new ConcurrentHashMap<>();
        private final Map<String, WorkflowDefinitionDto> definitions = new ConcurrentHashMap<>();
        private final Map<UUID, Integer> stepCount = new ConcurrentHashMap<>();
        private volatile UUID lastInstanceId;

        InMemoryWorkflowRepository() {
            WorkflowDefinitionDto definition = new WorkflowDefinitionDto(
                    UUID.randomUUID(),
                    "default",
                    "leave.approval",
                    "Leave Approval",
                    true,
                    Instant.now(),
                    Instant.now());
            definitions.put(definition.tenantId() + ":" + definition.workflowKey(), definition);
        }

        @Override
        public Mono<WorkflowDefinitionDto> findActiveDefinitionByKey(String tenantId, String workflowKey) {
            return Mono.justOrEmpty(definitions.get(tenantId + ":" + workflowKey));
        }

        @Override
        public Mono<WorkflowInstanceDto> saveInstance(WorkflowInstanceDto instance) {
            instances.put(instance.id(), instance);
            lastInstanceId = instance.id();
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstanceDto> updateInstance(WorkflowInstanceDto instance) {
            instances.put(instance.id(), instance);
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstanceDto> findInstanceById(String tenantId, UUID workflowInstanceId) {
            WorkflowInstanceDto instance = instances.get(workflowInstanceId);
            if (instance == null || !tenantId.equals(instance.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(instance);
        }

        @Override
        public Mono<Integer> countSteps(String tenantId, UUID workflowInstanceId) {
            return Mono.just(stepCount.getOrDefault(workflowInstanceId, 0));
        }

        @Override
        public Mono<WorkflowStepDto> saveStep(String tenantId, WorkflowStepDto step) {
            stepCount.put(step.workflowInstanceId(), stepCount.getOrDefault(step.workflowInstanceId(), 0) + 1);
            return Mono.just(step);
        }
    }
}
