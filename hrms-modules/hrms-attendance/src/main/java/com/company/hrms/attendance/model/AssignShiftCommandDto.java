package com.company.hrms.attendance.model;

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
public class AssignShiftCommandDto {
    private final UUID employeeId;
    private final UUID shiftId;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
}
