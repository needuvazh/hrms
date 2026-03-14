package com.company.hrms.attendance.api;

import java.time.LocalTime;

public record CreateShiftCommand(
        String shiftCode,
        String name,
        LocalTime startTime,
        LocalTime endTime
) {
}
