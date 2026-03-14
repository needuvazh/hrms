package com.company.hrms.wps.model;

import java.time.Instant;
import java.util.UUID;

public final class WpsEvents {

    private WpsEvents() {
    }

    public record WpsBatchCreatedEvent(UUID batchId, String tenantId, UUID payrollRunId, Instant occurredAt) {
    }

    public record WpsExportGeneratedEvent(UUID exportId, String tenantId, UUID batchId, Instant occurredAt) {
    }

    public record WpsExportedEvent(UUID batchId, String tenantId, String actor, Instant occurredAt) {
    }
}
