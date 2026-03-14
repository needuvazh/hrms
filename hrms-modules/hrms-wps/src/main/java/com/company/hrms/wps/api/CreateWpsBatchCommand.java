package com.company.hrms.wps.api;

import java.util.UUID;

public record CreateWpsBatchCommand(
        UUID payrollRunId,
        String createdBy
) {
}
