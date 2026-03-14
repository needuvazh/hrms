package com.company.hrms.reporting.repository;

import com.company.hrms.reporting.model.*;

import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class ReportingRepositoryImpl implements ReportingRepository {

    private final DatabaseClient databaseClient;

    public ReportingRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<EmployeeSummaryReportViewDto> employeeSummary(String tenantId) {
        return databaseClient.sql("""
                        SELECT
                            COUNT(*) AS total_employees,
                            COUNT(*) FILTER (WHERE created_at >= NOW() - INTERVAL '30 days') AS joined_last_30_days
                        FROM employee.employees
                        WHERE tenant_id = :tenantId
                        """)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new EmployeeSummaryReportViewDto(
                        tenantId,
                        readLong(row.get("total_employees", Number.class)),
                        readLong(row.get("joined_last_30_days", Number.class)),
                        Instant.now()))
                .one()
                .switchIfEmpty(Mono.fromSupplier(() -> new EmployeeSummaryReportViewDto(tenantId, 0L, 0L, Instant.now())));
    }

    @Override
    public Mono<AttendanceSummaryReportViewDto> attendanceSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
        return databaseClient.sql("""
                        SELECT
                            COUNT(*) AS total_records,
                            COUNT(*) FILTER (WHERE attendance_status = 'PRESENT') AS present_count,
                            COUNT(*) FILTER (WHERE attendance_status = 'ABSENT') AS absent_count,
                            COUNT(*) FILTER (WHERE attendance_status = 'MISSED_PUNCH') AS missed_punch_count,
                            COUNT(*) FILTER (WHERE attendance_status = 'IN_PROGRESS') AS in_progress_count
                        FROM attendance.attendance_records
                        WHERE tenant_id = :tenantId
                          AND attendance_date BETWEEN :fromDate AND :toDate
                        """)
                .bind("tenantId", tenantId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> new AttendanceSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        readLong(row.get("total_records", Number.class)),
                        readLong(row.get("present_count", Number.class)),
                        readLong(row.get("absent_count", Number.class)),
                        readLong(row.get("missed_punch_count", Number.class)),
                        readLong(row.get("in_progress_count", Number.class)),
                        Instant.now()))
                .one()
                .switchIfEmpty(Mono.fromSupplier(() -> new AttendanceSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        Instant.now())));
    }

    @Override
    public Mono<LeaveSummaryReportViewDto> leaveSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
        return databaseClient.sql("""
                        SELECT
                            COUNT(*) AS total_requests,
                            COUNT(*) FILTER (WHERE leave_status = 'SUBMITTED') AS submitted_count,
                            COUNT(*) FILTER (WHERE leave_status = 'APPROVED') AS approved_count,
                            COUNT(*) FILTER (WHERE leave_status = 'REJECTED') AS rejected_count,
                            COUNT(*) FILTER (WHERE leave_status = 'CANCELLED') AS cancelled_count,
                            COALESCE(SUM(requested_days), 0) AS total_requested_days
                        FROM leave.leave_requests
                        WHERE tenant_id = :tenantId
                          AND from_date BETWEEN :fromDate AND :toDate
                        """)
                .bind("tenantId", tenantId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> new LeaveSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        readLong(row.get("total_requests", Number.class)),
                        readLong(row.get("submitted_count", Number.class)),
                        readLong(row.get("approved_count", Number.class)),
                        readLong(row.get("rejected_count", Number.class)),
                        readLong(row.get("cancelled_count", Number.class)),
                        readLong(row.get("total_requested_days", Number.class)),
                        Instant.now()))
                .one()
                .switchIfEmpty(Mono.fromSupplier(() -> new LeaveSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        Instant.now())));
    }

    @Override
    public Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
        return databaseClient.sql("""
                        SELECT
                            COUNT(*) AS total_runs,
                            COUNT(*) FILTER (WHERE pr.status = 'DRAFT') AS draft_runs,
                            COUNT(*) FILTER (WHERE pr.status = 'SUBMITTED') AS submitted_runs,
                            COUNT(*) FILTER (WHERE pr.status = 'APPROVED') AS approved_runs,
                            COUNT(*) FILTER (WHERE pr.status = 'REJECTED') AS rejected_runs,
                            COUNT(*) FILTER (WHERE pr.status = 'FINALIZED') AS finalized_runs,
                            COALESCE(SUM(per.gross_amount), 0) AS total_gross_amount,
                            COALESCE(SUM(per.net_amount), 0) AS total_net_amount
                        FROM payroll.payroll_runs pr
                        LEFT JOIN payroll.payroll_employee_records per
                               ON per.payroll_run_id = pr.id
                              AND per.tenant_id = pr.tenant_id
                        WHERE pr.tenant_id = :tenantId
                          AND DATE(pr.created_at) BETWEEN :fromDate AND :toDate
                        """)
                .bind("tenantId", tenantId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> new PayrollRunSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        readLong(row.get("total_runs", Number.class)),
                        readLong(row.get("draft_runs", Number.class)),
                        readLong(row.get("submitted_runs", Number.class)),
                        readLong(row.get("approved_runs", Number.class)),
                        readLong(row.get("rejected_runs", Number.class)),
                        readLong(row.get("finalized_runs", Number.class)),
                        readBigDecimal(row.get("total_gross_amount", BigDecimal.class)),
                        readBigDecimal(row.get("total_net_amount", BigDecimal.class)),
                        Instant.now()))
                .one()
                .switchIfEmpty(Mono.fromSupplier(() -> new PayrollRunSummaryReportViewDto(
                        tenantId,
                        fromDate,
                        toDate,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        Instant.now())));
    }

    private long readLong(Number number) {
        return number == null ? 0L : number.longValue();
    }

    private BigDecimal readBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
