package com.company.hrms.pasi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiPeriodRecordDto(
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

    public PasiPeriodRecordDto fail(Instant at) {
        return new PasiPeriodRecordDto(
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
