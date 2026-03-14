package com.company.hrms.leave.api;

import java.util.UUID;

public record InitializeLeaveBalanceCommand(
        UUID employeeId,
        UUID leaveTypeId,
        int leaveYear,
        int totalDays
) {
}
