package com.company.hrms.employee.service;

import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import java.util.UUID;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeInformationApi {

    Mono<EmployeeProfileView> createEmployee(EmployeeProfileUpsertRequest request);

    Mono<EmployeeProfileView> updateEmployee(UUID employeeId, EmployeeProfileUpsertRequest request);

    Mono<EmployeeProfileView> getEmployee(UUID employeeId);

    Mono<EmployeeProfileView> getEmployeeByCode(String employeeCode);

    Flux<EmployeeProfileView> searchEmployees(EmployeeSearchFilter filter);

    Mono<EmployeeProfileView> updateEmployeeStatus(UUID employeeId, EmployeeProfileStatusUpdateRequest request);

    Mono<EmployeeAddressView> addAddress(UUID employeeId, EmployeeAddressUpsertRequest request);

    Mono<EmployeeAddressView> updateAddress(UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request);

    Mono<EmployeeAddressView> getAddress(UUID employeeId, UUID addressId);

    Flux<EmployeeAddressView> listAddresses(UUID employeeId);

    Mono<EmployeeAddressView> deactivateAddress(UUID employeeId, UUID addressId, String actor);

    Mono<EmergencyContactView> addEmergencyContact(UUID employeeId, EmergencyContactUpsertRequest request);

    Mono<EmergencyContactView> updateEmergencyContact(UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request);

    Mono<EmergencyContactView> getEmergencyContact(UUID employeeId, UUID emergencyContactId);

    Flux<EmergencyContactView> listEmergencyContacts(UUID employeeId);

    Mono<EmergencyContactView> deactivateEmergencyContact(UUID employeeId, UUID emergencyContactId, String actor);

    Mono<DependantView> addDependant(UUID employeeId, DependantUpsertRequest request);

    Mono<DependantView> updateDependant(UUID employeeId, UUID dependantId, DependantUpsertRequest request);

    Mono<DependantView> getDependant(UUID employeeId, UUID dependantId);

    Flux<DependantView> listDependants(UUID employeeId);

    Mono<DependantView> deactivateDependant(UUID employeeId, UUID dependantId, String actor);

    Mono<BeneficiaryView> addBeneficiary(UUID employeeId, BeneficiaryUpsertRequest request);

    Mono<BeneficiaryView> updateBeneficiary(UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request);

    Mono<BeneficiaryView> getBeneficiary(UUID employeeId, UUID beneficiaryId);

    Flux<BeneficiaryView> listBeneficiaries(UUID employeeId);

    Mono<BeneficiaryView> deactivateBeneficiary(UUID employeeId, UUID beneficiaryId, String actor);

    Mono<WorkforceDetailView> saveWorkforceDetail(UUID employeeId, WorkforceDetailUpsertRequest request);

    Mono<WorkforceDetailView> getWorkforceDetail(UUID employeeId);

    Mono<EmployeeDocumentView> addDocument(UUID employeeId, EmployeeDocumentUpsertRequest request);

    Mono<EmployeeDocumentView> uploadDocument(UUID employeeId, FilePart file, EmployeeDocumentUpsertRequest request);

    Mono<EmployeeDocumentView> replaceDocumentFile(UUID employeeId, UUID employeeDocumentId, FilePart file, String actor);

    Mono<EmployeeDocumentView> updateDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request);

    Mono<EmployeeDocumentView> verifyOrRejectDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request);

    Mono<EmployeeDocumentView> getDocument(UUID employeeId, UUID employeeDocumentId);

    Flux<EmployeeDocumentView> listDocuments(UUID employeeId, Boolean activeOnly);

    Mono<EmployeeDocumentView> deactivateDocument(UUID employeeId, UUID employeeDocumentId, String actor);

    Flux<EmployeeDocumentView> getExpiringDocuments(ExpiryBucket bucket, UUID employeeId, Boolean activeOnly);

    Mono<ExpiryDashboardView> getExpiryDashboard(UUID employeeId);

    Flux<EmployeeDocumentView> getExpiredDocuments(UUID employeeId, Boolean activeOnly);

    Mono<EmploymentHistoryView> addEmploymentHistory(UUID employeeId, EmploymentHistoryUpsertRequest request);

    Mono<EmploymentHistoryView> getEmploymentHistoryRecord(UUID employeeId, UUID employmentHistoryId);

    Flux<EmploymentHistoryView> getEmploymentHistory(UUID employeeId);

    Mono<DigitalOnboardingView> createOnboarding(UUID employeeId, DigitalOnboardingUpsertRequest request);

    Mono<DigitalOnboardingView> updateOnboardingProgress(UUID employeeId, DigitalOnboardingUpsertRequest request);

    Mono<PolicyAcknowledgementView> acknowledgePolicy(UUID employeeId, PolicyAcknowledgementRequest request);

    Mono<DigitalOnboardingView> submitOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request);

    Mono<DigitalOnboardingView> reviewOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request);

    Mono<DigitalOnboardingView> approveOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request);

    Mono<DigitalOnboardingView> rejectOnboarding(UUID employeeId, DigitalOnboardingRejectRequest request);

    Mono<DigitalOnboardingView> completeOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request);

    Mono<DigitalOnboardingView> getOnboarding(UUID employeeId);
}
