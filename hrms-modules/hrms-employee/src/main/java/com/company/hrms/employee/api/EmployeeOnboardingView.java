package com.company.hrms.employee.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmployeeOnboardingView(
        UUID employeeId,
        String tenantId,
        OnboardingStatus status,
        UUID workflowInstanceId,
        List<OnboardingStepView> steps,
        Instant completedAt
) {

    public enum OnboardingStatus {
        SUCCESS,
        PARTIAL_FAILED
    }

    public record OnboardingStepView(
            String step,
            String status,
            String message
    ) {
    }
}
