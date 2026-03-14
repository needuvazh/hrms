package com.company.hrms.leave.domain;

import java.time.Instant;
import java.util.UUID;

public record LeaveBalance(
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

    public LeaveBalance consume(int days, Instant at) {
        int updatedUsed = usedDays + days;
        int updatedRemaining = remainingDays - days;
        return new LeaveBalance(
                id,
                tenantId,
                employeeId,
                leaveTypeId,
                leaveYear,
                totalDays,
                updatedUsed,
                updatedRemaining,
                createdAt,
                at);
    }
}
