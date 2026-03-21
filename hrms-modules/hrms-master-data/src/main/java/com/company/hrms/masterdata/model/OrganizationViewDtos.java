package com.company.hrms.masterdata.model;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Company View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CompanyViewDto {
    private final UUID id;
    private final String tenantId;
    private final String companyCode;
    private final String companyName;
    private final String companyNameArabic;
    private final String registrationNumber;
    private final String taxRegistrationNumber;
    private final String industryType;
    private final String companySize; // SMALL, MEDIUM, LARGE
    private final String country;
    private final String state;
    private final String city;
    private final String addressLine1;
    private final String addressLine2;
    private final String postalCode;
    private final String phoneNumber;
    private final String email;
    private final String website;
    private final String logoUrl;
    private final String status; // ACTIVE, INACTIVE
    private final Boolean isHeadquarters;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Branch View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class BranchViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String branchCode;
    private final String branchName;
    private final String branchNameArabic;
    private final String branchType; // HEAD_OFFICE, REGIONAL, OPERATIONAL
    private final String country;
    private final String state;
    private final String city;
    private final String addressLine1;
    private final String addressLine2;
    private final String postalCode;
    private final String phoneNumber;
    private final String email;
    private final String managerName;
    private final String status; // ACTIVE, INACTIVE
    private final Integer employeeCount;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Department View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class DepartmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final UUID branchId;
    private final String branchName;
    private final UUID parentDepartmentId;
    private final String parentDepartmentName;
    private final String departmentCode;
    private final String departmentName;
    private final String departmentNameArabic;
    private final String departmentType; // FUNCTIONAL, COST_CENTER, BUSINESS_UNIT
    private final UUID departmentHeadId;
    private final String departmentHeadName;
    private final String description;
    private final String status; // ACTIVE, INACTIVE
    private final Integer employeeCount;
    private final Integer budgetAllocation;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Job Position View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class JobPositionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final UUID departmentId;
    private final String departmentName;
    private final String positionCode;
    private final String positionName;
    private final String positionNameArabic;
    private final String positionLevel; // EXECUTIVE, MANAGER, SUPERVISOR, STAFF
    private final String positionCategory; // TECHNICAL, ADMINISTRATIVE, OPERATIONAL
    private final UUID salaryGradeId;
    private final String salaryGradeName;
    private final String description;
    private final String keyResponsibilities;
    private final String requiredQualifications;
    private final String requiredExperience;
    private final Integer numberOfPositions;
    private final Integer filledPositions;
    private final String status; // ACTIVE, INACTIVE
    private final Boolean isOmanisationRequired;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Salary Grade View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SalaryGradeViewDto {
    private final UUID id;
    private final String tenantId;
    private final String gradeCode;
    private final String gradeName;
    private final String gradeNameArabic;
    private final Integer gradeLevel;
    private final java.math.BigDecimal minimumSalary;
    private final java.math.BigDecimal maximumSalary;
    private final java.math.BigDecimal midPointSalary;
    private final String currency;
    private final String description;
    private final String status; // ACTIVE, INACTIVE
    private final Instant createdAt;
    private final Instant updatedAt;
}
