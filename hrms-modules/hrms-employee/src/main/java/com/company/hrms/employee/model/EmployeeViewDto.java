package com.company.hrms.employee.model;

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
 * Employee view DTO for API responses.
 * Comprehensive employee information including personal, employment, and compliance details.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EmployeeViewDto {
    // Basic Information
    private final UUID id;
    private final String tenantId;
    private final String employeeCode;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final String alternatePhoneNumber;
    
    // Personal Details
    private final LocalDate dateOfBirth;
    private final String gender; // MALE, FEMALE, OTHER
    private final String nationality;
    private final String maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED
    private final String religion;
    
    // Address
    private final String permanentAddressLine1;
    private final String permanentAddressLine2;
    private final String permanentCity;
    private final String permanentState;
    private final String permanentPostalCode;
    private final String permanentCountry;
    
    private final String currentAddressLine1;
    private final String currentAddressLine2;
    private final String currentCity;
    private final String currentState;
    private final String currentPostalCode;
    private final String currentCountry;
    
    // Employment Details
    private final UUID companyId;
    private final String companyName;
    private final UUID branchId;
    private final String branchName;
    private final UUID departmentId;
    private final String departmentName;
    private final UUID jobPositionId;
    private final String jobPositionName;
    private final UUID reportingManagerId;
    private final String reportingManagerName;
    
    // Employment Status
    private final String employmentType; // PERMANENT, CONTRACT, TEMPORARY, PROBATION
    private final LocalDate dateOfJoining;
    private final LocalDate dateOfConfirmation;
    private final String employmentStatus; // ACTIVE, INACTIVE, ON_LEAVE, SUSPENDED, TERMINATED
    
    // Salary Information
    private final UUID salaryGradeId;
    private final String salaryGradeName;
    private final BigDecimal basicSalary;
    private final String currency; // OMR, USD, etc.
    private final String paymentFrequency; // MONTHLY, WEEKLY, DAILY
    
    // Oman-Specific Fields
    private final String omanisationStatus; // OMANI, EXPATRIATE
    private final String passportNumber;
    private final LocalDate passportExpiryDate;
    private final String labourCardNumber;
    private final LocalDate labourCardExpiryDate;
    private final String visaNumber;
    private final LocalDate visaExpiryDate;
    private final String visaType; // WORK, RESIDENCE, etc.
    
    // PASI Information
    private final String pasiNumber;
    private final LocalDate pasiRegistrationDate;
    private final String pasiStatus; // ACTIVE, INACTIVE
    
    // Bank Details
    private final String bankName;
    private final String bankAccountNumber;
    private final String bankIBANNumber;
    private final String bankBranchCode;
    
    // Emergency Contact
    private final String emergencyContactName;
    private final String emergencyContactRelationship;
    private final String emergencyContactPhoneNumber;
    
    // Status
    private final Boolean isActive;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastModifiedAt;
}
