package com.company.hrms.reporting.api;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveSummaryReportView(
        String tenantId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalRequests,
        long submittedCount,
        long approvedCount,
        long rejectedCount,
        long cancelledCount,
        long totalRequestedDays,
        Instant generatedAt
) {
}
