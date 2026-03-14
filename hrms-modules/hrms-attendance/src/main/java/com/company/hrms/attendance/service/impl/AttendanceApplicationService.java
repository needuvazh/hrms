package com.company.hrms.attendance.service.impl;

import com.company.hrms.attendance.model.*;
import com.company.hrms.attendance.repository.*;
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
import com.company.hrms.attendance.model.AttendanceRecordDto;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.attendance.model.AttendanceStatus;
import com.company.hrms.attendance.model.PunchEventDto;
import com.company.hrms.attendance.model.PunchType;
import com.company.hrms.attendance.model.ShiftDto;
import com.company.hrms.attendance.model.ShiftAssignmentDto;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Primary
public class AttendanceApplicationService implements AttendanceModuleApi {

    private final AttendanceRepository attendanceRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;

    public AttendanceApplicationService(
            AttendanceRepository attendanceRepository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard
    ) {
        this.attendanceRepository = attendanceRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
    }

    @Override
    public Mono<ShiftViewDto> createShift(CreateShiftCommandDto command) {
        validateShift(command);

        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    ShiftDto shift = new ShiftDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.shiftCode().trim().toUpperCase(),
                            command.name().trim(),
                            command.startTime(),
                            command.endTime(),
                            true,
                            now,
                            now);
                    return attendanceRepository.saveShift(shift).map(this::toShiftView);
                });
    }

    @Override
    public Mono<ShiftAssignmentViewDto> assignShift(AssignShiftCommandDto command) {
        validateShiftAssignment(command);

        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMap(tenantId -> attendanceRepository.findShiftById(tenantId, command.shiftId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "SHIFT_NOT_FOUND", "ShiftDto not found")))
                        .flatMap(shift -> {
                            Instant now = Instant.now();
                            ShiftAssignmentDto assignment = new ShiftAssignmentDto(
                                    UUID.randomUUID(),
                                    tenantId,
                                    command.employeeId(),
                                    command.shiftId(),
                                    command.effectiveFrom(),
                                    command.effectiveTo(),
                                    true,
                                    now,
                                    now);
                            return attendanceRepository.saveShiftAssignment(assignment).map(this::toShiftAssignmentView);
                        }));
    }

    @Override
    public Mono<PunchEventViewDto> recordPunch(RecordPunchCommandDto command) {
        validatePunch(command);

        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant eventTime = command.eventTime() == null ? Instant.now() : command.eventTime();
                    LocalDate attendanceDate = eventTime.atZone(ZoneOffset.UTC).toLocalDate();

                    return attendanceRepository.findActiveShiftAssignment(tenantId, command.employeeId(), attendanceDate)
                            .switchIfEmpty(Mono.error(new HrmsException(
                                    HttpStatus.BAD_REQUEST,
                                    "SHIFT_ASSIGNMENT_REQUIRED",
                                    "No active shift assignment found for employee")))
                            .flatMap(assignment -> attendanceRepository
                                    .findAttendanceRecordByEmployeeAndDate(tenantId, command.employeeId(), attendanceDate)
                                    .flatMap(record -> processExistingRecord(command, assignment, record, eventTime))
                                    .switchIfEmpty(processNewRecord(command, assignment, attendanceDate, eventTime, tenantId))
                                    .flatMap(updatedRecord -> createPunchEvent(command, tenantId, assignment.shiftId(), updatedRecord.id(), eventTime)
                                            .flatMap(attendanceRepository::savePunchEvent)
                                            .map(this::toPunchEventView)));
                });
    }

    @Override
    public Flux<AttendanceRecordViewDto> attendanceByEmployee(AttendanceQueryDto query) {
        validateAttendanceQuery(query);
        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMapMany(tenantId -> attendanceRepository.findAttendanceByEmployeeAndDateRange(
                                tenantId,
                                query.employeeId(),
                                query.fromDate(),
                                query.toDate())
                        .map(this::toAttendanceRecordView));
    }

    private Mono<AttendanceRecordDto> processExistingRecord(
            RecordPunchCommandDto command,
            ShiftAssignmentDto assignment,
            AttendanceRecordDto existing,
            Instant eventTime
    ) {
        if (command.punchType() == PunchType.IN) {
            return attendanceRepository.updateAttendanceRecord(existing.markPunchIn(eventTime, Instant.now()));
        }

        if (existing.firstPunchIn() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PUNCH_IN_REQUIRED", "Punch in is required before punch out"));
        }

        return attendanceRepository.updateAttendanceRecord(existing.markPunchOut(eventTime, Instant.now()));
    }

    private Mono<AttendanceRecordDto> processNewRecord(
            RecordPunchCommandDto command,
            ShiftAssignmentDto assignment,
            LocalDate attendanceDate,
            Instant eventTime,
            String tenantId
    ) {
        if (command.punchType() == PunchType.OUT) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PUNCH_IN_REQUIRED", "Punch in is required before punch out"));
        }

        Instant now = Instant.now();
        AttendanceRecordDto attendanceRecord = new AttendanceRecordDto(
                UUID.randomUUID(),
                tenantId,
                command.employeeId(),
                attendanceDate,
                assignment.shiftId(),
                AttendanceStatus.IN_PROGRESS,
                eventTime,
                null,
                now,
                now);

        return attendanceRepository.saveAttendanceRecord(attendanceRecord);
    }

    private Mono<PunchEventDto> createPunchEvent(
            RecordPunchCommandDto command,
            String tenantId,
            UUID shiftId,
            UUID attendanceRecordId,
            Instant eventTime
    ) {
        return Mono.just(new PunchEventDto(
                UUID.randomUUID(),
                tenantId,
                command.employeeId(),
                shiftId,
                attendanceRecordId,
                command.punchType(),
                eventTime,
                StringUtils.hasText(command.source()) ? command.source() : "MANUAL",
                Instant.now()));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateShift(CreateShiftCommandDto command) {
        if (!StringUtils.hasText(command.shiftCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_CODE_REQUIRED", "ShiftDto code is required");
        }
        if (!StringUtils.hasText(command.name())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_NAME_REQUIRED", "ShiftDto name is required");
        }
        if (command.startTime() == null || command.endTime() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_TIME_REQUIRED", "ShiftDto start and end time are required");
        }
        if (command.startTime().equals(command.endTime())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SHIFT_TIME", "ShiftDto start and end time must be different");
        }
    }

    private void validateShiftAssignment(AssignShiftCommandDto command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }
        if (command.shiftId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_REQUIRED", "ShiftDto id is required");
        }
        if (command.effectiveFrom() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EFFECTIVE_FROM_REQUIRED", "Effective from date is required");
        }
        if (command.effectiveTo() != null && command.effectiveTo().isBefore(command.effectiveFrom())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_EFFECTIVE_RANGE", "Effective to cannot be before effective from");
        }
    }

    private void validatePunch(RecordPunchCommandDto command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }
        if (command.punchType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PUNCH_TYPE_REQUIRED", "Punch type is required");
        }
    }

    private void validateAttendanceQuery(AttendanceQueryDto query) {
        if (query.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }
        if (query.fromDate() == null || query.toDate() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_REQUIRED", "From date and to date are required");
        }
        if (query.toDate().isBefore(query.fromDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "toDate must be after or equal to fromDate");
        }
    }

    private ShiftViewDto toShiftView(ShiftDto shift) {
        return new ShiftViewDto(
                shift.id(),
                shift.tenantId(),
                shift.shiftCode(),
                shift.name(),
                shift.startTime(),
                shift.endTime(),
                shift.active(),
                shift.createdAt(),
                shift.updatedAt());
    }

    private ShiftAssignmentViewDto toShiftAssignmentView(ShiftAssignmentDto assignment) {
        return new ShiftAssignmentViewDto(
                assignment.id(),
                assignment.tenantId(),
                assignment.employeeId(),
                assignment.shiftId(),
                assignment.effectiveFrom(),
                assignment.effectiveTo(),
                assignment.active(),
                assignment.createdAt(),
                assignment.updatedAt());
    }

    private PunchEventViewDto toPunchEventView(PunchEventDto event) {
        return new PunchEventViewDto(
                event.id(),
                event.tenantId(),
                event.employeeId(),
                event.shiftId(),
                event.attendanceRecordId(),
                event.punchType(),
                event.eventTime(),
                event.source(),
                event.createdAt());
    }

    private AttendanceRecordViewDto toAttendanceRecordView(AttendanceRecordDto attendanceRecord) {
        return new AttendanceRecordViewDto(
                attendanceRecord.id(),
                attendanceRecord.tenantId(),
                attendanceRecord.employeeId(),
                attendanceRecord.attendanceDate(),
                attendanceRecord.shiftId(),
                attendanceRecord.status(),
                attendanceRecord.firstPunchIn(),
                attendanceRecord.lastPunchOut(),
                attendanceRecord.createdAt(),
                attendanceRecord.updatedAt());
    }
}
