package com.company.hrms.leave.controller;

import com.company.hrms.leave.model.*;
import com.company.hrms.leave.service.*;

import com.company.hrms.leave.model.ApplyLeaveCommandDto;
import com.company.hrms.leave.model.DefineLeaveTypeCommandDto;
import com.company.hrms.leave.model.InitializeLeaveBalanceCommandDto;
import com.company.hrms.leave.model.LeaveBalanceViewDto;
import com.company.hrms.leave.service.LeaveModuleApi;
import com.company.hrms.leave.model.LeaveRequestViewDto;
import com.company.hrms.leave.model.LeaveTypeViewDto;
import com.company.hrms.leave.model.ReviewLeaveCommandDto;
import com.company.hrms.leave.model.LeaveStatus;
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
        public Mono<LeaveTypeViewDto> defineLeaveType(DefineLeaveTypeCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveBalanceViewDto> initializeLeaveBalance(InitializeLeaveBalanceCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveRequestViewDto> applyLeave(ApplyLeaveCommandDto command) {
            return Mono.just(new LeaveRequestViewDto(
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
        public Mono<LeaveRequestViewDto> reviewLeave(ReviewLeaveCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Flux<LeaveBalanceViewDto> balances(UUID employeeId, int leaveYear) {
            return Flux.empty();
        }

        @Override
        public Flux<LeaveRequestViewDto> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.empty();
        }
    }
}
