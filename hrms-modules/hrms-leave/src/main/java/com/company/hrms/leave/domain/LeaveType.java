package com.company.hrms.leave.domain;

import java.time.Instant;
import java.util.UUID;

public record LeaveType(
        UUID id,
        String tenantId,
        String leaveCode,
        String name,
        boolean paid,
        int annualLimitDays,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
