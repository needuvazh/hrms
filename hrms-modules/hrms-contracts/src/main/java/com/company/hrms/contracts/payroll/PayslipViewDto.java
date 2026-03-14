package com.company.hrms.contracts.payroll;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PayslipViewDto(
        UUID id,
        String tenantId,
        PayrollEmployeeRecordViewDto payrollEmployeeRecord,
        UUID documentRecordId,
        String artifactObjectKey,
        String artifactContentType,
        List<PayrollAmountComponentDto> earnings,
        List<PayrollAmountComponentDto> deductions,
        Instant generatedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
