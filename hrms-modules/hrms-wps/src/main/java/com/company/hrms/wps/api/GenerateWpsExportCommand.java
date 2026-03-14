package com.company.hrms.wps.api;

import java.util.UUID;

public record GenerateWpsExportCommand(
        UUID batchId,
        String exportType,
        String generatedBy
) {
}
