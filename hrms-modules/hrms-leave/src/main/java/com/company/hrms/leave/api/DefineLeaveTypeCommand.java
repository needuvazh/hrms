package com.company.hrms.leave.api;

public record DefineLeaveTypeCommand(
        String leaveCode,
        String name,
        boolean paid,
        int annualLimitDays
) {
}
