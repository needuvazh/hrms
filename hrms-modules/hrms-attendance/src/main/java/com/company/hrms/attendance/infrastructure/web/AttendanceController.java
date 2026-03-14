package com.company.hrms.attendance.infrastructure.web;

import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.AttendanceRecordView;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.PunchEventView;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.api.ShiftAssignmentView;
import com.company.hrms.attendance.api.ShiftView;
import com.company.hrms.attendance.domain.PunchType;
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
@Tag(name = "Attendance", description = "Shift setup, punch capture, and attendance reporting APIs")
public class AttendanceController {

    private final AttendanceModuleApi attendanceModuleApi;

    public AttendanceController(AttendanceModuleApi attendanceModuleApi) {
        this.attendanceModuleApi = attendanceModuleApi;
    }

    @PostMapping("/shifts")
    @Operation(summary = "Create shift", description = "Creates a shift definition with start and end times.")
    public Mono<ShiftView> createShift(@Valid @RequestBody CreateShiftRequest request) {
        return attendanceModuleApi.createShift(new CreateShiftCommand(
                request.shiftCode(),
                request.name(),
                request.startTime(),
                request.endTime()));
    }

    @PostMapping("/shift-assignments")
    @Operation(summary = "Assign shift", description = "Assigns an existing shift to an employee for a date range.")
    public Mono<ShiftAssignmentView> assignShift(@Valid @RequestBody AssignShiftRequest request) {
        return attendanceModuleApi.assignShift(new AssignShiftCommand(
                request.employeeId(),
                request.shiftId(),
                request.effectiveFrom(),
                request.effectiveTo()));
    }

    @PostMapping("/punch-events")
    @Operation(summary = "Record punch event", description = "Captures employee punch in/out events from biometric or manual sources.")
    public Mono<PunchEventView> recordPunch(@Valid @RequestBody RecordPunchRequest request) {
        return attendanceModuleApi.recordPunch(new RecordPunchCommand(
                request.employeeId(),
                request.punchType(),
                request.eventTime(),
                request.source()));
    }

    @GetMapping("/records")
    @Operation(summary = "Employee attendance records", description = "Returns computed attendance records for one employee within a date range.")
    public Flux<AttendanceRecordView> attendanceByEmployee(
            @Parameter(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @RequestParam("employeeId") UUID employeeId,
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-01-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return attendanceModuleApi.attendanceByEmployee(new AttendanceQuery(employeeId, fromDate, toDate));
    }

    public record CreateShiftRequest(
            @Schema(description = "Business shift code", example = "SHIFT-GEN")
            @NotBlank String shiftCode,
            @Schema(description = "Shift display name", example = "General Shift")
            @NotBlank String name,
            @Schema(description = "Shift start time in HH:mm:ss", example = "09:00:00")
            @NotNull LocalTime startTime,
            @Schema(description = "Shift end time in HH:mm:ss", example = "18:00:00")
            @NotNull LocalTime endTime
    ) {
    }

    public record AssignShiftRequest(
            @Schema(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @NotNull UUID employeeId,
            @Schema(description = "Shift identifier", example = "911f4648-72a6-4f05-840e-7af62097204d")
            @NotNull UUID shiftId,
            @Schema(description = "Effective start date", example = "2026-01-01")
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @Schema(description = "Effective end date, null means open-ended", example = "2026-12-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo
    ) {
    }

    public record RecordPunchRequest(
            @Schema(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
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
