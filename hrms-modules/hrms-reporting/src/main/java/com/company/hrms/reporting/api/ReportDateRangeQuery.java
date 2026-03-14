package com.company.hrms.reporting.api;

import java.time.LocalDate;

public record ReportDateRangeQuery(LocalDate fromDate, LocalDate toDate) {
}
