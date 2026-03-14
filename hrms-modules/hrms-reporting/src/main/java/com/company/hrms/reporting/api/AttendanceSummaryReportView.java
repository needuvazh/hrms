package com.company.hrms.reporting.api;

import java.time.Instant;
import java.time.LocalDate;

public record AttendanceSummaryReportView(
        String tenantId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalRecords,
        long presentCount,
        long absentCount,
        long missedPunchCount,
        long inProgressCount,
        Instant generatedAt
) {
}
