package com.company.hrms.attendance.domain;

import java.time.Instant;
import java.util.UUID;

public record PunchEvent(
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
