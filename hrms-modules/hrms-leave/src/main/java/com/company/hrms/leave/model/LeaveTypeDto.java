package com.company.hrms.leave.model;

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
public class LeaveTypeDto {
    private final UUID id;
    private final String tenantId;
    private final String leaveCode;
    private final String name;
    private final boolean paid;
    private final int annualLimitDays;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
