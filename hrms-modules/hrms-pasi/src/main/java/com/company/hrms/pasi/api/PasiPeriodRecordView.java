package com.company.hrms.pasi.api;

import com.company.hrms.pasi.domain.PasiStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiPeriodRecordView(
        UUID id,
        String tenantId,
        UUID payrollRunId,
        String periodCode,
        UUID contributionRuleId,
        PasiStatus status,
        int totalEmployees,
        BigDecimal totalEmployeeContribution,
        BigDecimal totalEmployerContribution,
        BigDecimal totalContribution,
        String calculatedBy,
        Instant calculatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
