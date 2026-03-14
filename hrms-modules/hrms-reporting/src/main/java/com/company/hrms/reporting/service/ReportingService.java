package com.company.hrms.reporting.service;

import com.company.hrms.reporting.model.*;

import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import com.company.hrms.reporting.model.ReportDateRangeQueryDto;

import reactor.core.publisher.Mono;

public interface ReportingService {

    Mono<EmployeeSummaryReportViewDto> employeeSummary();

    Mono<AttendanceSummaryReportViewDto> attendanceSummary(ReportDateRangeQueryDto query);

    Mono<LeaveSummaryReportViewDto> leaveSummary(ReportDateRangeQueryDto query);

    Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(ReportDateRangeQueryDto query);
}
