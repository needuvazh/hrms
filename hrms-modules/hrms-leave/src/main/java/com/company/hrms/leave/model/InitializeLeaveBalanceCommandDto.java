package com.company.hrms.leave.model;

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
public class InitializeLeaveBalanceCommandDto {
    private final UUID employeeId;
    private final UUID leaveTypeId;
    private final int leaveYear;
    private final int totalDays;
}
