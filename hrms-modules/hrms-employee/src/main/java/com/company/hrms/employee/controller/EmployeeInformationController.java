package com.company.hrms.employee.controller;

import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import com.company.hrms.employee.service.EmployeeInformationApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/employee-information")
public class EmployeeInformationController {

    private final EmployeeInformationApi employeeInformationApi;

    public EmployeeInformationController(EmployeeInformationApi employeeInformationApi) {
        this.employeeInformationApi = employeeInformationApi;
    }

    @PostMapping("/employees")
    @Operation(summary = "Create employee profile")
    public Mono<EmployeeProfileView> createEmployee(@Valid @RequestBody EmployeeProfileRequest request) {
        return employeeInformationApi.createEmployee(request.toCommand());
    }

    @PutMapping("/employees/{employeeId}")
    @Operation(summary = "Update employee profile")
    public Mono<EmployeeProfileView> updateEmployee(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmployeeProfileRequest request
    ) {
        return employeeInformationApi.updateEmployee(employeeId, request.toCommand());
    }

    @GetMapping("/employees/{employeeId}")
    @Operation(summary = "Get employee by id")
    public Mono<EmployeeProfileView> getEmployee(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.getEmployee(employeeId);
    }

    @GetMapping("/employees/code/{employeeCode}")
    @Operation(summary = "Get employee by employee code")
    public Mono<EmployeeProfileView> getEmployeeByCode(@PathVariable("employeeCode") String employeeCode) {
        return employeeInformationApi.getEmployeeByCode(employeeCode);
    }

