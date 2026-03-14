package com.company.hrms.reporting.infrastructure.web;

import com.company.hrms.reporting.api.AttendanceSummaryReportView;
import com.company.hrms.reporting.api.EmployeeSummaryReportView;
import com.company.hrms.reporting.api.LeaveSummaryReportView;
import com.company.hrms.reporting.api.PayrollRunSummaryReportView;
import com.company.hrms.reporting.api.ReportDateRangeQuery;
import com.company.hrms.reporting.api.ReportingModuleApi;
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

    private final ReportingModuleApi reportingModuleApi;

    public ReportingController(ReportingModuleApi reportingModuleApi) {
        this.reportingModuleApi = reportingModuleApi;
    }

    @GetMapping("/employees/summary")
    @Operation(summary = "Employee summary report", description = "Returns aggregated employee counts and key composition metrics.")
    public Mono<EmployeeSummaryReportView> employeeSummary() {
        return reportingModuleApi.employeeSummary();
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Attendance summary report", description = "Returns attendance summary metrics for the provided date range.")
    public Mono<AttendanceSummaryReportView> attendanceSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingModuleApi.attendanceSummary(new ReportDateRangeQuery(fromDate, toDate));
    }

    @GetMapping("/leave/summary")
    @Operation(summary = "Leave summary report", description = "Returns leave utilization and leave status totals for the provided date range.")
    public Mono<LeaveSummaryReportView> leaveSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingModuleApi.leaveSummary(new ReportDateRangeQuery(fromDate, toDate));
    }

    @GetMapping("/payroll-runs/summary")
    @Operation(summary = "Payroll run summary report", description = "Returns payroll run totals and status distribution for the provided date range.")
    public Mono<PayrollRunSummaryReportView> payrollRunSummary(
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return reportingModuleApi.payrollRunSummary(new ReportDateRangeQuery(fromDate, toDate));
    }
}
