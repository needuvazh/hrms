package com.company.hrms.wps.api;

import java.util.UUID;

public record MarkWpsExportedCommand(
        UUID batchId,
        String exportedBy
) {
}