    @GetMapping("/employees")
    @Operation(summary = "Search employees")
    public Flux<EmployeeProfileView> searchEmployees(
            @RequestParam(name = "employeeCode", required = false) String employeeCode,
            @RequestParam(name = "employeeName", required = false) String employeeName,
            @RequestParam(name = "officialEmail", required = false) String officialEmail,
            @RequestParam(name = "primaryMobileNumber", required = false) String primaryMobileNumber,
            @RequestParam(name = "workforceCategory", required = false) WorkforceCategory workforceCategory,
            @RequestParam(name = "employeeStatus", required = false) EmployeeStatus employeeStatus,
            @RequestParam(name = "joiningDateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joiningDateFrom,
            @RequestParam(name = "joiningDateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joiningDateTo,
            @RequestParam(name = "departmentId", required = false) UUID departmentId,
            @RequestParam(name = "designationId", required = false) UUID designationId,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return employeeInformationApi.searchEmployees(new EmployeeSearchFilter(
                employeeCode,
                employeeName,
                officialEmail,
                primaryMobileNumber,
                workforceCategory,
                employeeStatus,
                joiningDateFrom,
                joiningDateTo,
                departmentId,
                designationId,
                limit,
                offset));
    }

    @PatchMapping("/employees/{employeeId}/status")
    @Operation(summary = "Update employee status")
    public Mono<EmployeeProfileView> updateEmployeeStatus(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmployeeStatusUpdateRequest request
    ) {
        return employeeInformationApi.updateEmployeeStatus(employeeId, new EmployeeProfileStatusUpdateRequest(request.employeeStatus(), request.actor()));
    }

    @PostMapping("/employees/{employeeId}/addresses")
    @Operation(summary = "Add employee address")
    public Mono<EmployeeAddressView> addAddress(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmployeeAddressUpsertRequest request
    ) {
        return employeeInformationApi.addAddress(employeeId, request);
    }

    @PutMapping("/employees/{employeeId}/addresses/{addressId}")
    @Operation(summary = "Update employee address")
    public Mono<EmployeeAddressView> updateAddress(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("addressId") UUID addressId,
            @Valid @RequestBody EmployeeAddressUpsertRequest request
    ) {
        return employeeInformationApi.updateAddress(employeeId, addressId, request);
    }

    @GetMapping("/employees/{employeeId}/addresses/{addressId}")
    @Operation(summary = "Get employee address by id")
    public Mono<EmployeeAddressView> getAddress(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("addressId") UUID addressId
    ) {
        return employeeInformationApi.getAddress(employeeId, addressId);
    }

    @GetMapping("/employees/{employeeId}/addresses")
    @Operation(summary = "List employee addresses")
    public Flux<EmployeeAddressView> listAddresses(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.listAddresses(employeeId);
    }

    @DeleteMapping("/employees/{employeeId}/addresses/{addressId}")
    @Operation(summary = "Deactivate employee address")
    public Mono<EmployeeAddressView> deactivateAddress(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("addressId") UUID addressId,
            @RequestParam(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.deactivateAddress(employeeId, addressId, actor);
    }

    @PostMapping("/employees/{employeeId}/emergency-contacts")
    @Operation(summary = "Add emergency contact")
    public Mono<EmergencyContactView> addEmergencyContact(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmergencyContactUpsertRequest request
    ) {
        return employeeInformationApi.addEmergencyContact(employeeId, request);
    }

    @PutMapping("/employees/{employeeId}/emergency-contacts/{emergencyContactId}")
    @Operation(summary = "Update emergency contact")
    public Mono<EmergencyContactView> updateEmergencyContact(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("emergencyContactId") UUID emergencyContactId,
            @Valid @RequestBody EmergencyContactUpsertRequest request
    ) {
        return employeeInformationApi.updateEmergencyContact(employeeId, emergencyContactId, request);
    }

    @GetMapping("/employees/{employeeId}/emergency-contacts/{emergencyContactId}")
    @Operation(summary = "Get emergency contact by id")
    public Mono<EmergencyContactView> getEmergencyContact(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("emergencyContactId") UUID emergencyContactId
    ) {
        return employeeInformationApi.getEmergencyContact(employeeId, emergencyContactId);
    }

    @GetMapping("/employees/{employeeId}/emergency-contacts")
    @Operation(summary = "List emergency contacts")
    public Flux<EmergencyContactView> listEmergencyContacts(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.listEmergencyContacts(employeeId);
    }

    @DeleteMapping("/employees/{employeeId}/emergency-contacts/{emergencyContactId}")
    @Operation(summary = "Deactivate emergency contact")
    public Mono<EmergencyContactView> deactivateEmergencyContact(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("emergencyContactId") UUID emergencyContactId,
            @RequestParam(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.deactivateEmergencyContact(employeeId, emergencyContactId, actor);
    }

    @PostMapping("/employees/{employeeId}/dependants")
    @Operation(summary = "Add dependant")
    public Mono<DependantView> addDependant(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DependantUpsertRequest request
    ) {
        return employeeInformationApi.addDependant(employeeId, request);
    }

    @PutMapping("/employees/{employeeId}/dependants/{dependantId}")
    @Operation(summary = "Update dependant")
    public Mono<DependantView> updateDependant(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("dependantId") UUID dependantId,
            @Valid @RequestBody DependantUpsertRequest request
    ) {
        return employeeInformationApi.updateDependant(employeeId, dependantId, request);
    }

    @GetMapping("/employees/{employeeId}/dependants/{dependantId}")
    @Operation(summary = "Get dependant by id")
    public Mono<DependantView> getDependant(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("dependantId") UUID dependantId
    ) {
        return employeeInformationApi.getDependant(employeeId, dependantId);
    }

    @GetMapping("/employees/{employeeId}/dependants")
    @Operation(summary = "List dependants")
    public Flux<DependantView> listDependants(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.listDependants(employeeId);
    }

    @DeleteMapping("/employees/{employeeId}/dependants/{dependantId}")
    @Operation(summary = "Deactivate dependant")
    public Mono<DependantView> deactivateDependant(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("dependantId") UUID dependantId,
            @RequestParam(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.deactivateDependant(employeeId, dependantId, actor);
    }

    @PostMapping("/employees/{employeeId}/beneficiaries")
    @Operation(summary = "Add beneficiary")
    public Mono<BeneficiaryView> addBeneficiary(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody BeneficiaryUpsertRequest request
    ) {
        return employeeInformationApi.addBeneficiary(employeeId, request);
    }

    @PutMapping("/employees/{employeeId}/beneficiaries/{beneficiaryId}")
    @Operation(summary = "Update beneficiary")
    public Mono<BeneficiaryView> updateBeneficiary(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("beneficiaryId") UUID beneficiaryId,
            @Valid @RequestBody BeneficiaryUpsertRequest request
    ) {
        return employeeInformationApi.updateBeneficiary(employeeId, beneficiaryId, request);
    }

    @GetMapping("/employees/{employeeId}/beneficiaries/{beneficiaryId}")
    @Operation(summary = "Get beneficiary by id")
    public Mono<BeneficiaryView> getBeneficiary(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("beneficiaryId") UUID beneficiaryId
    ) {
        return employeeInformationApi.getBeneficiary(employeeId, beneficiaryId);
    }

    @GetMapping("/employees/{employeeId}/beneficiaries")
    @Operation(summary = "List beneficiaries")
    public Flux<BeneficiaryView> listBeneficiaries(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.listBeneficiaries(employeeId);
    }

    @DeleteMapping("/employees/{employeeId}/beneficiaries/{beneficiaryId}")
    @Operation(summary = "Deactivate beneficiary")
    public Mono<BeneficiaryView> deactivateBeneficiary(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("beneficiaryId") UUID beneficiaryId,
            @RequestParam(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.deactivateBeneficiary(employeeId, beneficiaryId, actor);
    }

    @PutMapping("/employees/{employeeId}/workforce-detail")
    @Operation(summary = "Save or update workforce detail")
    public Mono<WorkforceDetailView> saveWorkforceDetail(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody WorkforceDetailUpsertRequest request
    ) {
        return employeeInformationApi.saveWorkforceDetail(employeeId, request);
    }

    @GetMapping("/employees/{employeeId}/workforce-detail")
    @Operation(summary = "Get workforce detail")
    public Mono<WorkforceDetailView> getWorkforceDetail(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.getWorkforceDetail(employeeId);
    }

    @PostMapping("/employees/{employeeId}/documents")
    @Operation(summary = "Add employee document metadata")
    public Mono<EmployeeDocumentView> addDocument(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmployeeDocumentUpsertRequest request
    ) {
        return employeeInformationApi.addDocument(employeeId, request);
    }

    @PostMapping(value = "/employees/{employeeId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload employee document with metadata")
    public Mono<EmployeeDocumentView> uploadEmployeeDocument(
            @PathVariable("employeeId") UUID employeeId,
            @RequestPart("file") FilePart file,
            @RequestPart("documentType") EmployeeDocumentType documentType,
            @RequestPart("documentName") String documentName,
            @RequestPart(name = "documentNumber", required = false) String documentNumber,
            @RequestPart(name = "issuingCountryId", required = false) UUID issuingCountryId,
            @RequestPart(name = "issuingAuthority", required = false) String issuingAuthority,
            @RequestPart(name = "issueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestPart(name = "expiryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestPart(name = "alertEnabled", required = false) Boolean alertEnabled,
            @RequestPart(name = "remarks", required = false) String remarks,
            @RequestPart(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.uploadDocument(employeeId, file, new EmployeeDocumentUpsertRequest(
                documentType,
                documentName,
                documentNumber,
                issuingCountryId,
                issuingAuthority,
                issueDate,
                expiryDate,
                null,
                null,
                null,
                null,
                EmployeeDocumentVerificationStatus.PENDING,
                remarks,
                alertEnabled != null && alertEnabled,
                true,
                actor));
    }

    @PostMapping(value = "/employees/{employeeId}/documents/{employeeDocumentId}/replace-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Replace document file")
    public Mono<EmployeeDocumentView> replaceDocumentFile(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employeeDocumentId") UUID employeeDocumentId,
            @RequestPart("file") FilePart file,
            @RequestPart(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.replaceDocumentFile(employeeId, employeeDocumentId, file, actor);
    }

    @PutMapping("/employees/{employeeId}/documents/{employeeDocumentId}")
    @Operation(summary = "Update employee document metadata")
    public Mono<EmployeeDocumentView> updateDocument(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employeeDocumentId") UUID employeeDocumentId,
            @Valid @RequestBody EmployeeDocumentUpsertRequest request
    ) {
        return employeeInformationApi.updateDocument(employeeId, employeeDocumentId, request);
    }

    @PatchMapping("/employees/{employeeId}/documents/{employeeDocumentId}/verification")
    @Operation(summary = "Verify or reject employee document")
    public Mono<EmployeeDocumentView> verifyOrRejectDocument(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employeeDocumentId") UUID employeeDocumentId,
            @Valid @RequestBody EmployeeDocumentVerificationRequest request
    ) {
        return employeeInformationApi.verifyOrRejectDocument(employeeId, employeeDocumentId, request);
    }

    @GetMapping("/employees/{employeeId}/documents/{employeeDocumentId}")
    @Operation(summary = "Get document by id")
    public Mono<EmployeeDocumentView> getDocument(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employeeDocumentId") UUID employeeDocumentId
    ) {
        return employeeInformationApi.getDocument(employeeId, employeeDocumentId);
    }

    @GetMapping("/employees/{employeeId}/documents")
    @Operation(summary = "List employee documents")
    public Flux<EmployeeDocumentView> listDocuments(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        return employeeInformationApi.listDocuments(employeeId, activeOnly);
    }

    @DeleteMapping("/employees/{employeeId}/documents/{employeeDocumentId}")
    @Operation(summary = "Deactivate employee document")
    public Mono<EmployeeDocumentView> deactivateDocument(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employeeDocumentId") UUID employeeDocumentId,
            @RequestParam(name = "actor", required = false) String actor
    ) {
        return employeeInformationApi.deactivateDocument(employeeId, employeeDocumentId, actor);
    }

    @GetMapping("/employees/{employeeId}/documents/expiry/expiring")
    @Operation(summary = "Get expiring documents by bucket")
    public Flux<EmployeeDocumentView> getExpiringDocuments(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(name = "bucket") ExpiryBucket bucket,
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        return employeeInformationApi.getExpiringDocuments(bucket, employeeId, activeOnly);
    }

    @GetMapping("/employees/{employeeId}/documents/expiry/dashboard")
    @Operation(summary = "Get expiry dashboard counts")
    public Mono<ExpiryDashboardView> getExpiryDashboard(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.getExpiryDashboard(employeeId);
    }

    @GetMapping("/employees/{employeeId}/documents/expiry/expired")
    @Operation(summary = "Get all expired documents")
    public Flux<EmployeeDocumentView> getExpiredDocuments(
            @PathVariable("employeeId") UUID employeeId,
            @RequestParam(name = "activeOnly", required = false) Boolean activeOnly
    ) {
        return employeeInformationApi.getExpiredDocuments(employeeId, activeOnly);
    }

    @PostMapping("/employees/{employeeId}/employment-history")
    @Operation(summary = "Add employment history record")
    public Mono<EmploymentHistoryView> addEmploymentHistory(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody EmploymentHistoryUpsertRequest request
    ) {
        return employeeInformationApi.addEmploymentHistory(employeeId, request);
    }

    @GetMapping("/employees/{employeeId}/employment-history")
    @Operation(summary = "Get employment history timeline")
    public Flux<EmploymentHistoryView> getEmploymentHistory(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.getEmploymentHistory(employeeId);
    }

    @GetMapping("/employees/{employeeId}/employment-history/{employmentHistoryId}")
    @Operation(summary = "Get employment history record by id")
    public Mono<EmploymentHistoryView> getEmploymentHistoryRecord(
            @PathVariable("employeeId") UUID employeeId,
            @PathVariable("employmentHistoryId") UUID employmentHistoryId
    ) {
        return employeeInformationApi.getEmploymentHistoryRecord(employeeId, employmentHistoryId);
    }

    @PostMapping("/employees/{employeeId}/onboarding")
    @Operation(summary = "Create onboarding")
    public Mono<DigitalOnboardingView> createOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingUpsertRequest request
    ) {
        return employeeInformationApi.createOnboarding(employeeId, request);
    }

    @PutMapping("/employees/{employeeId}/onboarding/progress")
    @Operation(summary = "Update onboarding progress")
    public Mono<DigitalOnboardingView> updateOnboardingProgress(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingUpsertRequest request
    ) {
        return employeeInformationApi.updateOnboardingProgress(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/policy-ack")
    @Operation(summary = "Acknowledge policy")
    public Mono<PolicyAcknowledgementView> acknowledgePolicy(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody PolicyAcknowledgementRequest request
    ) {
        return employeeInformationApi.acknowledgePolicy(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/submit")
    @Operation(summary = "Submit onboarding")
    public Mono<DigitalOnboardingView> submitOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingReviewRequest request
    ) {
        return employeeInformationApi.submitOnboarding(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/review")
    @Operation(summary = "Review onboarding")
    public Mono<DigitalOnboardingView> reviewOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingReviewRequest request
    ) {
        return employeeInformationApi.reviewOnboarding(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/approve")
    @Operation(summary = "Approve onboarding")
    public Mono<DigitalOnboardingView> approveOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingReviewRequest request
    ) {
        return employeeInformationApi.approveOnboarding(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/reject")
    @Operation(summary = "Reject onboarding")
    public Mono<DigitalOnboardingView> rejectOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingRejectRequest request
    ) {
        return employeeInformationApi.rejectOnboarding(employeeId, request);
    }

    @PostMapping("/employees/{employeeId}/onboarding/complete")
    @Operation(summary = "Complete onboarding")
    public Mono<DigitalOnboardingView> completeOnboarding(
            @PathVariable("employeeId") UUID employeeId,
            @Valid @RequestBody DigitalOnboardingReviewRequest request
    ) {
        return employeeInformationApi.completeOnboarding(employeeId, request);
    }

    @GetMapping("/employees/{employeeId}/onboarding")
    @Operation(summary = "Get onboarding details")
    public Mono<DigitalOnboardingView> getOnboarding(@PathVariable("employeeId") UUID employeeId) {
        return employeeInformationApi.getOnboarding(employeeId);
    }

    public record EmployeeStatusUpdateRequest(
            @NotNull EmployeeStatus employeeStatus,
            String actor
    ) {
    }

    public record EmployeeProfileRequest(
            String employeeCode,
            @NotBlank String firstName,
            String middleName,
            @NotBlank String lastName,
            String fullName,
            String arabicName,
            String profilePhotoPath,
            UUID genderId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
            UUID maritalStatusId,
            UUID nationalityId,
            String bloodGroup,
            @Email String personalEmail,
            @NotBlank @Email String officialEmail,
            String primaryMobileNumber,
            String secondaryMobileNumber,
            String alternateContactNumber,
            @NotNull EmployeeStatus employeeStatus,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfJoining,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate confirmationDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate probationEndDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate retirementDate,
            @NotNull WorkforceCategory workforceCategory,
            UUID departmentId,
            UUID designationId,
            String jobTitle,
            UUID personId,
            @Schema(example = "hr.admin") String actor
    ) {
        EmployeeProfileUpsertRequest toCommand() {
            return new EmployeeProfileUpsertRequest(
                    employeeCode,
                    firstName,
                    middleName,
                    lastName,
                    fullName,
                    arabicName,
                    profilePhotoPath,
                    genderId,
                    dateOfBirth,
                    maritalStatusId,
                    nationalityId,
                    bloodGroup,
                    personalEmail,
                    officialEmail,
                    primaryMobileNumber,
                    secondaryMobileNumber,
                    alternateContactNumber,
                    employeeStatus,
                    dateOfJoining,
                    confirmationDate,
                    probationEndDate,
                    retirementDate,
                    workforceCategory,
                    departmentId,
                    designationId,
                    jobTitle,
                    personId,
                    actor);
        }
    }
}
