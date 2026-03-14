package com.company.hrms.attendance.domain;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AttendanceRepository {

    Mono<Shift> saveShift(Shift shift);

    Mono<Shift> findShiftById(String tenantId, UUID shiftId);

    Mono<ShiftAssignment> saveShiftAssignment(ShiftAssignment assignment);

    Mono<ShiftAssignment> findActiveShiftAssignment(String tenantId, UUID employeeId, LocalDate attendanceDate);

    Mono<AttendanceRecord> saveAttendanceRecord(AttendanceRecord attendanceRecord);

    Mono<AttendanceRecord> updateAttendanceRecord(AttendanceRecord attendanceRecord);

    Mono<AttendanceRecord> findAttendanceRecordByEmployeeAndDate(String tenantId, UUID employeeId, LocalDate attendanceDate);

    Mono<PunchEvent> savePunchEvent(PunchEvent punchEvent);

    Flux<AttendanceRecord> findAttendanceByEmployeeAndDateRange(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
