package com.company.hrms.attendance.model;

import com.company.hrms.attendance.model.AttendanceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AttendanceRecordViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final LocalDate attendanceDate;
    private final UUID shiftId;
    private final AttendanceStatus status;
    private final Instant firstPunchIn;
    private final Instant lastPunchOut;
    private final Instant createdAt;
    private final Instant updatedAt;
}
