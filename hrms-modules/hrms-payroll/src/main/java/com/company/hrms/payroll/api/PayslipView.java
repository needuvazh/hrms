package com.company.hrms.payroll.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PayslipView(
        UUID id,
        String tenantId,
        PayrollEmployeeRecordView payrollEmployeeRecord,
        UUID documentRecordId,
        String artifactObjectKey,
        String artifactContentType,
        List<PayrollAmountComponent> earnings,
        List<PayrollAmountComponent> deductions,
        Instant generatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
