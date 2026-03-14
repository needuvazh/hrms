package com.company.hrms.attendance.model;

import com.company.hrms.attendance.model.PunchType;
import java.time.Instant;
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
public class RecordPunchCommandDto {
    private final UUID employeeId;
    private final PunchType punchType;
    private final Instant eventTime;
    private final String source;
}
