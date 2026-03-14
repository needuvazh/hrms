package com.company.hrms.reporting.repository;

import com.company.hrms.reporting.model.*;

import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import java.time.LocalDate;
import reactor.core.publisher.Mono;

public interface ReportingRepository {

    Mono<EmployeeSummaryReportViewDto> employeeSummary(String tenantId);

    Mono<AttendanceSummaryReportViewDto> attendanceSummary(String tenantId, LocalDate fromDate, LocalDate toDate);

    Mono<LeaveSummaryReportViewDto> leaveSummary(String tenantId, LocalDate fromDate, LocalDate toDate);

    Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(String tenantId, LocalDate fromDate, LocalDate toDate);
}
