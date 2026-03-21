package com.company.hrms.leave.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Leave Application view DTO for API responses.
 * Represents an employee's leave request with multi-level approval workflow.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeaveApplicationViewDto {
    private final UUID id;
    private final String tenantId;
    private final String applicationNumber;
    
    // Employee information
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final UUID departmentId;
    private final String departmentName;
    private final UUID reportingManagerId;
    private final String reportingManagerName;
    
    // Leave details
    private final UUID leaveTypeId;
    private final String leaveTypeName;
    private final LocalDate leaveStartDate;
    private final LocalDate leaveEndDate;
    private final Integer numberOfDays;
    private final Boolean isHalfDay;
    private final String halfDayType; // FIRST_HALF, SECOND_HALF
    
    // Application details
    private final String reason;
    private final String contactPhoneNumber;
    private final String contactAddress;
    private final String alternateContactPerson;
    
    // Approval workflow
    private final String applicationStatus; // DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED
    private final Integer currentApprovalLevel;
    private final Integer totalApprovalLevels;
    private final String lastApprovedBy;
    private final Instant lastApprovedAt;
    private final String rejectionReason;
    
    // Leave balance impact
    private final BigDecimal balanceBeforeLeave;
    private final BigDecimal balanceAfterLeave;
    
    // Encashment details (if applicable)
    private final Boolean isEncashmentRequested;
    private final BigDecimal encashmentAmount;
    
    // Air ticket entitlement (for expatriates)
    private final Boolean isAirTicketEncashmentRequested;
    private final BigDecimal airTicketEncashmentAmount;
    
    // Attachment
    private final String attachmentUrl;
    
    // Status
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant submittedAt;
}
