package com.company.hrms.employee.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EmployeeOnboardingViewDto(
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
