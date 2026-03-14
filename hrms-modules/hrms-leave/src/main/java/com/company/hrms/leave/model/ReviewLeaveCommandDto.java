package com.company.hrms.leave.model;

import com.company.hrms.leave.model.LeaveStatus;
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
public class ReviewLeaveCommandDto {
    private final UUID leaveRequestId;
    private final LeaveStatus decision;
    private final String reviewer;
    private final String comments;
}
