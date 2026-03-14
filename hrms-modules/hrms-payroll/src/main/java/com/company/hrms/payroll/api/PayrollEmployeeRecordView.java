package com.company.hrms.payroll.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollEmployeeRecordView(
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
