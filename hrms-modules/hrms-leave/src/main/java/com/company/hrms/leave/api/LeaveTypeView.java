package com.company.hrms.leave.api;

import java.time.Instant;
import java.util.UUID;

public record LeaveTypeView(
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
