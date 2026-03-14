package com.company.hrms.pasi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiPeriodRecord(
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

    public PasiPeriodRecord fail(Instant at) {
        return new PasiPeriodRecord(
                id,
                tenantId,
                payrollRunId,
                periodCode,
                contributionRuleId,
                PasiStatus.FAILED,
                totalEmployees,
                totalEmployeeContribution,
                totalEmployerContribution,
                totalContribution,
                calculatedBy,
                calculatedAt,
                createdAt,
                at);
    }
}
