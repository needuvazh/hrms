package com.company.hrms.reporting.controller;

import com.company.hrms.reporting.model.*;
import com.company.hrms.reporting.service.*;

import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import com.company.hrms.reporting.model.AttendanceSummaryReportViewDto;
import com.company.hrms.reporting.model.EmployeeSummaryReportViewDto;
import com.company.hrms.reporting.model.LeaveSummaryReportViewDto;
import com.company.hrms.reporting.model.PayrollRunSummaryReportViewDto;
import com.company.hrms.reporting.model.ReportDateRangeQueryDto;
import com.company.hrms.reporting.service.ReportingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

class ReportingControllerTest {

    private final ReportingController controller = new ReportingController(new StubReportingService());

    private final WebTestClient webTestClient = WebTestClient.bindToController(controller)
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void employeeSummaryReturnsForTenant() {
        webTestClient.get()
                .uri("/api/v1/reports/employees/summary")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tenantId").isEqualTo("default")
                .jsonPath("$.totalEmployees").isEqualTo(12);
    }

    @Test
    void attendanceSummaryReturnsDateRangeResult() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/reports/attendance/summary")
                        .queryParam("fromDate", "2026-03-01")
                        .queryParam("toDate", "2026-03-10")
                        .build())
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.fromDate[0]").isEqualTo(2026)
                .jsonPath("$.fromDate[1]").isEqualTo(3)
                .jsonPath("$.fromDate[2]").isEqualTo(1)
                .jsonPath("$.toDate[0]").isEqualTo(2026)
                .jsonPath("$.toDate[1]").isEqualTo(3)
                .jsonPath("$.toDate[2]").isEqualTo(10)
                .jsonPath("$.presentCount").isEqualTo(7);
    }

    private static class StubReportingService implements ReportingService {

        @Override
        public Mono<EmployeeSummaryReportViewDto> employeeSummary() {
            return Mono.just(new EmployeeSummaryReportViewDto("default", 12L, 2L, Instant.now()));
        }

        @Override
        public Mono<AttendanceSummaryReportViewDto> attendanceSummary(ReportDateRangeQueryDto query) {
            return Mono.just(new AttendanceSummaryReportViewDto("default", query.fromDate(), query.toDate(), 9L, 7L, 1L, 1L, 0L, Instant.now()));
        }

        @Override
        public Mono<LeaveSummaryReportViewDto> leaveSummary(ReportDateRangeQueryDto query) {
            return Mono.just(new LeaveSummaryReportViewDto(
                    "default",
                    query.fromDate(),
                    query.toDate(),
                    3L,
                    1L,
                    2L,
                    0L,
                    0L,
                    4L,
                    Instant.now()));
        }

        @Override
        public Mono<PayrollRunSummaryReportViewDto> payrollRunSummary(ReportDateRangeQueryDto query) {
            return Mono.just(new PayrollRunSummaryReportViewDto(
                    "default",
                    query.fromDate(),
                    query.toDate(),
                    1L,
                    0L,
                    0L,
                    0L,
                    0L,
                    1L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("900.00"),
                    Instant.now()));
        }
    }
}
