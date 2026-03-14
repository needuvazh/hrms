package com.company.hrms.leave.api;

import java.time.Instant;
import java.util.UUID;

public record LeaveBalanceView(
        UUID id,
        String tenantId,
        UUID employeeId,
        UUID leaveTypeId,
        int leaveYear,
        int totalDays,
        int usedDays,
        int remainingDays,
        Instant createdAt,
        Instant updatedAt
) {
}
