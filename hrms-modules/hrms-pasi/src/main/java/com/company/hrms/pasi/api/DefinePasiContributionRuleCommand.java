package com.company.hrms.pasi.api;

import java.math.BigDecimal;

public record DefinePasiContributionRuleCommand(
        String ruleCode,
        String name,
        BigDecimal employeeRatePercent,
        BigDecimal employerRatePercent,
        BigDecimal salaryCap,
        boolean active
) {
}
