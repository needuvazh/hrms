package com.company.hrms.platform.audit.infrastructure;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.service.AuditEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import reactor.core.publisher.Mono;

public class R2dbcAuditEventRepository implements AuditEventRepository {

    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper;

    public R2dbcAuditEventRepository(DatabaseClient databaseClient, ObjectMapper objectMapper) {
        this.databaseClient = databaseClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> append(AuditEvent event) {
        return Mono.fromCallable(() -> new SerializedAuditPayload(
                        toJson(event.metadata()),
                        toJson(event.changedFields()),
                        toJson(event.oldValues()),
                        toJson(event.newValues())))
                .flatMap(payload -> {
                    GenericExecuteSpec spec = databaseClient.sql("""
                                INSERT INTO audit.audit_event_log(
                                    actor,
                                    tenant_id,
                                    action,
                                    target_type,
                                    target_id,
                                    module_name,
                                    entity_version,
                                    changed_fields,
                                    old_values,
                                    new_values,
                                    changed_by_actor_type,
                                    changed_by_actor_id,
                                    approved_by_actor_id,
                                    change_reason,
                                    source_service,
                                    source_event_id,
                                    request_id,
                                    transaction_id,
                                    workflow_id,
                                    correlation_id,
                                    event_timestamp,
                                    metadata,
                                    legal_hold_flag
                                ) VALUES (
                                    :actor,
                                    :tenantId,
                                    :action,
                                    :targetType,
                                    :targetId,
                                    :moduleName,
                                    :entityVersion,
                                    CAST(:changedFields AS JSONB),
                                    CAST(:oldValues AS JSONB),
                                    CAST(:newValues AS JSONB),
                                    :changedByActorType,
                                    :changedByActorId,
                                    :approvedByActorId,
                                    :changeReason,
                                    :sourceService,
                                    :sourceEventId,
                                    :requestId,
                                    :transactionId,
                                    :workflowId,
                                    :correlationId,
                                    :eventTimestamp,
                                    CAST(:metadata AS JSONB),
                                    :legalHoldFlag
                                )
                                RETURNING id
                                """);

                    spec = spec.bind("actor", event.actor())
                            .bind("tenantId", event.tenant())
                            .bind("action", event.action())
                            .bind("targetType", event.targetType())
                            .bind("targetId", event.targetId())
                            .bind("moduleName", event.moduleName())
                            .bind("changedFields", payload.changedFieldsJson())
                            .bind("oldValues", payload.oldValuesJson())
                            .bind("newValues", payload.newValuesJson())
                            .bind("changedByActorType", event.changedByActorType())
                            .bind("changedByActorId", event.changedByActorId())
                            .bind("sourceService", event.sourceService())
                            .bind("eventTimestamp", event.timestamp())
                            .bind("metadata", payload.metadataJson())
                            .bind("legalHoldFlag", event.legalHold());

                    spec = bindNullable(spec, "entityVersion", event.entityVersion(), Long.class);
                    spec = bindNullable(spec, "approvedByActorId", event.approvedByActorId(), String.class);
                    spec = bindNullable(spec, "changeReason", event.changeReason(), String.class);
                    spec = bindNullable(spec, "sourceEventId", event.sourceEventId(), String.class);
                    spec = bindNullable(spec, "requestId", event.requestId(), String.class);
                    spec = bindNullable(spec, "transactionId", event.transactionId(), String.class);
                    spec = bindNullable(spec, "workflowId", event.workflowId(), String.class);
                    spec = bindNullable(spec, "correlationId", event.correlationId(), String.class);

                    return spec.map((row, metadata) -> row.get("id", Long.class))
                            .one()
                            .flatMap(auditEventId -> appendModuleHistory(event, auditEventId, payload));
                });
    }

    private Mono<Void> appendModuleHistory(AuditEvent event, Long auditEventId, SerializedAuditPayload payload) {
        if (auditEventId == null) {
            return Mono.empty();
        }

        return switch (event.moduleName()) {
            case "person" -> insertPersonAuditHistory(event, auditEventId, payload);
            case "recruitment" -> insertRecruitmentAuditHistory(event, auditEventId, payload);
            case "employee" -> insertEmployeeAuditHistory(event, auditEventId, payload);
            default -> Mono.empty();
        };
    }

    private Mono<Void> insertPersonAuditHistory(AuditEvent event, Long auditEventId, SerializedAuditPayload payload) {
        UUID personId = tryParseUuid(event.targetId());
        if (personId == null) {
            return Mono.empty();
        }

        return databaseClient.sql("""
                        INSERT INTO audit.person_audit_history(
                            audit_event_id,
                            tenant_id,
                            person_id,
                            action_type,
                            effective_at,
                            old_snapshot,
                            new_snapshot
                        ) VALUES (
                            :auditEventId,
                            :tenantId,
                            :personId,
                            :actionType,
                            :effectiveAt,
                            CAST(:oldSnapshot AS JSONB),
                            CAST(:newSnapshot AS JSONB)
                        )
                        """)
                .bind("auditEventId", auditEventId)
                .bind("tenantId", event.tenant())
                .bind("personId", personId)
                .bind("actionType", event.action())
                .bind("effectiveAt", event.timestamp())
                .bind("oldSnapshot", payload.oldValuesJson())
                .bind("newSnapshot", payload.newValuesJson())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Void> insertRecruitmentAuditHistory(AuditEvent event, Long auditEventId, SerializedAuditPayload payload) {
        UUID candidateId = tryParseUuid(event.targetId());
        if (candidateId == null) {
            return Mono.empty();
        }

        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO audit.recruitment_audit_history(
                            audit_event_id,
                            tenant_id,
                            candidate_id,
                            application_id,
                            action_type,
                            status_from,
                            status_to,
                            details,
                            occurred_at
                        ) VALUES (
                            :auditEventId,
                            :tenantId,
                            :candidateId,
                            :applicationId,
                            :actionType,
                            :statusFrom,
                            :statusTo,
                            CAST(:details AS JSONB),
                            :occurredAt
                        )
                        """)
                .bind("auditEventId", auditEventId)
                .bind("tenantId", event.tenant())
                .bind("candidateId", candidateId)
                .bind("actionType", event.action())
                .bind("details", payload.metadataJson())
                .bind("occurredAt", event.timestamp());

        spec = bindNullable(spec, "applicationId", null, UUID.class);
        spec = bindNullable(spec, "statusFrom", readAsString(event.oldValues(), "status"), String.class);
        spec = bindNullable(spec, "statusTo", readAsString(event.newValues(), "status"), String.class);

        return spec.fetch().rowsUpdated().then();
    }

    private Mono<Void> insertEmployeeAuditHistory(AuditEvent event, Long auditEventId, SerializedAuditPayload payload) {
        UUID employeeId = tryParseUuid(event.targetId());
        if (employeeId == null) {
            return Mono.empty();
        }

        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO audit.employee_audit_history(
                            audit_event_id,
                            tenant_id,
                            employee_id,
                            person_id,
                            action_type,
                            old_snapshot,
                            new_snapshot,
                            occurred_at
                        ) VALUES (
                            :auditEventId,
                            :tenantId,
                            :employeeId,
                            :personId,
                            :actionType,
                            CAST(:oldSnapshot AS JSONB),
                            CAST(:newSnapshot AS JSONB),
                            :occurredAt
                        )
                        """)
                .bind("auditEventId", auditEventId)
                .bind("tenantId", event.tenant())
                .bind("employeeId", employeeId)
                .bind("actionType", event.action())
                .bind("oldSnapshot", payload.oldValuesJson())
                .bind("newSnapshot", payload.newValuesJson())
                .bind("occurredAt", event.timestamp());

        UUID personId = tryParseUuid(readAsString(event.newValues(), "personId"));
        spec = bindNullable(spec, "personId", personId, UUID.class);

        return spec.fetch().rowsUpdated().then();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize audit metadata", e);
        }
    }

    private record SerializedAuditPayload(
            String metadataJson,
            String changedFieldsJson,
            String oldValuesJson,
            String newValuesJson
    ) {
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }

    private UUID tryParseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String readAsString(java.util.Map<String, Object> payload, String key) {
        if (payload == null || !payload.containsKey(key) || payload.get(key) == null) {
            return null;
        }
        return String.valueOf(payload.get(key));
    }
}
