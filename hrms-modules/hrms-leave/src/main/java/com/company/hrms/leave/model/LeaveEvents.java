package com.company.hrms.leave.model;

import java.time.Instant;
import java.util.UUID;

public final class LeaveEvents {

    private LeaveEvents() {
    }

    public record LeaveAppliedEvent(UUID leaveRequestId, UUID employeeId, String tenantId, Instant occurredAt) {
    }

    public record LeaveReviewedEvent(UUID leaveRequestId, String decision, String tenantId, Instant occurredAt) {
    }
}
