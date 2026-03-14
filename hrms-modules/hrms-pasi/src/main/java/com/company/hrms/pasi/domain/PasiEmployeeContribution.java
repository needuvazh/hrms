package com.company.hrms.pasi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PasiEmployeeContribution(
        UUID id,
        String tenantId,
        UUID pasiPeriodRecordId,
        UUID payrollEmployeeRecordId,
        UUID employeeId,
        BigDecimal contributableSalary,
        BigDecimal employeeContribution,
        BigDecimal employerContribution,
        BigDecimal totalContribution,
        Instant createdAt
) {
}
