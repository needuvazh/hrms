package com.company.hrms.platform.audit.service;

import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

public class DefaultAuditEventPublisher implements AuditEventPublisher {

    private final AuditEventRepository auditEventRepository;

    public DefaultAuditEventPublisher(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public Mono<Void> publish(AuditEvent event) {
        AuditEvent sanitized = new AuditEvent(
                sanitize(event.actor()),
                sanitize(event.tenant()),
                sanitize(event.action()),
                sanitize(event.targetType()),
                sanitize(event.targetId()),
                event.timestamp(),
                event.metadata() == null ? Map.of() : event.metadata(),
                sanitize(event.moduleName(), "generic"),
                event.entityVersion(),
                event.changedFields() == null ? List.of() : event.changedFields(),
                event.oldValues() == null ? Map.of() : event.oldValues(),
                event.newValues() == null ? Map.of() : event.newValues(),
                sanitize(event.changedByActorType(), "user"),
                sanitize(event.changedByActorId(), sanitize(event.actor())),
                event.approvedByActorId(),
                event.changeReason(),
                sanitize(event.sourceService(), "application"),
                event.sourceEventId(),
                event.requestId(),
                event.transactionId(),
                event.workflowId(),
                event.correlationId(),
                event.legalHold());
        return auditEventRepository.append(sanitized);
    }

    private String sanitize(String value) {
        return value == null ? "system" : value;
    }

    private String sanitize(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
