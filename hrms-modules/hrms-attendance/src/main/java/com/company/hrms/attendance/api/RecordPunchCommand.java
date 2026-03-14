package com.company.hrms.attendance.api;

import com.company.hrms.attendance.domain.PunchType;
import java.time.Instant;
import java.util.UUID;

public record RecordPunchCommand(
        UUID employeeId,
        PunchType punchType,
        Instant eventTime,
        String source
) {
}
