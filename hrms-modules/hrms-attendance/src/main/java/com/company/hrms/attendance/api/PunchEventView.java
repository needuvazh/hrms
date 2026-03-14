package com.company.hrms.attendance.api;

import com.company.hrms.attendance.domain.PunchType;
import java.time.Instant;
import java.util.UUID;

public record PunchEventView(
        UUID id,
        String tenantId,
        UUID employeeId,
        UUID shiftId,
        UUID attendanceRecordId,
        PunchType punchType,
        Instant eventTime,
        String source,
        Instant createdAt
) {
}
