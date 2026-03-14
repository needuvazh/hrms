package com.company.hrms.attendance.api;

import com.company.hrms.attendance.domain.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AttendanceRecordView(
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
}
