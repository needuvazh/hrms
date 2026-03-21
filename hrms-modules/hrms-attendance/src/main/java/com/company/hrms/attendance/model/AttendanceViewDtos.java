package com.company.hrms.attendance.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Attendance Record View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AttendanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate attendanceDate;
    private final LocalDateTime punchInTime;
    private final LocalDateTime punchOutTime;
    private final UUID shiftId;
    private final String shiftName;
    private final LocalDateTime expectedPunchInTime;
    private final LocalDateTime expectedPunchOutTime;
    private final String attendanceStatus; // PRESENT, ABSENT, LATE, HALF_DAY, LEAVE, HOLIDAY
    private final String punchSource; // BIOMETRIC, MOBILE, MANUAL, SYSTEM
    private final BigDecimal workingHours;
    private final BigDecimal overtimeHours;
    private final String remarks;
    private final Boolean isApproved;
    private final UUID approvedBy;
    private final Instant approvedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Biometric Punch View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class BiometricPunchViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDateTime punchTime;
    private final String punchType; // IN, OUT
    private final String biometricDeviceId;
    private final String biometricDeviceName;
    private final String fingerprintId;
    private final String verificationStatus; // SUCCESS, FAILED, RETRY
    private final String punchLocation;
    private final String ipAddress;
    private final String remarks;
    private final Instant createdAt;
}

/**
 * Shift View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ShiftViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String shiftCode;
    private final String shiftName;
    private final String shiftNameArabic;
    private final String shiftType; // FIXED, ROTATING, FLEXIBLE
    private final String startTime;
    private final String endTime;
    private final BigDecimal workingHours;
    private final BigDecimal breakDuration;
    private final String breakStartTime;
    private final String breakEndTime;
    private final String applicableDays; // MON,TUE,WED,THU,FRI,SAT,SUN
    private final String status; // ACTIVE, INACTIVE
    private final Integer employeeCount;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Shift Assignment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ShiftAssignmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final UUID shiftId;
    private final String shiftName;
    private final LocalDate assignmentStartDate;
    private final LocalDate assignmentEndDate;
    private final String assignmentType; // PERMANENT, TEMPORARY, ROTATIONAL
    private final String status; // ACTIVE, INACTIVE
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Overtime Record View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class OvertimeViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate overtimeDate;
    private final BigDecimal overtimeHours;
    private final String overtimeType; // DAILY, WEEKLY, HOLIDAY
    private final String reason;
    private final String approvalStatus; // PENDING, APPROVED, REJECTED
    private final UUID approvedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Attendance Mismatch View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class AttendanceMismatchViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID attendanceRecordId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate mismatchDate;
    private final String mismatchType; // LATE_PUNCH_IN, EARLY_PUNCH_OUT, MISSING_PUNCH_OUT, MISSING_PUNCH_IN
    private final LocalDateTime expectedTime;
    private final LocalDateTime actualTime;
    private final BigDecimal variance;
    private final String reason;
    private final String correctionStatus; // PENDING, CORRECTED, REJECTED
    private final UUID correctedBy;
    private final Instant createdAt;
    private final Instant updatedAt;
}
