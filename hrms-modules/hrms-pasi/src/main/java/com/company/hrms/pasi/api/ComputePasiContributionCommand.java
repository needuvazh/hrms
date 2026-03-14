package com.company.hrms.pasi.api;

import java.util.UUID;

public record ComputePasiContributionCommand(
        UUID payrollRunId,
        String periodCode,
        String calculatedBy,
        String ruleCode
) {
}
