package com.company.hrms.employee.events;

import java.time.Instant;
import java.util.UUID;

public record EmployeeCreatedEvent(
        UUID employeeId,
        String tenantId,
        Instant occurredAt
) {
}
