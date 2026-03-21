package com.company.hrms.leave.model;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Leave Type view DTO for API responses.
 * Represents different types of leave available in the system.
 * Oman Labour Law compliant.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeaveTypeViewDto {
    private final UUID id;
    private final String tenantId;
    private final String leaveTypeCode;
    private final String leaveTypeName;
    private final String description;
    
    // Leave entitlement details
    private final Integer annualEntitlementDays;
    private final Boolean isCarryForwardAllowed;
    private final Integer maxCarryForwardDays;
    private final Boolean isEncashmentAllowed;
    private final Boolean isPaidLeave;
    
    // Labour Law compliance (Oman specific)
    private final Boolean isLabourLawEntitled;
    private final String labourLawArticleReference;
    
    // Approval workflow
    private final Integer approvalLevelRequired;
    private final Boolean requiresHRApproval;
    
    // Restrictions
    private final Integer minimumLeaveDays;
    private final Integer maximumConsecutiveDays;
    private final Boolean allowPartialDays;
    private final Boolean allowHalfDays;
    
    // Hijri calendar (Islamic calendar) support
    private final Boolean isIslamicHoliday;
    private final String hijriDate;
    
    // Status
    private final Boolean isActive;
    private final Integer displayOrder;
    private final Instant createdAt;
    private final Instant updatedAt;
}
