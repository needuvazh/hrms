package com.company.hrms.payroll.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollPeriodView(
        UUID id,
        String tenantId,
        String periodCode,
        LocalDate startDate,
        LocalDate endDate,
        PayrollPeriodStatus status,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
