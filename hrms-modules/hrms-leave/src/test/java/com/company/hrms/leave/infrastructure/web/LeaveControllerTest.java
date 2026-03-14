package com.company.hrms.leave.infrastructure.web;

import com.company.hrms.leave.api.ApplyLeaveCommand;
import com.company.hrms.leave.api.DefineLeaveTypeCommand;
import com.company.hrms.leave.api.InitializeLeaveBalanceCommand;
import com.company.hrms.leave.api.LeaveBalanceView;
import com.company.hrms.leave.api.LeaveModuleApi;
import com.company.hrms.leave.api.LeaveRequestView;
import com.company.hrms.leave.api.LeaveTypeView;
import com.company.hrms.leave.api.ReviewLeaveCommand;
import com.company.hrms.leave.domain.LeaveStatus;
import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LeaveControllerTest {

    private final LeaveController leaveController = new LeaveController(new StubLeaveModuleApi());

    private final WebTestClient webTestClient = WebTestClient.bindToController(leaveController)
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void applyLeaveEndpointWorks() {
        webTestClient.post()
                .uri("/api/v1/leaves/requests")
                .header("X-Tenant-Id", "default")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "employeeId": "21111111-1111-1111-1111-111111111111",
                          "leaveTypeId": "81000000-0000-0000-0000-000000000001",
                          "fromDate": "2026-03-10",
                          "toDate": "2026-03-11",
                          "reason": "Vacation",
                          "requestedBy": "emp-1"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("SUBMITTED");
    }

    static class StubLeaveModuleApi implements LeaveModuleApi {
        @Override
        public Mono<LeaveTypeView> defineLeaveType(DefineLeaveTypeCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveBalanceView> initializeLeaveBalance(InitializeLeaveBalanceCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveRequestView> applyLeave(ApplyLeaveCommand command) {
            return Mono.just(new LeaveRequestView(
                    UUID.randomUUID(),
                    "default",
                    command.employeeId(),
                    command.leaveTypeId(),
                    command.fromDate(),
                    command.toDate(),
                    2,
                    command.reason(),
                    LeaveStatus.SUBMITTED,
                    UUID.randomUUID(),
                    command.requestedBy(),
                    null,
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Mono<LeaveRequestView> reviewLeave(ReviewLeaveCommand command) {
            return Mono.empty();
        }

        @Override
        public Flux<LeaveBalanceView> balances(UUID employeeId, int leaveYear) {
            return Flux.empty();
        }

        @Override
        public Flux<LeaveRequestView> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.empty();
        }
    }
}
