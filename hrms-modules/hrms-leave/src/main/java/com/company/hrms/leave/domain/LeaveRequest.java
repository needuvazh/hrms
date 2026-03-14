package com.company.hrms.leave.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequest(
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

    public LeaveRequest decide(LeaveStatus decision, String reviewer, Instant at) {
        return new LeaveRequest(
                id,
                tenantId,
                employeeId,
                leaveTypeId,
                fromDate,
                toDate,
                requestedDays,
                reason,
                decision,
                workflowInstanceId,
                requestedBy,
                reviewer,
                createdAt,
                at);
    }
}
