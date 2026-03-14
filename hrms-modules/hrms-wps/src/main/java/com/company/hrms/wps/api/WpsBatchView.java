package com.company.hrms.wps.api;

import com.company.hrms.wps.domain.WpsStatus;
import java.time.Instant;
import java.util.UUID;

public record WpsBatchView(
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
}
