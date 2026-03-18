package com.company.hrms.employee.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class EmployeeInformationDtos {

    private EmployeeInformationDtos() {
    }

    public enum WorkforceCategory {
        OMANI_NATIONAL,
        EXPATRIATE
    }

    public enum EmployeeStatus {
        ACTIVE,
        INACTIVE,
        PROBATION,
        SUSPENDED,
        TERMINATED
    }

    public enum AddressType {
        CURRENT,
        TEMPORARY,
        PERMANENT,
        WORK
    }

    public enum EmployeeDocumentType {
        PASSPORT,
        VISA,
        RESIDENCE_CARD,
        LABOUR_CARD,
        CIVIL_ID,
        EDUCATIONAL_CERTIFICATE,
        PROFESSIONAL_LICENSE,
        INSURANCE_CARD,
        CONTRACT,
        OFFER_LETTER,
        OTHER
    }

    public enum EmployeeDocumentVerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }

    public enum EmploymentActionType {
        JOINING,
        PROMOTION,
        TRANSFER,
        SECONDMENT,
        DESIGNATION_CHANGE,
        DEPARTMENT_CHANGE,
        LOCATION_CHANGE,
        GRADE_CHANGE,
        REPORTING_MANAGER_CHANGE
    }

    public enum DigitalOnboardingStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        COMPLETED
    }

    public enum ExpiryBucket {
        EXPIRED,
        DAYS_7,
        DAYS_30,
        DAYS_60,
        DAYS_90
    }

    public record EmployeeProfileUpsertRequest(
            String employeeCode,
            String firstName,
            String middleName,
            String lastName,
            String fullName,
            String arabicName,
            String profilePhotoPath,
            UUID genderId,
            LocalDate dateOfBirth,
            UUID maritalStatusId,
            UUID nationalityId,
            String bloodGroup,
            String personalEmail,
            String officialEmail,
            String primaryMobileNumber,
            String secondaryMobileNumber,
            String alternateContactNumber,
            EmployeeStatus employeeStatus,
            LocalDate dateOfJoining,
            LocalDate confirmationDate,
            LocalDate probationEndDate,
            LocalDate retirementDate,
            WorkforceCategory workforceCategory,
            UUID departmentId,
            UUID designationId,
            String jobTitle,
            UUID personId,
            String actor
    ) {
    }

    public record EmployeeProfileStatusUpdateRequest(
            EmployeeStatus employeeStatus,
            String actor
    ) {
    }

    public record EmployeeSearchFilter(
            String employeeCode,
            String employeeName,
            String officialEmail,
            String primaryMobileNumber,
            WorkforceCategory workforceCategory,
            EmployeeStatus employeeStatus,
            LocalDate joiningDateFrom,
            LocalDate joiningDateTo,
            UUID departmentId,
            UUID designationId,
            int limit,
            int offset
    ) {
    }

    public record EmployeeProfileView(
            UUID employeeId,
            String tenantId,
            String employeeCode,
            String firstName,
            String middleName,
            String lastName,
            String fullName,
            String arabicName,
            String profilePhotoPath,
            UUID genderId,
            LocalDate dateOfBirth,
            UUID maritalStatusId,
            UUID nationalityId,
            String bloodGroup,
            String personalEmail,
            String officialEmail,
            String primaryMobileNumber,
            String secondaryMobileNumber,
            String alternateContactNumber,
            EmployeeStatus employeeStatus,
            LocalDate dateOfJoining,
            LocalDate confirmationDate,
            LocalDate probationEndDate,
            LocalDate retirementDate,
            WorkforceCategory workforceCategory,
            UUID departmentId,
            UUID designationId,
            String jobTitle,
            UUID personId,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
    }

    public record EmployeeAddressUpsertRequest(
            AddressType addressType,
            String flatVillaNumber,
            String buildingName,
            String street,
            String area,
            String city,
            String stateProvince,
            UUID countryId,
            String postalCode,
            String poBox,
            String landmark,
            boolean primary,
            boolean active,
            String actor
    ) {
    }

    public record EmployeeAddressView(
            UUID addressId,
            UUID employeeId,
            AddressType addressType,
            String flatVillaNumber,
            String buildingName,
            String street,
            String area,
            String city,
            String stateProvince,
            UUID countryId,
            String postalCode,
            String poBox,
            String landmark,
            boolean primary,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record EmergencyContactUpsertRequest(
            String name,
            UUID relationshipTypeId,
            String primaryMobileNumber,
            String secondaryMobileNumber,
            String email,
            String addressLine1,
            String addressLine2,
            String city,
            UUID countryId,
            boolean primary,
            String remarks,
            boolean active,
            String actor
    ) {
    }

    public record EmergencyContactView(
            UUID emergencyContactId,
            UUID employeeId,
            String name,
            UUID relationshipTypeId,
            String primaryMobileNumber,
            String secondaryMobileNumber,
            String email,
            String addressLine1,
            String addressLine2,
            String city,
            UUID countryId,
            boolean primary,
            String remarks,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record DependantUpsertRequest(
            String fullName,
            UUID relationshipTypeId,
            UUID genderId,
            LocalDate dateOfBirth,
            UUID nationalityId,
            String passportNumber,
            String civilIdNumber,
            boolean insuranceEligible,
            boolean minor,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String status,
            String remarks,
            boolean active,
            String actor
    ) {
    }

    public record DependantView(
            UUID dependantId,
            UUID employeeId,
            String fullName,
            UUID relationshipTypeId,
            UUID genderId,
            LocalDate dateOfBirth,
            UUID nationalityId,
            String passportNumber,
            String civilIdNumber,
            boolean insuranceEligible,
            boolean minor,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String status,
            String remarks,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record BeneficiaryUpsertRequest(
            String fullName,
            UUID relationshipTypeId,
            LocalDate dateOfBirth,
            String contactNumber,
            String email,
            String address,
            String identificationNumber,
            BigDecimal allocationPercentage,
            Integer priorityOrder,
            boolean active,
            String remarks,
            String actor
    ) {
    }

    public record BeneficiaryView(
            UUID beneficiaryId,
            UUID employeeId,
            String fullName,
            UUID relationshipTypeId,
            LocalDate dateOfBirth,
            String contactNumber,
            String email,
            String address,
            String identificationNumber,
            BigDecimal allocationPercentage,
            Integer priorityOrder,
            boolean active,
            String remarks,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record WorkforceDetailUpsertRequest(
            WorkforceCategory workforceCategory,
            String pasiNumber,
            LocalDate pasiRegistrationDate,
            String permitNumber,
            String permitType,
            String sponsorName,
            String sponsorId,
            String visaStatus,
            LocalDate workPermitIssueDate,
            LocalDate workPermitExpiryDate,
            String remarks,
            String actor
    ) {
    }

    public record WorkforceDetailView(
            UUID workforceDetailId,
            UUID employeeId,
            WorkforceCategory workforceCategory,
            String pasiNumber,
            LocalDate pasiRegistrationDate,
            String permitNumber,
            String permitType,
            String sponsorName,
            String sponsorId,
            String visaStatus,
            LocalDate workPermitIssueDate,
            LocalDate workPermitExpiryDate,
            String remarks,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record EmployeeDocumentUpsertRequest(
            EmployeeDocumentType documentType,
            String documentName,
            String documentNumber,
            UUID issuingCountryId,
            String issuingAuthority,
            LocalDate issueDate,
            LocalDate expiryDate,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            EmployeeDocumentVerificationStatus verificationStatus,
            String remarks,
            boolean alertEnabled,
            boolean active,
            String actor
    ) {
    }

    public record EmployeeDocumentVerificationRequest(
            EmployeeDocumentVerificationStatus verificationStatus,
            String remarks,
            String actor
    ) {
    }

    public record EmployeeDocumentView(
            UUID employeeDocumentId,
            UUID employeeId,
            EmployeeDocumentType documentType,
            String documentName,
            String documentNumber,
            UUID issuingCountryId,
            String issuingAuthority,
            LocalDate issueDate,
            LocalDate expiryDate,
            String fileName,
            String filePath,
            String fileType,
            Long fileSize,
            EmployeeDocumentVerificationStatus verificationStatus,
            String verifiedBy,
            Instant verifiedDate,
            boolean alertEnabled,
            String remarks,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ExpiryDashboardView(
            long expired,
            long expiringIn7Days,
            long expiringIn30Days,
            long expiringIn60Days,
            long expiringIn90Days
    ) {
    }

    public record EmploymentHistoryUpsertRequest(
            EmploymentActionType actionType,
            UUID oldDepartmentId,
            UUID newDepartmentId,
            UUID oldDesignationId,
            UUID newDesignationId,
            UUID oldLocationId,
            UUID newLocationId,
            UUID oldGradeId,
            UUID newGradeId,
            UUID oldManagerId,
            UUID newManagerId,
            LocalDate effectiveDate,
            String remarks,
            String supportingDocumentPath,
            boolean active,
            String actor
    ) {
    }

    public record EmploymentHistoryView(
            UUID employmentHistoryId,
            UUID employeeId,
            EmploymentActionType actionType,
            UUID oldDepartmentId,
            UUID newDepartmentId,
            UUID oldDesignationId,
            UUID newDesignationId,
            UUID oldLocationId,
            UUID newLocationId,
            UUID oldGradeId,
            UUID newGradeId,
            UUID oldManagerId,
            UUID newManagerId,
            LocalDate effectiveDate,
            String remarks,
            String supportingDocumentPath,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record DigitalOnboardingUpsertRequest(
            boolean eFormCompleted,
            boolean documentUploadCompleted,
            boolean policyAcknowledged,
            String remarks,
            String actor
    ) {
    }

    public record DigitalOnboardingReviewRequest(
            String remarks,
            String actor
    ) {
    }

    public record DigitalOnboardingRejectRequest(
            String rejectedReason,
            String remarks,
            String actor
    ) {
    }

    public record PolicyAcknowledgementRequest(
            String policyCode,
            String policyVersion,
            boolean acceptedFlag,
            String acceptedBy,
            String remarks,
            String actor
    ) {
    }

    public record PolicyAcknowledgementView(
            UUID acknowledgementId,
            UUID onboardingId,
            String policyCode,
            String policyVersion,
            boolean acceptedFlag,
            Instant acceptedDateTime,
            String acceptedBy,
            String remarks,
            Instant createdAt
    ) {
    }

    public record DigitalOnboardingView(
            UUID onboardingId,
            UUID employeeId,
            DigitalOnboardingStatus onboardingStatus,
            boolean eFormCompleted,
            boolean documentUploadCompleted,
            boolean policyAcknowledged,
            Instant submittedDate,
            Instant reviewedDate,
            Instant approvedDate,
            String rejectedReason,
            String remarks,
            Instant createdAt,
            Instant updatedAt,
            List<PolicyAcknowledgementView> acknowledgements
    ) {
    }
}
