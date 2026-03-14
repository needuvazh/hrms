package com.company.hrms.attendance.infrastructure.web;

import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.AttendanceRecordView;
import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.PunchEventView;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.api.ShiftAssignmentView;
import com.company.hrms.attendance.api.ShiftView;
import com.company.hrms.attendance.domain.AttendanceStatus;
import com.company.hrms.attendance.domain.PunchType;
import com.company.hrms.platform.starter.error.web.GlobalExceptionHandler;
import com.company.hrms.platform.starter.tenancy.web.TenantContextWebFilter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AttendanceTenantRequestHandlingTest {

    private final AttendanceController attendanceController = new AttendanceController(new StubAttendanceModuleApi());

    private final WebTestClient webTestClient = WebTestClient.bindToController(attendanceController)
            .controllerAdvice(new GlobalExceptionHandler())
            .webFilter(new TenantContextWebFilter())
            .build();

    @Test
    void createShiftWorksWithTenantHeader() {
        webTestClient.post()
                .uri("/api/v1/attendance/shifts")
                .header("X-Tenant-Id", "default")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "shiftCode": "NIGHT",
                          "name": "Night Shift",
                          "startTime": "21:00:00",
                          "endTime": "06:00:00"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.shiftCode").isEqualTo("NIGHT");
    }

    static class StubAttendanceModuleApi implements AttendanceModuleApi {

        @Override
        public Mono<ShiftView> createShift(CreateShiftCommand command) {
            return Mono.just(new ShiftView(
                    UUID.randomUUID(),
                    "default",
                    command.shiftCode(),
                    command.name(),
                    command.startTime(),
                    command.endTime(),
                    true,
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Mono<ShiftAssignmentView> assignShift(AssignShiftCommand command) {
            return Mono.just(new ShiftAssignmentView(
                    UUID.randomUUID(),
                    "default",
                    command.employeeId(),
                    command.shiftId(),
                    command.effectiveFrom(),
                    command.effectiveTo(),
                    true,
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Mono<PunchEventView> recordPunch(RecordPunchCommand command) {
            return Mono.just(new PunchEventView(
                    UUID.randomUUID(),
                    "default",
                    command.employeeId(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    PunchType.IN,
                    Instant.now(),
                    "MANUAL",
                    Instant.now()));
        }

        @Override
        public Flux<AttendanceRecordView> attendanceByEmployee(AttendanceQuery query) {
            return Flux.just(new AttendanceRecordView(
                    UUID.randomUUID(),
                    "default",
                    query.employeeId(),
                    LocalDate.now(),
                    UUID.randomUUID(),
                    AttendanceStatus.PRESENT,
                    Instant.now().minusSeconds(3600),
                    Instant.now(),
                    Instant.now().minusSeconds(3600),
                    Instant.now()));
        }
    }
}
