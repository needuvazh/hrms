package com.company.hrms.leave.api;

import java.time.LocalDate;
import java.util.UUID;

public record ApplyLeaveCommand(
        UUID employeeId,
        UUID leaveTypeId,
        LocalDate fromDate,
        LocalDate toDate,
        String reason,
        String requestedBy
) {
}
