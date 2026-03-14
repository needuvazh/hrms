package com.company.hrms.reporting.domain;

import com.company.hrms.reporting.api.AttendanceSummaryReportView;
import com.company.hrms.reporting.api.EmployeeSummaryReportView;
import com.company.hrms.reporting.api.LeaveSummaryReportView;
import com.company.hrms.reporting.api.PayrollRunSummaryReportView;
import java.time.LocalDate;
import reactor.core.publisher.Mono;

public interface ReportingQueryRepository {

    Mono<EmployeeSummaryReportView> employeeSummary(String tenantId);

    Mono<AttendanceSummaryReportView> attendanceSummary(String tenantId, LocalDate fromDate, LocalDate toDate);

    Mono<LeaveSummaryReportView> leaveSummary(String tenantId, LocalDate fromDate, LocalDate toDate);

    Mono<PayrollRunSummaryReportView> payrollRunSummary(String tenantId, LocalDate fromDate, LocalDate toDate);
}
