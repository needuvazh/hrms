package com.company.hrms.pasi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiContributionRule(
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
