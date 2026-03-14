package com.company.hrms.platform.audit.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record AuditEvent(
        String actor,
        String tenant,
        String action,
        String targetType,
        String targetId,
        Instant timestamp,
        Map<String, Object> metadata,
        String moduleName,
        Long entityVersion,
        List<String> changedFields,
        Map<String, Object> oldValues,
        Map<String, Object> newValues,
        String changedByActorType,
        String changedByActorId,
        String approvedByActorId,
        String changeReason,
        String sourceService,
        String sourceEventId,
        String requestId,
        String transactionId,
        String workflowId,
        String correlationId,
        boolean legalHold
) {

    public AuditEvent(
            String actor,
            String tenant,
            String action,
            String targetType,
            String targetId,
            Instant timestamp,
            Map<String, ?> metadata
    ) {
        this(
                actor,
                tenant,
                action,
                targetType,
                targetId,
                timestamp,
                castMetadata(metadata),
                "generic",
                null,
                List.of(),
                Map.of(),
                Map.of(),
                "user",
                actor,
                null,
                null,
                "application",
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public static AuditEvent of(
            String actor,
            String tenant,
            String action,
            String targetType,
            String targetId,
            Map<String, ?> metadata
    ) {
        return new AuditEvent(actor, tenant, action, targetType, targetId, Instant.now(), castMetadata(metadata));
    }

    public static AuditEvent detailed(
            String actor,
            String tenant,
            String action,
            String targetType,
            String targetId,
            String moduleName,
            Long entityVersion,
            List<String> changedFields,
            Map<String, ?> oldValues,
            Map<String, ?> newValues,
            String changedByActorType,
            String changedByActorId,
            String approvedByActorId,
            String changeReason,
            String sourceService,
            String sourceEventId,
            String requestId,
            String transactionId,
            String workflowId,
            String correlationId,
            Map<String, ?> metadata,
            boolean legalHold
    ) {
        return new AuditEvent(
                actor,
                tenant,
                action,
                targetType,
                targetId,
                Instant.now(),
                castMetadata(metadata),
                moduleName,
                entityVersion,
                changedFields == null ? List.of() : changedFields,
                castMetadata(oldValues),
                castMetadata(newValues),
                changedByActorType,
                changedByActorId,
                approvedByActorId,
                changeReason,
                sourceService,
                sourceEventId,
                requestId,
                transactionId,
                workflowId,
                correlationId,
                legalHold
        );
    }

    private static Map<String, Object> castMetadata(Map<String, ?> metadata) {
        if (metadata == null) {
            return Map.of();
        }
        return metadata.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
