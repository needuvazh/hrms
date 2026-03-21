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
 * Leave Balance view DTO for API responses.
 * Represents an employee's leave balance for a specific leave type.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LeaveBalanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    
    // Leave type
    private final UUID leaveTypeId;
    private final String leaveTypeCode;
    private final String leaveTypeName;
    
    // Financial year
    private final Integer financialYear;
    private final LocalDate yearStartDate;
    private final LocalDate yearEndDate;
    
    // Balance calculation
    private final BigDecimal openingBalance;
    private final BigDecimal entitlementForYear;
    private final BigDecimal carryForwardFromPreviousYear;
    private final BigDecimal totalAvailable;
    
    // Utilization
    private final BigDecimal leaveTaken;
    private final BigDecimal leaveApproved;
    private final BigDecimal leavePending;
    private final BigDecimal leaveRejected;
    
    // Encashment
    private final BigDecimal leaveEncashed;
    private final BigDecimal leaveForfeited;
    
    // Current balance
    private final BigDecimal closingBalance;
    private final BigDecimal carryForwardToNextYear;
    
    // Status
    private final Boolean isActive;
    private final Instant createdAt;
    private final Instant updatedAt;
}
