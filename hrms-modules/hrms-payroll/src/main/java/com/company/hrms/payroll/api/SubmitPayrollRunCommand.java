package com.company.hrms.payroll.api;

import java.util.UUID;

public record SubmitPayrollRunCommand(
        UUID payrollRunId,
        String actor,
        String comments
) {
}
