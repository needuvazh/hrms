package com.company.hrms.separation.model;

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
 * Resignation View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ResignationViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate resignationDate;
    private final LocalDate noticePeriodStartDate;
    private final LocalDate noticePeriodEndDate;
    private final Integer noticePeriodDays;
    private final String resignationReason;
    private final String resignationReasonCategory; // BETTER_OPPORTUNITY, RELOCATION, FAMILY, SALARY, OTHERS
    private final String resignationStatus; // SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN
    private final UUID acceptedBy;
    private final LocalDate acceptanceDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * EOSB Calculation View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class EosbCalculationViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate separationDate;
    private final LocalDate dateOfJoining;
    private final BigDecimal totalServiceYears;
    private final BigDecimal basicSalary;
    private final String currency;
    private final BigDecimal lastMonthSalary;
    private final BigDecimal eosbAmount;
    private final String eosbCalculationMethod; // OMAN_LABOUR_LAW, COMPANY_POLICY
    private final String eosbStatus; // CALCULATED, APPROVED, PAID
    private final LocalDate paymentDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Exit Clearance View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ExitClearanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate separationDate;
    private final String clearanceStatus; // PENDING, IN_PROGRESS, COMPLETED
    private final ExitClearanceItem hrClearance;
    private final ExitClearanceItem financeClearance;
    private final ExitClearanceItem itClearance;
    private final ExitClearanceItem facilitiesClearance;
    private final ExitClearanceItem assetsClearance;
    private final String overallStatus; // PENDING, CLEARED, NOT_CLEARED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Exit Clearance Item
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ExitClearanceItem {
    private final String departmentCode;
    private final String departmentName;
    private final String clearanceStatus; // PENDING, CLEARED, NOT_APPLICABLE
    private final String remarks;
    private final UUID clearedBy;
    private final LocalDate clearedDate;
}

/**
 * Full and Final Settlement View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class FullFinalSettlementViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate separationDate;
    private final BigDecimal basicSalary;
    private final String currency;
    private final BigDecimal salaryForNoticeMonth;
    private final BigDecimal unpaidLeaveEncashment;
    private final BigDecimal eosbAmount;
    private final BigDecimal gratuityAmount;
    private final BigDecimal totalEarnings;
    private final BigDecimal loanRecovery;
    private final BigDecimal advanceRecovery;
    private final BigDecimal otherDeductions;
    private final BigDecimal totalDeductions;
    private final BigDecimal netPayableAmount;
    private final String settlementStatus; // PENDING, APPROVED, PAID
    private final LocalDate paymentDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Experience Certificate View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ExperienceCertificateViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String employeeNameArabic;
    private final String designation;
    private final String designationArabic;
    private final LocalDate dateOfJoining;
    private final LocalDate dateOfSeparation;
    private final BigDecimal totalServiceYears;
    private final String performanceRating;
    private final String certificateStatus; // DRAFT, GENERATED, SIGNED, ISSUED
    private final String documentUrl;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Separation Checklist View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SeparationChecklistViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate separationDate;
    private final SeparationTask resignationProcessing;
    private final SeparationTask noticePeriodCompletion;
    private final SeparationTask exitClearance;
    private final SeparationTask eosbCalculation;
    private final SeparationTask finalSettlement;
    private final SeparationTask documentCollection;
    private final SeparationTask assetRecovery;
    private final SeparationTask experienceCertificate;
    private final String overallStatus; // PENDING, IN_PROGRESS, COMPLETED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Separation Task
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SeparationTask {
    private final String taskCode;
    private final String taskName;
    private final String taskNameArabic;
    private final String taskStatus; // PENDING, IN_PROGRESS, COMPLETED
    private final UUID assignedTo;
    private final LocalDate dueDate;
    private final LocalDate completionDate;
    private final String remarks;
}

/**
 * Re-employment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ReemploymentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate previousSeparationDate;
    private final LocalDate reemploymentDate;
    private final String reemploymentReason;
    private final String reemploymentType; // REJOINED, REHIRED, RECALLED
    private final String reemploymentStatus; // PENDING, APPROVED, REJECTED
    private final UUID approvedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
