package com.company.hrms.attendance.controller;

import com.company.hrms.attendance.model.*;
import com.company.hrms.attendance.service.*;

import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.service.AttendanceModuleApi;
import com.company.hrms.attendance.model.AttendanceQueryDto;
import com.company.hrms.attendance.model.AttendanceRecordViewDto;
import com.company.hrms.attendance.model.CreateShiftCommandDto;
import com.company.hrms.attendance.model.PunchEventViewDto;
import com.company.hrms.attendance.model.RecordPunchCommandDto;
import com.company.hrms.attendance.model.ShiftAssignmentViewDto;
import com.company.hrms.attendance.model.ShiftViewDto;
import com.company.hrms.attendance.model.PunchType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance", description = "ShiftDto setup, punch capture, and attendance reporting APIs")
public class AttendanceController {

    private final AttendanceModuleApi attendanceModuleApi;

    public AttendanceController(AttendanceModuleApi attendanceModuleApi) {
        this.attendanceModuleApi = attendanceModuleApi;
    }

    @PostMapping("/shifts")
    @Operation(summary = "Create shift", description = "Creates a shift definition with start and end times.")
    public Mono<ShiftViewDto> createShift(@Valid @RequestBody CreateShiftRequest request) {
        return attendanceModuleApi.createShift(new CreateShiftCommandDto(
                request.shiftCode(),
                request.name(),
                request.startTime(),
                request.endTime()));
    }

    @PostMapping("/shift-assignments")
    @Operation(summary = "Assign shift", description = "Assigns an existing shift to an employee for a date range.")
    public Mono<ShiftAssignmentViewDto> assignShift(@Valid @RequestBody AssignShiftRequest request) {
        return attendanceModuleApi.assignShift(new AssignShiftCommandDto(
                request.employeeId(),
                request.shiftId(),
                request.effectiveFrom(),
                request.effectiveTo()));
    }

    @PostMapping("/punch-events")
    @Operation(summary = "Record punch event", description = "Captures employee punch in/out events from biometric or manual sources.")
    public Mono<PunchEventViewDto> recordPunch(@Valid @RequestBody RecordPunchRequest request) {
        return attendanceModuleApi.recordPunch(new RecordPunchCommandDto(
                request.employeeId(),
                request.punchType(),
                request.eventTime(),
                request.source()));
    }

    @GetMapping("/records")
    @Operation(summary = "EmployeeDto attendance records", description = "Returns computed attendance records for one employee within a date range.")
    public Flux<AttendanceRecordViewDto> attendanceByEmployee(
            @Parameter(description = "EmployeeDto identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @RequestParam("employeeId") UUID employeeId,
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return attendanceModuleApi.attendanceByEmployee(new AttendanceQueryDto(employeeId, fromDate, toDate));
    }

    public record CreateShiftRequest(
            @Schema(description = "Business shift code", example = "SHIFT-GEN")
            @NotBlank String shiftCode,
            @Schema(description = "ShiftDto display name", example = "General ShiftDto")
            @NotBlank String name,
            @Schema(description = "ShiftDto start time in HH:mm:ss", example = "09:00:00")
            @NotNull LocalTime startTime,
            @Schema(description = "ShiftDto end time in HH:mm:ss", example = "18:00:00")
            @NotNull LocalTime endTime
    ) {
    }

    public record AssignShiftRequest(
            @Schema(description = "EmployeeDto identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @NotNull UUID employeeId,
            @Schema(description = "ShiftDto identifier", example = "911f4648-72a6-4f05-840e-7af62097204d")
            @NotNull UUID shiftId,
            @Schema(description = "Effective start date", example = "2026-01-01")
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @Schema(description = "Effective end date, null means open-ended", example = "2026-12-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo
    ) {
    }

    public record RecordPunchRequest(
            @Schema(description = "EmployeeDto identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @NotNull UUID employeeId,
            @Schema(description = "Punch direction", example = "IN")
            @NotNull PunchType punchType,
            @Schema(description = "Event timestamp in UTC ISO-8601 format", example = "2026-01-12T05:30:00Z")
            Instant eventTime,
            @Schema(description = "Punch source name", example = "biometric-device-01")
            String source
    ) {
    }
}
