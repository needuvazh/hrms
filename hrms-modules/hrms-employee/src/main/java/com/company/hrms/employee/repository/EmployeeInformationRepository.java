package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeInformationRepository {

    Mono<Long> nextEmployeeCodeSequence();

    Mono<EmployeeProfileView> createEmployee(String tenantId, EmployeeProfileUpsertRequest request, String employeeCode);

    Mono<EmployeeProfileView> updateEmployee(String tenantId, UUID employeeId, EmployeeProfileUpsertRequest request);

    Mono<EmployeeProfileView> findEmployeeById(String tenantId, UUID employeeId);

    Mono<EmployeeProfileView> findEmployeeByCode(String tenantId, String employeeCode);

    Flux<EmployeeProfileView> searchEmployees(String tenantId, EmployeeSearchFilter filter);

    Mono<EmployeeProfileView> updateEmployeeStatus(String tenantId, UUID employeeId, EmployeeStatus employeeStatus, String actor);

    Mono<Boolean> existsEmployeeById(String tenantId, UUID employeeId);

    Mono<Boolean> existsOfficialEmail(String tenantId, String officialEmail, UUID excludeEmployeeId);

    Mono<Boolean> existsCountry(UUID countryId);

    Mono<UUID> findCountryIdByCode(String countryCode);

    Mono<Boolean> existsNationality(UUID nationalityId);

    Mono<Boolean> existsGender(UUID genderId);

    Mono<Boolean> existsMaritalStatus(UUID maritalStatusId);

    Mono<Boolean> existsRelationshipType(UUID relationshipTypeId);

    Mono<Boolean> existsDepartment(String tenantId, UUID departmentId);

    Mono<Boolean> existsDesignation(String tenantId, UUID designationId);

    Mono<Boolean> existsWorkLocation(String tenantId, UUID locationId);

    Mono<Boolean> existsGrade(String tenantId, UUID gradeId);

    Mono<Boolean> existsEmployee(String tenantId, UUID employeeId);

    Mono<EmployeeAddressView> createAddress(String tenantId, UUID employeeId, EmployeeAddressUpsertRequest request);

    Mono<EmployeeAddressView> updateAddress(String tenantId, UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request);

    Mono<EmployeeAddressView> findAddressById(String tenantId, UUID employeeId, UUID addressId);

    Flux<EmployeeAddressView> findAddressesByEmployeeId(String tenantId, UUID employeeId);

    Mono<EmployeeAddressView> deactivateAddress(String tenantId, UUID employeeId, UUID addressId, String actor);

    Mono<Void> clearPrimaryAddressByType(String tenantId, UUID employeeId, AddressType addressType, UUID excludeAddressId);

    Mono<EmergencyContactView> createEmergencyContact(String tenantId, UUID employeeId, EmergencyContactUpsertRequest request);

    Mono<EmergencyContactView> updateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request);

    Mono<EmergencyContactView> findEmergencyContactById(String tenantId, UUID employeeId, UUID emergencyContactId);

    Flux<EmergencyContactView> findEmergencyContactsByEmployeeId(String tenantId, UUID employeeId);

    Mono<EmergencyContactView> deactivateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, String actor);

    Mono<Void> clearPrimaryEmergencyContact(String tenantId, UUID employeeId, UUID excludeEmergencyContactId);

    Mono<DependantView> createDependant(String tenantId, UUID employeeId, DependantUpsertRequest request);

    Mono<DependantView> updateDependant(String tenantId, UUID employeeId, UUID dependantId, DependantUpsertRequest request);

    Mono<DependantView> findDependantById(String tenantId, UUID employeeId, UUID dependantId);

    Flux<DependantView> findDependantsByEmployeeId(String tenantId, UUID employeeId);

    Mono<DependantView> deactivateDependant(String tenantId, UUID employeeId, UUID dependantId, String actor);

    Mono<BeneficiaryView> createBeneficiary(String tenantId, UUID employeeId, BeneficiaryUpsertRequest request);

    Mono<BeneficiaryView> updateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request);

    Mono<BeneficiaryView> findBeneficiaryById(String tenantId, UUID employeeId, UUID beneficiaryId);

    Flux<BeneficiaryView> findBeneficiariesByEmployeeId(String tenantId, UUID employeeId);

    Mono<BeneficiaryView> deactivateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, String actor);

    Mono<java.math.BigDecimal> activeBeneficiaryAllocationTotal(String tenantId, UUID employeeId, UUID excludeBeneficiaryId);

    Mono<WorkforceDetailView> upsertWorkforceDetail(String tenantId, UUID employeeId, WorkforceDetailUpsertRequest request);

    Mono<WorkforceDetailView> findWorkforceDetailByEmployeeId(String tenantId, UUID employeeId);

    Mono<EmployeeDocumentView> createDocument(String tenantId, UUID employeeId, EmployeeDocumentUpsertRequest request);

    Mono<EmployeeDocumentView> updateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request);

    Mono<EmployeeDocumentView> updateDocumentFile(String tenantId, UUID employeeId, UUID employeeDocumentId, String fileName, String filePath, String fileType, long fileSize, String actor);

    Mono<EmployeeDocumentView> updateDocumentVerification(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request);

    Mono<EmployeeDocumentView> findDocumentById(String tenantId, UUID employeeId, UUID employeeDocumentId);

    Flux<EmployeeDocumentView> findDocumentsByEmployeeId(String tenantId, UUID employeeId, Boolean activeOnly);

    Mono<EmployeeDocumentView> deactivateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, String actor);

    Flux<EmployeeDocumentView> findDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to, Boolean activeOnly);

    Mono<Long> countDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to);

    Flux<EmployeeDocumentView> findExpiredDocuments(String tenantId, UUID employeeId, Boolean activeOnly);

    Mono<EmploymentHistoryView> createEmploymentHistory(String tenantId, UUID employeeId, EmploymentHistoryUpsertRequest request);

    Mono<EmploymentHistoryView> findEmploymentHistoryRecord(String tenantId, UUID employeeId, UUID employmentHistoryId);

    Flux<EmploymentHistoryView> findEmploymentHistoryByEmployeeId(String tenantId, UUID employeeId);

    Mono<DigitalOnboardingView> createOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status);

    Mono<DigitalOnboardingView> updateOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status, String rejectedReason);

    Mono<DigitalOnboardingView> findOnboardingByEmployeeId(String tenantId, UUID employeeId);

    Mono<PolicyAcknowledgementView> createPolicyAcknowledgement(String tenantId, UUID onboardingId, PolicyAcknowledgementRequest request);

    Flux<PolicyAcknowledgementView> findPolicyAcknowledgements(String tenantId, UUID onboardingId);
}
