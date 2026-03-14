package com.company.hrms.reporting.application;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.reporting.api.AttendanceSummaryReportView;
import com.company.hrms.reporting.api.EmployeeSummaryReportView;
import com.company.hrms.reporting.api.LeaveSummaryReportView;
import com.company.hrms.reporting.api.PayrollRunSummaryReportView;
import com.company.hrms.reporting.api.ReportDateRangeQuery;
import com.company.hrms.reporting.domain.ReportingQueryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportingApplicationServiceTest {

    private final ReportingApplicationService reportingApplicationService = new ReportingApplicationService(
            new StubReportingQueryRepository(),
            new DefaultTenantContextAccessor());

    @Test
    void resolvesEmployeeSummaryForCurrentTenant() {
        StepVerifier.create(reportingApplicationService.employeeSummary()
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .assertNext(report -> {
                    assertEquals("tenant-a", report.tenantId());
                    assertEquals(5L, report.totalEmployees());
                })
                .verifyComplete();
    }

    @Test
    void rejectsMissingTenantContext() {
        StepVerifier.create(reportingApplicationService.employeeSummary())
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("TENANT_REQUIRED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void rejectsInvalidDateRange() {
        HrmsException exception = assertThrows(HrmsException.class, () -> reportingApplicationService.attendanceSummary(
                new ReportDateRangeQuery(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1))));
        assertEquals("DATE_RANGE_INVALID", exception.getErrorCode());
    }

    private static class StubReportingQueryRepository implements ReportingQueryRepository {

        @Override
        public Mono<EmployeeSummaryReportView> employeeSummary(String tenantId) {
            long count = "tenant-a".equals(tenantId) ? 5L : 2L;
            return Mono.just(new EmployeeSummaryReportView(tenantId, count, 1L, Instant.now()));
        }

        @Override
        public Mono<AttendanceSummaryReportView> attendanceSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new AttendanceSummaryReportView(tenantId, fromDate, toDate, 8L, 6L, 1L, 1L, 0L, Instant.now()));
        }

        @Override
        public Mono<LeaveSummaryReportView> leaveSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new LeaveSummaryReportView(tenantId, fromDate, toDate, 4L, 1L, 2L, 1L, 0L, 6L, Instant.now()));
        }

        @Override
        public Mono<PayrollRunSummaryReportView> payrollRunSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new PayrollRunSummaryReportView(
                    tenantId,
                    fromDate,
                    toDate,
                    1L,
                    0L,
                    0L,
                    0L,
                    0L,
                    1L,
                    new BigDecimal("12000.00"),
                    new BigDecimal("9500.00"),
                    Instant.now()));
        }
    }
}
