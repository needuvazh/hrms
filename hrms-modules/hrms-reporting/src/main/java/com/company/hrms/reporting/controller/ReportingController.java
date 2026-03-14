package com.company.hrms.reporting.controller;

import com.company.hrms.reporting.model.*;
import com.company.hrms.reporting.service.*;

import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import com.company.hrms.reporting.model.ReportDateRangeQueryDto;
import com.company.hrms.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reporting", description = "Operational summary reporting APIs")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/employees/summary")
    @Operation(summary = "EmployeeDto summary report", description = "Returns aggregated employee counts and key composition metrics.")
    public Mono<EmployeeSummaryReportViewDto> employeeSummary() {
        return reportingService.employeeSummary();
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Attendance summary report", description = "Returns attendance summary metrics for the provided date range.")
    public Mono<AttendanceSummaryReportViewDto> attendanceSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingService.attendanceSummary(new ReportDateRangeQueryDto(fromDate, toDate));
    }

    @GetMapping("/leave/summary")
    @Operation(summary = "Leave summary report", description = "Returns leave utilization and leave status totals for the provided date range.")
    public Mono<LeaveSummaryReportViewDto> leaveSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingService.leaveSummary(new ReportDateRangeQueryDto(fromDate, toDate));
    }

    @GetMapping("/payroll-runs/summary")
    @Operation(summary = "Payroll run summary report", description = "Returns payroll run totals and status distribution for the provided date range.")
    public Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingService.payrollRunSummary(new ReportDateRangeQueryDto(fromDate, toDate));
    }
}
