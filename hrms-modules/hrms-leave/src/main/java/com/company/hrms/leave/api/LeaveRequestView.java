package com.company.hrms.leave.api;

import com.company.hrms.leave.domain.LeaveStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestView(
        UUID id,
        String tenantId,
        UUID employeeId,
        UUID leaveTypeId,
        LocalDate fromDate,
        LocalDate toDate,
        int requestedDays,
        String reason,
        LeaveStatus status,
        UUID workflowInstanceId,
        String requestedBy,
        String reviewedBy,
        Instant createdAt,
        Instant updatedAt
) {
}
