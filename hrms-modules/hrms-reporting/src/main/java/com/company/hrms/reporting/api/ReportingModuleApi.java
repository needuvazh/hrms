package com.company.hrms.reporting.api;

import reactor.core.publisher.Mono;

public interface ReportingModuleApi {

    Mono<EmployeeSummaryReportView> employeeSummary();

    Mono<AttendanceSummaryReportView> attendanceSummary(ReportDateRangeQuery query);

    Mono<LeaveSummaryReportView> leaveSummary(ReportDateRangeQuery query);

    Mono<PayrollRunSummaryReportView> payrollRunSummary(ReportDateRangeQuery query);
}
