package com.company.hrms.payroll.api;

import java.util.UUID;

public record StartPayrollRunCommand(
        UUID payrollPeriodId,
        String initiatedBy,
        String notes
) {
}
