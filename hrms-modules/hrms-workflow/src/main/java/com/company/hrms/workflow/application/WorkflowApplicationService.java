package com.company.hrms.workflow.application;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.workflow.api.AdvanceWorkflowCommand;
import com.company.hrms.workflow.api.ApprovalStatus;
import com.company.hrms.workflow.api.StartWorkflowCommand;
import com.company.hrms.workflow.api.WorkflowAction;
import com.company.hrms.workflow.api.WorkflowInstanceView;
import com.company.hrms.workflow.api.WorkflowModuleApi;
import com.company.hrms.workflow.domain.WorkflowDefinition;
import com.company.hrms.workflow.domain.WorkflowInstance;
import com.company.hrms.workflow.domain.WorkflowRepository;
import com.company.hrms.workflow.domain.WorkflowStep;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
public class WorkflowApplicationService implements WorkflowModuleApi {

    private final WorkflowRepository workflowRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final MeterRegistry meterRegistry;

    public WorkflowApplicationService(
            WorkflowRepository workflowRepository,
            TenantContextAccessor tenantContextAccessor,
            MeterRegistry meterRegistry
    ) {
        this.workflowRepository = workflowRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<WorkflowInstanceView> startWorkflow(StartWorkflowCommand command) {
        validateStart(command);

        return requireTenant().flatMap(tenantId -> workflowRepository.findActiveDefinitionByKey(tenantId, command.workflowKey())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WORKFLOW_DEFINITION_NOT_FOUND", "Workflow definition not found")))
                .flatMap(definition -> createInstance(tenantId, definition, command))
                .doOnNext(view -> recordStartMetric(view.workflowKey()))
                .map(this::toView));
    }

    @Override
    public Mono<WorkflowInstanceView> advanceWorkflow(AdvanceWorkflowCommand command) {
        validateAdvance(command);

        return requireTenant().flatMap(tenantId -> workflowRepository.findInstanceById(tenantId, command.workflowInstanceId())
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WORKFLOW_INSTANCE_NOT_FOUND", "Workflow instance not found")))
                .flatMap(existing -> transition(tenantId, existing, command))
                .doOnNext(instance -> recordTransitionMetric(instance.workflowKey(), instance.status().name()))
                .map(this::toView));
    }

    @Override
    public Mono<WorkflowInstanceView> getWorkflowInstance(UUID workflowInstanceId) {
        return requireTenant().flatMap(tenantId -> workflowRepository.findInstanceById(tenantId, workflowInstanceId)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WORKFLOW_INSTANCE_NOT_FOUND", "Workflow instance not found")))
                .map(this::toView));
    }

    private Mono<WorkflowInstance> createInstance(String tenantId, WorkflowDefinition definition, StartWorkflowCommand command) {
        Instant now = Instant.now();
        WorkflowInstance instance = new WorkflowInstance(
                UUID.randomUUID(),
                tenantId,
                definition.id(),
                definition.workflowKey(),
                command.targetType().trim().toUpperCase(),
                command.targetId().trim(),
                com.company.hrms.workflow.domain.ApprovalStatus.SUBMITTED,
                command.actor().trim(),
                null,
                now,
                null,
                now,
                now);

        return workflowRepository.saveInstance(instance)
                .flatMap(saved -> workflowRepository.saveStep(tenantId, new WorkflowStep(
                                        UUID.randomUUID(),
                                        saved.id(),
                                        1,
                                        command.actor().trim(),
                                        com.company.hrms.workflow.domain.WorkflowAction.SUBMIT,
                                        command.comments(),
                                        now,
                                        now))
                                .thenReturn(saved));
    }

    private Mono<WorkflowInstance> transition(String tenantId, WorkflowInstance existing, AdvanceWorkflowCommand command) {
        if (existing.status() != com.company.hrms.workflow.domain.ApprovalStatus.SUBMITTED) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_ALREADY_DECIDED", "Workflow already has terminal state"));
        }

        com.company.hrms.workflow.domain.ApprovalStatus next = mapStatus(command.action());
        Instant now = Instant.now();

        return workflowRepository.countSteps(tenantId, existing.id())
                .flatMap(count -> workflowRepository.saveStep(tenantId, new WorkflowStep(
                                        UUID.randomUUID(),
                                        existing.id(),
                                        count + 1,
                                        command.actor().trim(),
                                        toDomainWorkflowAction(command.action()),
                                        command.comments(),
                                        now,
                                        now))
                                .thenReturn(existing.transition(next, command.actor().trim(), now)))
                .flatMap(workflowRepository::updateInstance);
    }

    private com.company.hrms.workflow.domain.ApprovalStatus mapStatus(WorkflowAction action) {
        return switch (action) {
            case APPROVE -> com.company.hrms.workflow.domain.ApprovalStatus.APPROVED;
            case REJECT -> com.company.hrms.workflow.domain.ApprovalStatus.REJECTED;
            case CANCEL -> com.company.hrms.workflow.domain.ApprovalStatus.CANCELLED;
            case SUBMIT -> throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_WORKFLOW_ACTION", "SUBMIT cannot be used for workflow advancement");
        };
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateStart(StartWorkflowCommand command) {
        if (!StringUtils.hasText(command.workflowKey())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_KEY_REQUIRED", "Workflow key is required");
        }
        if (!StringUtils.hasText(command.targetType())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "TARGET_TYPE_REQUIRED", "Target type is required");
        }
        if (!StringUtils.hasText(command.targetId())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "TARGET_ID_REQUIRED", "Target id is required");
        }
        if (!StringUtils.hasText(command.actor())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTOR_REQUIRED", "Actor is required");
        }
    }

    private void validateAdvance(AdvanceWorkflowCommand command) {
        if (command.workflowInstanceId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_INSTANCE_REQUIRED", "Workflow instance id is required");
        }
        if (command.action() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_ACTION_REQUIRED", "Workflow action is required");
        }
        if (!StringUtils.hasText(command.actor())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTOR_REQUIRED", "Actor is required");
        }
    }

    private WorkflowInstanceView toView(WorkflowInstance instance) {
        return new WorkflowInstanceView(
                instance.id(),
                instance.tenantId(),
                instance.workflowKey(),
                instance.targetType(),
                instance.targetId(),
                toApiApprovalStatus(instance.status()),
                instance.requestedBy(),
                instance.decidedBy(),
                instance.submittedAt(),
                instance.decidedAt(),
                instance.createdAt(),
                instance.updatedAt());
    }

    private void recordStartMetric(String workflowKey) {
        meterRegistry.counter(
                "hrms.workflow.transitions",
                "workflow_key", workflowKey,
                "transition", "START",
                "result", ApprovalStatus.SUBMITTED.name())
                .increment();
    }

    private void recordTransitionMetric(String workflowKey, String result) {
        meterRegistry.counter(
                "hrms.workflow.transitions",
                "workflow_key", workflowKey,
                "transition", "ADVANCE",
                "result", result)
                .increment();
    }

    private com.company.hrms.workflow.domain.WorkflowAction toDomainWorkflowAction(WorkflowAction action) {
        return com.company.hrms.workflow.domain.WorkflowAction.valueOf(action.name());
    }

    private ApprovalStatus toApiApprovalStatus(com.company.hrms.workflow.domain.ApprovalStatus status) {
        return ApprovalStatus.valueOf(status.name());
    }
}
