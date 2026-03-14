package com.company.hrms.attendance.api;

import java.time.LocalDate;
import java.util.UUID;

public record AssignShiftCommand(
        UUID employeeId,
        UUID shiftId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) {
}
