package com.company.hrms.workflow.infrastructure.persistence;

import com.company.hrms.workflow.domain.ApprovalStatus;
import com.company.hrms.workflow.domain.WorkflowAction;
import com.company.hrms.workflow.domain.WorkflowDefinition;
import com.company.hrms.workflow.domain.WorkflowInstance;
import com.company.hrms.workflow.domain.WorkflowRepository;
import com.company.hrms.workflow.domain.WorkflowStep;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class WorkflowR2dbcRepository implements WorkflowRepository {

    private final DatabaseClient databaseClient;

    public WorkflowR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<WorkflowDefinition> findActiveDefinitionByKey(String tenantId, String workflowKey) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, workflow_key, workflow_name, is_active, created_at, updated_at
                        FROM workflow.workflow_definitions
                        WHERE tenant_id = :tenantId
                          AND workflow_key = :workflowKey
                          AND is_active = true
                        """)
                .bind("tenantId", tenantId)
                .bind("workflowKey", workflowKey)
                .map((row, metadata) -> new WorkflowDefinition(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("workflow_key", String.class),
                        row.get("workflow_name", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WorkflowInstance> saveInstance(WorkflowInstance instance) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO workflow.workflow_instances(
                            id, tenant_id, workflow_definition_id, workflow_key, target_type, target_id,
                            approval_status, requested_by, decided_by, submitted_at, decided_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :workflowDefinitionId, :workflowKey, :targetType, :targetId,
                            :approvalStatus, :requestedBy, :decidedBy, :submittedAt, :decidedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, workflow_definition_id, workflow_key, target_type, target_id,
                                  approval_status, requested_by, decided_by, submitted_at, decided_at, created_at, updated_at
                        """)
                .bind("id", instance.id())
                .bind("tenantId", instance.tenantId())
                .bind("workflowDefinitionId", instance.workflowDefinitionId())
                .bind("workflowKey", instance.workflowKey())
                .bind("targetType", instance.targetType())
                .bind("targetId", instance.targetId())
                .bind("approvalStatus", instance.status().name())
                .bind("requestedBy", instance.requestedBy())
                .bind("submittedAt", instance.submittedAt())
                .bind("createdAt", instance.createdAt())
                .bind("updatedAt", instance.updatedAt());

        spec = bindNullable(spec, "decidedBy", instance.decidedBy(), String.class);
        spec = bindNullable(spec, "decidedAt", instance.decidedAt(), Instant.class);

        return spec.map((row, metadata) -> mapInstance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("workflow_definition_id", UUID.class),
                        row.get("workflow_key", String.class),
                        row.get("target_type", String.class),
                        row.get("target_id", String.class),
                        row.get("approval_status", String.class),
                        row.get("requested_by", String.class),
                        row.get("decided_by", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("decided_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WorkflowInstance> updateInstance(WorkflowInstance instance) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE workflow.workflow_instances
                        SET approval_status = :approvalStatus,
                            decided_by = :decidedBy,
                            decided_at = :decidedAt,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, workflow_definition_id, workflow_key, target_type, target_id,
                                  approval_status, requested_by, decided_by, submitted_at, decided_at, created_at, updated_at
                        """)
                .bind("id", instance.id())
                .bind("tenantId", instance.tenantId())
                .bind("approvalStatus", instance.status().name())
                .bind("updatedAt", instance.updatedAt());

        spec = bindNullable(spec, "decidedBy", instance.decidedBy(), String.class);
        spec = bindNullable(spec, "decidedAt", instance.decidedAt(), Instant.class);

        return spec.map((row, metadata) -> mapInstance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("workflow_definition_id", UUID.class),
                        row.get("workflow_key", String.class),
                        row.get("target_type", String.class),
                        row.get("target_id", String.class),
                        row.get("approval_status", String.class),
                        row.get("requested_by", String.class),
                        row.get("decided_by", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("decided_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WorkflowInstance> findInstanceById(String tenantId, UUID workflowInstanceId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, workflow_definition_id, workflow_key, target_type, target_id,
                               approval_status, requested_by, decided_by, submitted_at, decided_at, created_at, updated_at
                        FROM workflow.workflow_instances
                        WHERE tenant_id = :tenantId
                          AND id = :id
                        """)
                .bind("tenantId", tenantId)
                .bind("id", workflowInstanceId)
                .map((row, metadata) -> mapInstance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("workflow_definition_id", UUID.class),
                        row.get("workflow_key", String.class),
                        row.get("target_type", String.class),
                        row.get("target_id", String.class),
                        row.get("approval_status", String.class),
                        row.get("requested_by", String.class),
                        row.get("decided_by", String.class),
                        row.get("submitted_at", Instant.class),
                        row.get("decided_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Integer> countSteps(String tenantId, UUID workflowInstanceId) {
        return databaseClient.sql("""
                        SELECT COUNT(*) AS total
                        FROM workflow.workflow_steps
                        WHERE tenant_id = :tenantId
                          AND workflow_instance_id = :workflowInstanceId
                        """)
                .bind("tenantId", tenantId)
                .bind("workflowInstanceId", workflowInstanceId)
                .map((row, metadata) -> row.get("total", Long.class))
                .one()
                .map(Long::intValue)
                .defaultIfEmpty(0);
    }

    @Override
    public Mono<WorkflowStep> saveStep(String tenantId, WorkflowStep step) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO workflow.workflow_steps(
                            id, tenant_id, workflow_instance_id, step_order, actor, workflow_action, comments, acted_at, created_at
                        ) VALUES (
                            :id, :tenantId, :workflowInstanceId, :stepOrder, :actor, :workflowAction, :comments, :actedAt, :createdAt
                        )
                        RETURNING id, workflow_instance_id, step_order, actor, workflow_action, comments, acted_at, created_at
                        """)
                .bind("id", step.id())
                .bind("tenantId", tenantId)
                .bind("workflowInstanceId", step.workflowInstanceId())
                .bind("stepOrder", step.stepOrder())
                .bind("actor", step.actor())
                .bind("workflowAction", step.action().name())
                .bind("actedAt", step.actedAt())
                .bind("createdAt", step.createdAt());

        spec = bindNullable(spec, "comments", step.comments(), String.class);

        return spec.map((row, metadata) -> new WorkflowStep(
                        row.get("id", UUID.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("step_order", Integer.class),
                        row.get("actor", String.class),
                        WorkflowAction.valueOf(row.get("workflow_action", String.class)),
                        row.get("comments", String.class),
                        row.get("acted_at", Instant.class),
                        row.get("created_at", Instant.class)))
                .one();
    }

    private WorkflowInstance mapInstance(
            UUID id,
            String tenantId,
            UUID workflowDefinitionId,
            String workflowKey,
            String targetType,
            String targetId,
            String approvalStatus,
            String requestedBy,
            String decidedBy,
            Instant submittedAt,
            Instant decidedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new WorkflowInstance(
                id,
                tenantId,
                workflowDefinitionId,
                workflowKey,
                targetType,
                targetId,
                ApprovalStatus.valueOf(approvalStatus),
                requestedBy,
                decidedBy,
                submittedAt,
                decidedAt,
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
