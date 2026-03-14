package com.company.hrms.wps.domain;

import java.time.Instant;
import java.util.UUID;

public record WpsBatch(
        UUID id,
        String tenantId,
        UUID payrollRunId,
        WpsStatus status,
        String validationSummary,
        String createdBy,
        String exportedBy,
        Instant exportedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public WpsBatch validated(String summary, Instant at) {
        return new WpsBatch(
                id,
                tenantId,
                payrollRunId,
                WpsStatus.VALIDATED,
                summary,
                createdBy,
                exportedBy,
                exportedAt,
                createdAt,
                at);
    }

    public WpsBatch generated(Instant at) {
        return new WpsBatch(
                id,
                tenantId,
                payrollRunId,
                WpsStatus.GENERATED,
                validationSummary,
                createdBy,
                exportedBy,
                exportedAt,
                createdAt,
                at);
    }

    public WpsBatch exported(String actor, Instant at) {
        return new WpsBatch(
                id,
                tenantId,
                payrollRunId,
                WpsStatus.EXPORTED,
                validationSummary,
                createdBy,
                actor,
                at,
                createdAt,
                at);
    }

    public WpsBatch failed(String summary, Instant at) {
        return new WpsBatch(
                id,
                tenantId,
                payrollRunId,
                WpsStatus.FAILED,
                summary,
                createdBy,
                exportedBy,
                exportedAt,
                createdAt,
                at);
    }
}
