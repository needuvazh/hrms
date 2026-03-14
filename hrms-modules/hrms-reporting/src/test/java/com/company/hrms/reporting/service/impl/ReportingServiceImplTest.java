package com.company.hrms.reporting.service.impl;

import com.company.hrms.reporting.model.*;
import com.company.hrms.reporting.repository.*;
import com.company.hrms.reporting.service.*;

import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import com.company.hrms.reporting.model.ReportDateRangeQueryDto;
import com.company.hrms.reporting.repository.ReportingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportingServiceImplTest {

    private final ReportingServiceImpl reportingService = new ReportingServiceImpl(
            new StubReportingRepository(),
            new DefaultTenantContextAccessor());

    @Test
    void resolvesEmployeeSummaryForCurrentTenant() {
        StepVerifier.create(reportingService.employeeSummary()
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .assertNext(report -> {
                    assertEquals("tenant-a", report.tenantId());
                    assertEquals(5L, report.totalEmployees());
                })
                .verifyComplete();
    }

    @Test
    void rejectsMissingTenantContext() {
        StepVerifier.create(reportingService.employeeSummary())
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("TENANT_REQUIRED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void rejectsInvalidDateRange() {
        HrmsException exception = assertThrows(HrmsException.class, () -> reportingService.attendanceSummary(
                new ReportDateRangeQueryDto(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1))));
        assertEquals("DATE_RANGE_INVALID", exception.getErrorCode());
    }

    private static class StubReportingRepository implements ReportingRepository {

        @Override
        public Mono<EmployeeSummaryReportViewDto> employeeSummary(String tenantId) {
            long count = "tenant-a".equals(tenantId) ? 5L : 2L;
            return Mono.just(new EmployeeSummaryReportViewDto(tenantId, count, 1L, Instant.now()));
        }

        @Override
        public Mono<AttendanceSummaryReportViewDto> attendanceSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new AttendanceSummaryReportViewDto(tenantId, fromDate, toDate, 8L, 6L, 1L, 1L, 0L, Instant.now()));
        }

        @Override
        public Mono<LeaveSummaryReportViewDto> leaveSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new LeaveSummaryReportViewDto(tenantId, fromDate, toDate, 4L, 1L, 2L, 1L, 0L, 6L, Instant.now()));
        }

        @Override
        public Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(String tenantId, LocalDate fromDate, LocalDate toDate) {
            return Mono.just(new PayrollRunSummaryReportViewDto(
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
