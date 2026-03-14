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
public class LeaveBalanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final UUID leaveTypeId;
    private final int leaveYear;
    private final int totalDays;
    private final int usedDays;
    private final int remainingDays;
    private final Instant createdAt;
    private final Instant updatedAt;
}
