package com.company.hrms.payroll.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollPeriod(
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

    public PayrollPeriod close(Instant at) {
        return new PayrollPeriod(
                id,
                tenantId,
                periodCode,
                startDate,
                endDate,
                PayrollPeriodStatus.CLOSED,
                description,
                createdAt,
                at);
    }
}
