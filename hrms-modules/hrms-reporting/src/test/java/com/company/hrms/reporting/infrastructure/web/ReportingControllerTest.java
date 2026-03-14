package com.company.hrms.reporting.infrastructure.web;

import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import com.company.hrms.reporting.api.AttendanceSummaryReportView;
import com.company.hrms.reporting.api.EmployeeSummaryReportView;
import com.company.hrms.reporting.api.LeaveSummaryReportView;
import com.company.hrms.reporting.api.PayrollRunSummaryReportView;
import com.company.hrms.reporting.api.ReportDateRangeQuery;
import com.company.hrms.reporting.api.ReportingModuleApi;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

class ReportingControllerTest {

    private final ReportingController controller = new ReportingController(new StubReportingModuleApi());

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

    private static class StubReportingModuleApi implements ReportingModuleApi {

        @Override
        public Mono<EmployeeSummaryReportView> employeeSummary() {
            return Mono.just(new EmployeeSummaryReportView("default", 12L, 2L, Instant.now()));
        }

        @Override
        public Mono<AttendanceSummaryReportView> attendanceSummary(ReportDateRangeQuery query) {
            return Mono.just(new AttendanceSummaryReportView("default", query.fromDate(), query.toDate(), 9L, 7L, 1L, 1L, 0L, Instant.now()));
        }

        @Override
        public Mono<LeaveSummaryReportView> leaveSummary(ReportDateRangeQuery query) {
            return Mono.just(new LeaveSummaryReportView(
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
        public Mono<PayrollRunSummaryReportView> payrollRunSummary(ReportDateRangeQuery query) {
            return Mono.just(new PayrollRunSummaryReportView(
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
