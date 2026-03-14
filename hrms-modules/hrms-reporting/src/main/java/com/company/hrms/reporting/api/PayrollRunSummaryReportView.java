package com.company.hrms.reporting.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PayrollRunSummaryReportView(
        String tenantId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalRuns,
        long draftRuns,
        long submittedRuns,
        long approvedRuns,
        long rejectedRuns,
        long finalizedRuns,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        Instant generatedAt
) {
}
