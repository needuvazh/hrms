package com.company.hrms.employee.events;

import java.time.Instant;
import java.util.UUID;

public final class EmployeeOnboardingEvents {

    private EmployeeOnboardingEvents() {
    }

    public record EmployeeOnboardingStartedEvent(UUID employeeId, String tenantId, String actor, Instant occurredAt) {
    }

    public record EmployeeOnboardingStepEvent(UUID employeeId, String tenantId, String step, String status, String details, Instant occurredAt) {
    }

    public record EmployeeOnboardingCompletedEvent(UUID employeeId, String tenantId, String status, Instant occurredAt) {
    }
}
