package com.company.hrms.attendance.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordDto(
        UUID id,
        String tenantId,
        UUID employeeId,
        LocalDate attendanceDate,
        UUID shiftId,
        AttendanceStatus status,
        Instant firstPunchIn,
        Instant lastPunchOut,
        Instant createdAt,
        Instant updatedAt
) {

    public AttendanceRecordDto markPunchOut(Instant punchOutTime, Instant updatedAtValue) {
        Instant last = lastPunchOut == null || punchOutTime.isAfter(lastPunchOut) ? punchOutTime : lastPunchOut;
        return new AttendanceRecordDto(
                id,
                tenantId,
                employeeId,
                attendanceDate,
                shiftId,
                AttendanceStatus.PRESENT,
                firstPunchIn,
                last,
                createdAt,
                updatedAtValue);
    }

    public AttendanceRecordDto markPunchIn(Instant punchInTime, Instant updatedAtValue) {
        Instant first = firstPunchIn == null || punchInTime.isBefore(firstPunchIn) ? punchInTime : firstPunchIn;
        return new AttendanceRecordDto(
                id,
                tenantId,
                employeeId,
                attendanceDate,
                shiftId,
                AttendanceStatus.IN_PROGRESS,
                first,
                lastPunchOut,
                createdAt,
                updatedAtValue);
    }
}
