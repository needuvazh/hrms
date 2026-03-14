package com.company.hrms.leave.model;

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
public class ApplyLeaveCommandDto {
    private final UUID employeeId;
    private final UUID leaveTypeId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String reason;
    private final String requestedBy;
}
