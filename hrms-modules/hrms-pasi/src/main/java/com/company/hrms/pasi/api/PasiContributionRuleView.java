package com.company.hrms.pasi.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiContributionRuleView(
        UUID id,
        String tenantId,
        String ruleCode,
        String name,
        BigDecimal employeeRatePercent,
        BigDecimal employerRatePercent,
        BigDecimal salaryCap,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
