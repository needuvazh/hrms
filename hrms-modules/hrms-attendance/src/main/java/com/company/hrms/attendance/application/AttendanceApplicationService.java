package com.company.hrms.attendance.application;

import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.AttendanceRecordView;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.PunchEventView;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.api.ShiftAssignmentView;
import com.company.hrms.attendance.api.ShiftView;
import com.company.hrms.attendance.domain.AttendanceRecord;
import com.company.hrms.attendance.domain.AttendanceRepository;
import com.company.hrms.attendance.domain.AttendanceStatus;
import com.company.hrms.attendance.domain.PunchEvent;
import com.company.hrms.attendance.domain.PunchType;
import com.company.hrms.attendance.domain.Shift;
import com.company.hrms.attendance.domain.ShiftAssignment;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
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
    public Mono<ShiftView> createShift(CreateShiftCommand command) {
        validateShift(command);

        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    Shift shift = new Shift(
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
    public Mono<ShiftAssignmentView> assignShift(AssignShiftCommand command) {
        validateShiftAssignment(command);

        return enablementGuard.requireModuleEnabled("attendance")
                .then(requireTenant())
                .flatMap(tenantId -> attendanceRepository.findShiftById(tenantId, command.shiftId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "SHIFT_NOT_FOUND", "Shift not found")))
                        .flatMap(shift -> {
                            Instant now = Instant.now();
                            ShiftAssignment assignment = new ShiftAssignment(
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
    public Mono<PunchEventView> recordPunch(RecordPunchCommand command) {
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
    public Flux<AttendanceRecordView> attendanceByEmployee(AttendanceQuery query) {
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

    private Mono<AttendanceRecord> processExistingRecord(
            RecordPunchCommand command,
            ShiftAssignment assignment,
            AttendanceRecord existing,
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

    private Mono<AttendanceRecord> processNewRecord(
            RecordPunchCommand command,
            ShiftAssignment assignment,
            LocalDate attendanceDate,
            Instant eventTime,
            String tenantId
    ) {
        if (command.punchType() == PunchType.OUT) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "PUNCH_IN_REQUIRED", "Punch in is required before punch out"));
        }

        Instant now = Instant.now();
        AttendanceRecord attendanceRecord = new AttendanceRecord(
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

    private Mono<PunchEvent> createPunchEvent(
            RecordPunchCommand command,
            String tenantId,
            UUID shiftId,
            UUID attendanceRecordId,
            Instant eventTime
    ) {
        return Mono.just(new PunchEvent(
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

    private void validateShift(CreateShiftCommand command) {
        if (!StringUtils.hasText(command.shiftCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_CODE_REQUIRED", "Shift code is required");
        }
        if (!StringUtils.hasText(command.name())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_NAME_REQUIRED", "Shift name is required");
        }
        if (command.startTime() == null || command.endTime() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_TIME_REQUIRED", "Shift start and end time are required");
        }
        if (command.startTime().equals(command.endTime())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SHIFT_TIME", "Shift start and end time must be different");
        }
    }

    private void validateShiftAssignment(AssignShiftCommand command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "Employee id is required");
        }
        if (command.shiftId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "SHIFT_REQUIRED", "Shift id is required");
        }
        if (command.effectiveFrom() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EFFECTIVE_FROM_REQUIRED", "Effective from date is required");
        }
        if (command.effectiveTo() != null && command.effectiveTo().isBefore(command.effectiveFrom())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_EFFECTIVE_RANGE", "Effective to cannot be before effective from");
        }
    }

    private void validatePunch(RecordPunchCommand command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "Employee id is required");
        }
        if (command.punchType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PUNCH_TYPE_REQUIRED", "Punch type is required");
        }
    }

    private void validateAttendanceQuery(AttendanceQuery query) {
        if (query.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "Employee id is required");
        }
        if (query.fromDate() == null || query.toDate() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DATE_RANGE_REQUIRED", "From date and to date are required");
        }
        if (query.toDate().isBefore(query.fromDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "toDate must be after or equal to fromDate");
        }
    }

    private ShiftView toShiftView(Shift shift) {
        return new ShiftView(
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

    private ShiftAssignmentView toShiftAssignmentView(ShiftAssignment assignment) {
        return new ShiftAssignmentView(
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

    private PunchEventView toPunchEventView(PunchEvent event) {
        return new PunchEventView(
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

    private AttendanceRecordView toAttendanceRecordView(AttendanceRecord attendanceRecord) {
        return new AttendanceRecordView(
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
