package com.company.hrms.workflow.application;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.workflow.api.AdvanceWorkflowCommand;
import com.company.hrms.workflow.api.StartWorkflowCommand;
import com.company.hrms.workflow.api.ApprovalStatus;
import com.company.hrms.workflow.api.WorkflowAction;
import com.company.hrms.workflow.domain.WorkflowDefinition;
import com.company.hrms.workflow.domain.WorkflowInstance;
import com.company.hrms.workflow.domain.WorkflowRepository;
import com.company.hrms.workflow.domain.WorkflowStep;
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
        StepVerifier.create(workflowApplicationService.startWorkflow(new StartWorkflowCommand(
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

        StepVerifier.create(workflowApplicationService.advanceWorkflow(new AdvanceWorkflowCommand(
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
        StepVerifier.create(workflowApplicationService.startWorkflow(new StartWorkflowCommand(
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
        private final Map<UUID, WorkflowInstance> instances = new ConcurrentHashMap<>();
        private final Map<String, WorkflowDefinition> definitions = new ConcurrentHashMap<>();
        private final Map<UUID, Integer> stepCount = new ConcurrentHashMap<>();
        private volatile UUID lastInstanceId;

        InMemoryWorkflowRepository() {
            WorkflowDefinition definition = new WorkflowDefinition(
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
        public Mono<WorkflowDefinition> findActiveDefinitionByKey(String tenantId, String workflowKey) {
            return Mono.justOrEmpty(definitions.get(tenantId + ":" + workflowKey));
        }

        @Override
        public Mono<WorkflowInstance> saveInstance(WorkflowInstance instance) {
            instances.put(instance.id(), instance);
            lastInstanceId = instance.id();
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstance> updateInstance(WorkflowInstance instance) {
            instances.put(instance.id(), instance);
            return Mono.just(instance);
        }

        @Override
        public Mono<WorkflowInstance> findInstanceById(String tenantId, UUID workflowInstanceId) {
            WorkflowInstance instance = instances.get(workflowInstanceId);
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
        public Mono<WorkflowStep> saveStep(String tenantId, WorkflowStep step) {
            stepCount.put(step.workflowInstanceId(), stepCount.getOrDefault(step.workflowInstanceId(), 0) + 1);
            return Mono.just(step);
        }
    }
}
