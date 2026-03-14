package com.company.hrms.reporting.api;

import java.time.Instant;

public record EmployeeSummaryReportView(
        String tenantId,
        long totalEmployees,
        long joinedLast30Days,
        Instant generatedAt
) {
}
