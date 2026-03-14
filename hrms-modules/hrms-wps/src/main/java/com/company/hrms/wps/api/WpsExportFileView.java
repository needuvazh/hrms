package com.company.hrms.wps.api;

import com.company.hrms.wps.domain.WpsStatus;
import java.time.Instant;
import java.util.UUID;

public record WpsExportFileView(
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
