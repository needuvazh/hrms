package com.company.hrms.leave.model;

import com.company.hrms.leave.model.LeaveStatus;
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
public class LeaveRequestViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final UUID leaveTypeId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final int requestedDays;
    private final String reason;
    private final LeaveStatus status;
    private final UUID workflowInstanceId;
    private final String requestedBy;
    private final String reviewedBy;
    private final Instant createdAt;
    private final Instant updatedAt;
}
