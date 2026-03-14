package com.company.hrms.payroll.api;

import java.util.List;
import java.util.UUID;

public record AttachPayrollEmployeeRecordCommand(
        UUID payrollRunId,
        UUID employeeId,
        List<PayrollAmountComponent> earnings,
        List<PayrollAmountComponent> deductions,
        String remarks
) {
}
