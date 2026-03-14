package com.company.hrms.payroll.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EarningComponent(
        UUID id,
        UUID payrollEmployeeRecordId,
        String code,
        String name,
        BigDecimal amount,
        Instant createdAt
) {
}
