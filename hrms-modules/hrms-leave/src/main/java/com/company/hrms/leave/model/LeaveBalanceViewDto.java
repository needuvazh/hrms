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

    public LeaveBalanceViewDto(
            UUID id,
            String tenantId,
            UUID employeeId,
            UUID leaveTypeId,
            Integer financialYear,
            Integer totalDays,
            Integer usedDays,
            Integer remainingDays,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                tenantId,
                employeeId,
                null,
                null,
                leaveTypeId,
                null,
                null,
                financialYear,
                LocalDate.of(financialYear, 1, 1),
                LocalDate.of(financialYear, 12, 31),
                BigDecimal.valueOf(totalDays),
                BigDecimal.valueOf(totalDays),
                BigDecimal.ZERO,
                BigDecimal.valueOf(totalDays),
                BigDecimal.valueOf(usedDays),
                BigDecimal.valueOf(usedDays),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(remainingDays),
                BigDecimal.valueOf(remainingDays),
                true,
                createdAt,
                updatedAt);
    }

    public int usedDays() {
        return leaveTaken.intValue();
    }

    public int remainingDays() {
        return closingBalance.intValue();
    }
}
