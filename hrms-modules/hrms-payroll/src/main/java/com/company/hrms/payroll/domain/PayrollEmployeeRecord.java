package com.company.hrms.payroll.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayrollEmployeeRecord(
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
