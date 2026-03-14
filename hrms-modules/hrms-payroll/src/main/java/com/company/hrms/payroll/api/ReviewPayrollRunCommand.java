package com.company.hrms.payroll.api;

import java.util.UUID;

public record ReviewPayrollRunCommand(
        UUID payrollRunId,
        PayrollApprovalDecision decision,
        String actor,
        String comments
) {
}
