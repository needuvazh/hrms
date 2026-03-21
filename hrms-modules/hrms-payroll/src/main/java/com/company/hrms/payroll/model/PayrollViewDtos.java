package com.company.hrms.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Payroll Period View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PayrollPeriodViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String periodCode;
    private final LocalDate periodStartDate;
    private final LocalDate periodEndDate;
    private final LocalDate paymentDate;
    private final String periodStatus; // DRAFT, LOCKED, PROCESSED, PAID
    private final Integer totalEmployees;
    private final BigDecimal totalGrossAmount;
    private final BigDecimal totalNetAmount;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Salary Structure View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SalaryStructureViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final UUID salaryGradeId;
    private final String salaryGradeName;
    private final String structureCode;
    private final String structureName;
    private final String structureType; // FIXED, VARIABLE, HYBRID
    private final BigDecimal basicSalary;
    private final List<SalaryComponent> earningComponents;
    private final List<SalaryComponent> deductionComponents;
    private final String currency;
    private final String status; // ACTIVE, INACTIVE
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Salary Component
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SalaryComponent {
    private final String componentCode;
    private final String componentName;
    private final String componentNameArabic;
    private final String componentType; // EARNING, DEDUCTION
    private final String calculationType; // FIXED, PERCENTAGE, FORMULA
    private final BigDecimal amount;
    private final String formula;
    private final Boolean isTaxable;
    private final Boolean isOmanisationRelated;
}

/**
 * Employee Payroll View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class EmployeePayrollViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollPeriodId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final UUID salaryStructureId;
    private final String salaryStructureName;
    private final BigDecimal basicSalary;
    private final BigDecimal grossAmount;
    private final BigDecimal totalEarnings;
    private final BigDecimal totalDeductions;
    private final BigDecimal netAmount;
    private final String paymentStatus; // PENDING, PROCESSED, PAID, REVERSED
    private final String paymentMethod; // BANK_TRANSFER, CASH, CHEQUE
    private final LocalDate paymentDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Payslip View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PayslipViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeePayrollId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String employeeNameArabic;
    private final UUID payrollPeriodId;
    private final LocalDate periodStartDate;
    private final LocalDate periodEndDate;
    private final BigDecimal basicSalary;
    private final List<PayslipLineItem> earnings;
    private final List<PayslipLineItem> deductions;
    private final BigDecimal grossAmount;
    private final BigDecimal totalDeductions;
    private final BigDecimal netAmount;
    private final String paymentMethod;
    private final LocalDate paymentDate;
    private final String payslipStatus; // DRAFT, GENERATED, SENT, VIEWED
    private final String documentUrl;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Payslip Line Item
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PayslipLineItem {
    private final String componentCode;
    private final String componentName;
    private final String componentNameArabic;
    private final BigDecimal amount;
    private final String description;
}

/**
 * PASI Contribution View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PasiContributionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeePayrollId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String pasiNumber;
    private final LocalDate contributionDate;
    private final BigDecimal basicSalary;
    private final BigDecimal employeeContributionRate;
    private final BigDecimal employeeContributionAmount;
    private final BigDecimal employerContributionRate;
    private final BigDecimal employerContributionAmount;
    private final BigDecimal totalContribution;
    private final String contributionStatus; // PENDING, SUBMITTED, CONFIRMED
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * WPS Export View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class WpsExportViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollPeriodId;
    private final String exportCode;
    private final LocalDate exportDate;
    private final Integer totalRecords;
    private final BigDecimal totalAmount;
    private final String fileFormat; // XML, CSV, TXT
    private final String bankCode;
    private final String bankName;
    private final String exportStatus; // DRAFT, GENERATED, SUBMITTED, CONFIRMED
    private final String fileUrl;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Salary Advance View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SalaryAdvanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final BigDecimal requestAmount;
    private final String reason;
    private final LocalDate requestDate;
    private final LocalDate approvalDate;
    private final String approvalStatus; // PENDING, APPROVED, REJECTED
    private final UUID approvedBy;
    private final BigDecimal approvedAmount;
    private final String paymentStatus; // PENDING, PAID
    private final LocalDate paymentDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Loan View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class LoanViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final BigDecimal loanAmount;
    private final BigDecimal interestRate;
    private final Integer loanTenureMonths;
    private final BigDecimal monthlyEMI;
    private final LocalDate loanStartDate;
    private final LocalDate loanEndDate;
    private final String loanType; // PERSONAL, HOUSING, VEHICLE, EDUCATION
    private final String loanStatus; // ACTIVE, CLOSED, DEFAULTED
    private final BigDecimal paidAmount;
    private final BigDecimal pendingAmount;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
