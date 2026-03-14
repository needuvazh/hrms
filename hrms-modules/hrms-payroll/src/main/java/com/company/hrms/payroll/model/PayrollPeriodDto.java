package com.company.hrms.payroll.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollPeriodDto(
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

    public PayrollPeriodDto close(Instant at) {
        return new PayrollPeriodDto(
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
