package com.company.hrms.attendance.api;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceQuery(
        UUID employeeId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
