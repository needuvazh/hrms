package com.company.hrms.leave.model;

import java.time.Instant;
import java.util.UUID;

public record LeaveBalanceDto(
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

    public LeaveBalanceDto consume(int days, Instant at) {
        int updatedUsed = usedDays + days;
        int updatedRemaining = remainingDays - days;
        return new LeaveBalanceDto(
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
