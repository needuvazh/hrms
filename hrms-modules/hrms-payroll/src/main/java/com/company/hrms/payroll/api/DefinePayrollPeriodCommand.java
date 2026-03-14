package com.company.hrms.payroll.api;

import java.time.LocalDate;

public record DefinePayrollPeriodCommand(
        String periodCode,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {
}
