package com.company.hrms.attendance.domain;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record Shift(
        UUID id,
        String tenantId,
        String shiftCode,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
