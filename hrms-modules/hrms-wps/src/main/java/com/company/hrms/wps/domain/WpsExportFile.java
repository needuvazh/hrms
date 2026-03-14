package com.company.hrms.wps.domain;

import java.time.Instant;
import java.util.UUID;

public record WpsExportFile(
        UUID id,
        String tenantId,
        UUID wpsBatchId,
        String exportType,
        String fileName,
        String contentType,
        String contentHash,
        String payload,
        WpsStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
