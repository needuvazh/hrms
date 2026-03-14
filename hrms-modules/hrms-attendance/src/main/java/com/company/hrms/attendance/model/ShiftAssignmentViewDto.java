package com.company.hrms.attendance.model;

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
public class ShiftAssignmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final UUID shiftId;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
