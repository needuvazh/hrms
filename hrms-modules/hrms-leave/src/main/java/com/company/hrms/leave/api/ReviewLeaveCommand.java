package com.company.hrms.leave.api;

import com.company.hrms.leave.domain.LeaveStatus;
import java.util.UUID;

public record ReviewLeaveCommand(
        UUID leaveRequestId,
        LeaveStatus decision,
        String reviewer,
        String comments
) {
}
