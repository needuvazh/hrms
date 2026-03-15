package com.company.hrms.masterdata.saas.api;

import java.time.Instant;

public record AuditLogViewDto(
        long id,
        String actor,
        String tenantId,
        String action,
        String targetType,
        String targetId,
        Instant eventTimestamp,
        String metadata
) {
}
