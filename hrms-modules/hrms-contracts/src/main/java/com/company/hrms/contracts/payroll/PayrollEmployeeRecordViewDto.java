package com.company.hrms.contracts.payroll;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollEmployeeRecordViewDto(
        UUID id,
        String tenantId,
        UUID payrollRunId,
        UUID employeeId,
        BigDecimal grossAmount,
        BigDecimal totalDeductionAmount,
        BigDecimal netAmount,
        String remarks,
        Instant createdAt,
        Instant updatedAt
) {
}
