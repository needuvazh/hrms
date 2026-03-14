package com.company.hrms.wps.model;

import java.time.Instant;
import java.util.UUID;

public record WpsBatchDto(
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

    public WpsBatchDto validated(String summary, Instant at) {
        return new WpsBatchDto(
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

    public WpsBatchDto generated(Instant at) {
        return new WpsBatchDto(
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

    public WpsBatchDto exported(String actor, Instant at) {
        return new WpsBatchDto(
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

    public WpsBatchDto failed(String summary, Instant at) {
        return new WpsBatchDto(
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
