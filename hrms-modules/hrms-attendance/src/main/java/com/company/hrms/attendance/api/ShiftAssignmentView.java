package com.company.hrms.attendance.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShiftAssignmentView(
        UUID id,
        String tenantId,
        UUID employeeId,
        UUID shiftId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
