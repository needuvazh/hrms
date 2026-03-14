package com.company.hrms.attendance.repository;

import com.company.hrms.attendance.model.*;

import com.company.hrms.attendance.model.AttendanceRecordDto;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.attendance.model.AttendanceStatus;
import com.company.hrms.attendance.model.PunchEventDto;
import com.company.hrms.attendance.model.PunchType;
import com.company.hrms.attendance.model.ShiftDto;
import com.company.hrms.attendance.model.ShiftAssignmentDto;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class AttendanceR2dbcRepository implements AttendanceRepository {

    private final DatabaseClient databaseClient;

    public AttendanceR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<ShiftDto> saveShift(ShiftDto shift) {
        return databaseClient.sql("""
                        INSERT INTO attendance.shifts(
                            id, tenant_id, shift_code, shift_name, start_time, end_time, is_active, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :shiftCode, :shiftName, :startTime, :endTime, :isActive, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, shift_code, shift_name, start_time, end_time, is_active, created_at, updated_at
                        """)
                .bind("id", shift.id())
                .bind("tenantId", shift.tenantId())
                .bind("shiftCode", shift.shiftCode())
                .bind("shiftName", shift.name())
                .bind("startTime", shift.startTime())
                .bind("endTime", shift.endTime())
                .bind("isActive", shift.active())
                .bind("createdAt", shift.createdAt())
                .bind("updatedAt", shift.updatedAt())
                .map((row, metadata) -> mapShift(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("shift_code", String.class),
                        row.get("shift_name", String.class),
                        row.get("start_time", LocalTime.class),
                        row.get("end_time", LocalTime.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<ShiftDto> findShiftById(String tenantId, UUID shiftId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, shift_code, shift_name, start_time, end_time, is_active, created_at, updated_at
                        FROM attendance.shifts
                        WHERE tenant_id = :tenantId
                          AND id = :id
                          AND is_active = true
                        """)
                .bind("tenantId", tenantId)
                .bind("id", shiftId)
                .map((row, metadata) -> mapShift(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("shift_code", String.class),
                        row.get("shift_name", String.class),
                        row.get("start_time", LocalTime.class),
                        row.get("end_time", LocalTime.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<ShiftAssignmentDto> saveShiftAssignment(ShiftAssignmentDto assignment) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO attendance.shift_assignments(
                            id, tenant_id, employee_id, shift_id, effective_from, effective_to, is_active, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :employeeId, :shiftId, :effectiveFrom, :effectiveTo, :isActive, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, employee_id, shift_id, effective_from, effective_to, is_active, created_at, updated_at
                        """)
                .bind("id", assignment.id())
                .bind("tenantId", assignment.tenantId())
                .bind("employeeId", assignment.employeeId())
                .bind("shiftId", assignment.shiftId())
                .bind("effectiveFrom", assignment.effectiveFrom())
                .bind("isActive", assignment.active())
                .bind("createdAt", assignment.createdAt())
                .bind("updatedAt", assignment.updatedAt());

        spec = bindNullable(spec, "effectiveTo", assignment.effectiveTo(), LocalDate.class);

        return spec
                .map((row, metadata) -> mapShiftAssignment(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("shift_id", UUID.class),
                        row.get("effective_from", LocalDate.class),
                        row.get("effective_to", LocalDate.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<ShiftAssignmentDto> findActiveShiftAssignment(String tenantId, UUID employeeId, LocalDate attendanceDate) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, shift_id, effective_from, effective_to, is_active, created_at, updated_at
                        FROM attendance.shift_assignments
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND is_active = true
                          AND effective_from <= :attendanceDate
                          AND (effective_to IS NULL OR effective_to >= :attendanceDate)
                        ORDER BY effective_from DESC
                        LIMIT 1
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("attendanceDate", attendanceDate)
                .map((row, metadata) -> mapShiftAssignment(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("shift_id", UUID.class),
                        row.get("effective_from", LocalDate.class),
                        row.get("effective_to", LocalDate.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<AttendanceRecordDto> saveAttendanceRecord(AttendanceRecordDto attendanceRecord) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO attendance.attendance_records(
                            id, tenant_id, employee_id, attendance_date, shift_id, attendance_status,
                            first_punch_in, last_punch_out, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :employeeId, :attendanceDate, :shiftId, :attendanceStatus,
                            :firstPunchIn, :lastPunchOut, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, employee_id, attendance_date, shift_id, attendance_status,
                                  first_punch_in, last_punch_out, created_at, updated_at
                        """)
                .bind("id", attendanceRecord.id())
                .bind("tenantId", attendanceRecord.tenantId())
                .bind("employeeId", attendanceRecord.employeeId())
                .bind("attendanceDate", attendanceRecord.attendanceDate())
                .bind("shiftId", attendanceRecord.shiftId())
                .bind("attendanceStatus", attendanceRecord.status().name())
                .bind("createdAt", attendanceRecord.createdAt())
                .bind("updatedAt", attendanceRecord.updatedAt());

        spec = bindNullable(spec, "firstPunchIn", attendanceRecord.firstPunchIn(), Instant.class);
        spec = bindNullable(spec, "lastPunchOut", attendanceRecord.lastPunchOut(), Instant.class);

        return spec
                .map((row, metadata) -> mapAttendanceRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("attendance_date", LocalDate.class),
                        row.get("shift_id", UUID.class),
                        row.get("attendance_status", String.class),
                        row.get("first_punch_in", Instant.class),
                        row.get("last_punch_out", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<AttendanceRecordDto> updateAttendanceRecord(AttendanceRecordDto attendanceRecord) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE attendance.attendance_records
                        SET attendance_status = :attendanceStatus,
                            first_punch_in = :firstPunchIn,
                            last_punch_out = :lastPunchOut,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, employee_id, attendance_date, shift_id, attendance_status,
                                  first_punch_in, last_punch_out, created_at, updated_at
                        """)
                .bind("id", attendanceRecord.id())
                .bind("tenantId", attendanceRecord.tenantId())
                .bind("attendanceStatus", attendanceRecord.status().name())
                .bind("updatedAt", attendanceRecord.updatedAt());

        spec = bindNullable(spec, "firstPunchIn", attendanceRecord.firstPunchIn(), Instant.class);
        spec = bindNullable(spec, "lastPunchOut", attendanceRecord.lastPunchOut(), Instant.class);

        return spec
                .map((row, metadata) -> mapAttendanceRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("attendance_date", LocalDate.class),
                        row.get("shift_id", UUID.class),
                        row.get("attendance_status", String.class),
                        row.get("first_punch_in", Instant.class),
                        row.get("last_punch_out", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<AttendanceRecordDto> findAttendanceRecordByEmployeeAndDate(String tenantId, UUID employeeId, LocalDate attendanceDate) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, attendance_date, shift_id, attendance_status,
                               first_punch_in, last_punch_out, created_at, updated_at
                        FROM attendance.attendance_records
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND attendance_date = :attendanceDate
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("attendanceDate", attendanceDate)
                .map((row, metadata) -> mapAttendanceRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("attendance_date", LocalDate.class),
                        row.get("shift_id", UUID.class),
                        row.get("attendance_status", String.class),
                        row.get("first_punch_in", Instant.class),
                        row.get("last_punch_out", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<PunchEventDto> savePunchEvent(PunchEventDto punchEvent) {
        return databaseClient.sql("""
                        INSERT INTO attendance.punch_events(
                            id, tenant_id, employee_id, shift_id, attendance_record_id, punch_type, event_time, source, created_at
                        ) VALUES (
                            :id, :tenantId, :employeeId, :shiftId, :attendanceRecordId, :punchType, :eventTime, :source, :createdAt
                        )
                        RETURNING id, tenant_id, employee_id, shift_id, attendance_record_id, punch_type, event_time, source, created_at
                        """)
                .bind("id", punchEvent.id())
                .bind("tenantId", punchEvent.tenantId())
                .bind("employeeId", punchEvent.employeeId())
                .bind("shiftId", punchEvent.shiftId())
                .bind("attendanceRecordId", punchEvent.attendanceRecordId())
                .bind("punchType", punchEvent.punchType().name())
                .bind("eventTime", punchEvent.eventTime())
                .bind("source", punchEvent.source())
                .bind("createdAt", punchEvent.createdAt())
                .map((row, metadata) -> mapPunchEvent(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("shift_id", UUID.class),
                        row.get("attendance_record_id", UUID.class),
                        row.get("punch_type", String.class),
                        row.get("event_time", Instant.class),
                        row.get("source", String.class),
                        row.get("created_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<AttendanceRecordDto> findAttendanceByEmployeeAndDateRange(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, attendance_date, shift_id, attendance_status,
                               first_punch_in, last_punch_out, created_at, updated_at
                        FROM attendance.attendance_records
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND attendance_date BETWEEN :fromDate AND :toDate
                        ORDER BY attendance_date ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> mapAttendanceRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("attendance_date", LocalDate.class),
                        row.get("shift_id", UUID.class),
                        row.get("attendance_status", String.class),
                        row.get("first_punch_in", Instant.class),
                        row.get("last_punch_out", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private ShiftDto mapShift(
            UUID id,
            String tenantId,
            String shiftCode,
            String shiftName,
            LocalTime startTime,
            LocalTime endTime,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new ShiftDto(id, tenantId, shiftCode, shiftName, startTime, endTime, Boolean.TRUE.equals(active), createdAt, updatedAt);
    }

    private ShiftAssignmentDto mapShiftAssignment(
            UUID id,
            String tenantId,
            UUID employeeId,
            UUID shiftId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new ShiftAssignmentDto(id, tenantId, employeeId, shiftId, effectiveFrom, effectiveTo, Boolean.TRUE.equals(active), createdAt, updatedAt);
    }

    private AttendanceRecordDto mapAttendanceRecord(
            UUID id,
            String tenantId,
            UUID employeeId,
            LocalDate attendanceDate,
            UUID shiftId,
            String attendanceStatus,
            Instant firstPunchIn,
            Instant lastPunchOut,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new AttendanceRecordDto(
                id,
                tenantId,
                employeeId,
                attendanceDate,
                shiftId,
                AttendanceStatus.valueOf(attendanceStatus),
                firstPunchIn,
                lastPunchOut,
                createdAt,
                updatedAt);
    }

    private PunchEventDto mapPunchEvent(
            UUID id,
            String tenantId,
            UUID employeeId,
            UUID shiftId,
            UUID attendanceRecordId,
            String punchType,
            Instant eventTime,
            String source,
            Instant createdAt
    ) {
        return new PunchEventDto(id, tenantId, employeeId, shiftId, attendanceRecordId, PunchType.valueOf(punchType), eventTime, source, createdAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
