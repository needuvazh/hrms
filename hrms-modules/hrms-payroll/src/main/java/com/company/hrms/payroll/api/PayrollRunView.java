package com.company.hrms.payroll.api;

import java.time.Instant;
import java.util.UUID;

public record PayrollRunView(
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
