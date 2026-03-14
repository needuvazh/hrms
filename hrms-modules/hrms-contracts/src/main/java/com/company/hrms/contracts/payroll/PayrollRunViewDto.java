package com.company.hrms.contracts.payroll;

import java.time.Instant;
import java.util.UUID;

public record PayrollRunViewDto(
        UUID id,
        String tenantId,
        UUID payrollPeriodId,
        PayrollRunStatus status,
        UUID workflowInstanceId,
        String initiatedBy,
        String submittedBy,
        String reviewedBy,
        String notes,
        Instant submittedAt,
        Instant reviewedAt,
        Instant finalizedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
