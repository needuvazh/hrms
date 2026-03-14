package com.company.hrms.attendance.controller;

import com.company.hrms.attendance.model.*;
import com.company.hrms.attendance.service.*;

import com.company.hrms.attendance.service.AttendanceModuleApi;
import com.company.hrms.attendance.model.AttendanceQueryDto;
import com.company.hrms.attendance.model.AttendanceRecordViewDto;
import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.model.CreateShiftCommandDto;
import com.company.hrms.attendance.model.PunchEventViewDto;
import com.company.hrms.attendance.model.RecordPunchCommandDto;
import com.company.hrms.attendance.model.ShiftAssignmentViewDto;
import com.company.hrms.attendance.model.ShiftViewDto;
import com.company.hrms.attendance.model.AttendanceStatus;
import com.company.hrms.attendance.model.PunchType;
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
                          "name": "Night ShiftDto",
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
        public Mono<ShiftViewDto> createShift(CreateShiftCommandDto command) {
            return Mono.just(new ShiftViewDto(
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
        public Mono<ShiftAssignmentViewDto> assignShift(AssignShiftCommandDto command) {
            return Mono.just(new ShiftAssignmentViewDto(
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
        public Mono<PunchEventViewDto> recordPunch(RecordPunchCommandDto command) {
            return Mono.just(new PunchEventViewDto(
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
        public Flux<AttendanceRecordViewDto> attendanceByEmployee(AttendanceQueryDto query) {
            return Flux.just(new AttendanceRecordViewDto(
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
