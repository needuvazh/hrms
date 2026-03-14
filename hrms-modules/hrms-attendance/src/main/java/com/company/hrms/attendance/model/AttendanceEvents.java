package com.company.hrms.attendance.model;

import java.time.Instant;
import java.util.UUID;

public final class AttendanceEvents {

    private AttendanceEvents() {
    }

    public record PunchRecordedEvent(UUID punchEventId, UUID employeeId, String tenantId, Instant occurredAt) {
    }

    public record ShiftAssignedEvent(UUID assignmentId, UUID employeeId, UUID shiftId, String tenantId, Instant occurredAt) {
    }
}
