package com.company.hrms.attendance.model;

import java.time.Instant;
import java.time.LocalTime;
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
public class ShiftDto {
    private final UUID id;
    private final String tenantId;
    private final String shiftCode;
    private final String name;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
