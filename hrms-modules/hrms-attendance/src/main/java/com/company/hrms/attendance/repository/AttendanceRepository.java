package com.company.hrms.attendance.repository;

import com.company.hrms.attendance.model.*;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AttendanceRepository {

    Mono<ShiftDto> saveShift(ShiftDto shift);

    Mono<ShiftDto> findShiftById(String tenantId, UUID shiftId);

    Mono<ShiftAssignmentDto> saveShiftAssignment(ShiftAssignmentDto assignment);

    Mono<ShiftAssignmentDto> findActiveShiftAssignment(String tenantId, UUID employeeId, LocalDate attendanceDate);

    Mono<AttendanceRecordDto> saveAttendanceRecord(AttendanceRecordDto attendanceRecord);

    Mono<AttendanceRecordDto> updateAttendanceRecord(AttendanceRecordDto attendanceRecord);

    Mono<AttendanceRecordDto> findAttendanceRecordByEmployeeAndDate(String tenantId, UUID employeeId, LocalDate attendanceDate);

    Mono<PunchEventDto> savePunchEvent(PunchEventDto punchEvent);

    Flux<AttendanceRecordDto> findAttendanceByEmployeeAndDateRange(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
