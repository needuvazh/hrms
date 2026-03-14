package com.company.hrms.document.events;

import java.time.Instant;
import java.util.UUID;

public final class DocumentEvents {

    private DocumentEvents() {
    }

    public record DocumentAttachedEvent(UUID documentId, String tenantId, String entityType, String entityId, Instant occurredAt) {
    }

    public record DocumentArchivedEvent(UUID documentId, String tenantId, Instant occurredAt) {
    }
}
