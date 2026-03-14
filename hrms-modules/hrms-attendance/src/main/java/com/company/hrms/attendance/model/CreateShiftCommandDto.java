package com.company.hrms.attendance.model;

import java.time.LocalTime;
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
public class CreateShiftCommandDto {
    private final String shiftCode;
    private final String name;
    private final LocalTime startTime;
    private final LocalTime endTime;
}
