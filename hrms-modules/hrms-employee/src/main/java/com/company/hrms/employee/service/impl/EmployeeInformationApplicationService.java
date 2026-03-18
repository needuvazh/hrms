package com.company.hrms.employee.service.impl;

import com.company.hrms.document.model.DocumentStorageAdapter;
import com.company.hrms.document.model.StorageRegistrationRequestDto;
import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import com.company.hrms.employee.repository.EmployeeInformationRepository;
import com.company.hrms.employee.service.EmployeeInformationApi;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeInformationApplicationService implements EmployeeInformationApi {

    private static final Set<String> ALLOWED_DOC_TYPES = Set.of("application/pdf", "image/png", "image/jpg", "image/jpeg");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final EmployeeInformationRepository repository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final DocumentStorageAdapter documentStorageAdapter;

    public EmployeeInformationApplicationService(
            EmployeeInformationRepository repository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            DocumentStorageAdapter documentStorageAdapter
    ) {
        this.repository = repository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.documentStorageAdapter = documentStorageAdapter;
    }

    @Override
    public Mono<EmployeeProfileView> createEmployee(EmployeeProfileUpsertRequest request) {
        validateEmployeeUpsert(request, null);
        return enablementGuard.requireModuleEnabled("employee")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Mono<String> codeMono = StringUtils.hasText(request.employeeCode())
                            ? Mono.just(request.employeeCode().trim())
                            : repository.nextEmployeeCodeSequence().map(this::formatEmployeeCode);
                    return codeMono.flatMap(code -> repository.existsOfficialEmail(tenantId, request.officialEmail(), null)
                                    .flatMap(exists -> exists
                                            ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "OFFICIAL_EMAIL_EXISTS", "Official email already exists"))
                                            : Mono.empty())
                                    .then(repository.createEmployee(tenantId, request, code)));
                });
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployee(UUID employeeId, EmployeeProfileUpsertRequest request) {
        validateEmployeeUpsert(request, employeeId);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.existsOfficialEmail(tenantId, request.officialEmail(), employeeId)
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "OFFICIAL_EMAIL_EXISTS", "Official email already exists"))
                                : Mono.empty()))
                .then(repository.updateEmployee(tenantId, employeeId, request)));
    }

    @Override
    public Mono<EmployeeProfileView> getEmployee(UUID employeeId) {
        return requireTenant().flatMap(tenantId -> repository.findEmployeeById(tenantId, employeeId)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found"))));
    }

    @Override
    public Mono<EmployeeProfileView> getEmployeeByCode(String employeeCode) {
        return requireTenant().flatMap(tenantId -> repository.findEmployeeByCode(tenantId, employeeCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found"))));
    }

    @Override
    public Flux<EmployeeProfileView> searchEmployees(EmployeeSearchFilter filter) {
        EmployeeSearchFilter normalized = new EmployeeSearchFilter(
                filter.employeeCode(),
                filter.employeeName(),
                filter.officialEmail(),
                filter.primaryMobileNumber(),
                filter.workforceCategory(),
                filter.employeeStatus(),
                filter.joiningDateFrom(),
                filter.joiningDateTo(),
                filter.departmentId(),
                filter.designationId(),
                filter.limit() > 0 ? filter.limit() : 50,
                Math.max(filter.offset(), 0));
        return requireTenant().flatMapMany(tenantId -> repository.searchEmployees(tenantId, normalized));
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployeeStatus(UUID employeeId, EmployeeProfileStatusUpdateRequest request) {
        if (request.employeeStatus() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_STATUS_REQUIRED", "Employee status is required"));
        }
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.updateEmployeeStatus(tenantId, employeeId, request.employeeStatus(), request.actor())));
    }

    @Override public Mono<EmployeeAddressView> addAddress(UUID employeeId, EmployeeAddressUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmployeeAddressView> updateAddress(UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmployeeAddressView> getAddress(UUID employeeId, UUID addressId) { return Mono.error(notImplemented()); }
    @Override public Flux<EmployeeAddressView> listAddresses(UUID employeeId) { return Flux.error(notImplemented()); }
    @Override public Mono<EmployeeAddressView> deactivateAddress(UUID employeeId, UUID addressId, String actor) { return Mono.error(notImplemented()); }
    @Override public Mono<EmergencyContactView> addEmergencyContact(UUID employeeId, EmergencyContactUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmergencyContactView> updateEmergencyContact(UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmergencyContactView> getEmergencyContact(UUID employeeId, UUID emergencyContactId) { return Mono.error(notImplemented()); }
    @Override public Flux<EmergencyContactView> listEmergencyContacts(UUID employeeId) { return Flux.error(notImplemented()); }
    @Override public Mono<EmergencyContactView> deactivateEmergencyContact(UUID employeeId, UUID emergencyContactId, String actor) { return Mono.error(notImplemented()); }
    @Override public Mono<DependantView> addDependant(UUID employeeId, DependantUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DependantView> updateDependant(UUID employeeId, UUID dependantId, DependantUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DependantView> getDependant(UUID employeeId, UUID dependantId) { return Mono.error(notImplemented()); }
    @Override public Flux<DependantView> listDependants(UUID employeeId) { return Flux.error(notImplemented()); }
    @Override public Mono<DependantView> deactivateDependant(UUID employeeId, UUID dependantId, String actor) { return Mono.error(notImplemented()); }
    @Override public Mono<BeneficiaryView> addBeneficiary(UUID employeeId, BeneficiaryUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<BeneficiaryView> updateBeneficiary(UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<BeneficiaryView> getBeneficiary(UUID employeeId, UUID beneficiaryId) { return Mono.error(notImplemented()); }
    @Override public Flux<BeneficiaryView> listBeneficiaries(UUID employeeId) { return Flux.error(notImplemented()); }
    @Override public Mono<BeneficiaryView> deactivateBeneficiary(UUID employeeId, UUID beneficiaryId, String actor) { return Mono.error(notImplemented()); }
    @Override public Mono<WorkforceDetailView> saveWorkforceDetail(UUID employeeId, WorkforceDetailUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<WorkforceDetailView> getWorkforceDetail(UUID employeeId) { return Mono.error(notImplemented()); }
    @Override
    public Mono<EmployeeDocumentView> addDocument(UUID employeeId, EmployeeDocumentUpsertRequest request) {
        if (request.documentType() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_TYPE_REQUIRED", "Document type is required"));
        }
        if (!StringUtils.hasText(request.documentName())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DOCUMENT_NAME_REQUIRED", "Document name is required"));
        }
        if (request.issueDate() != null && request.expiryDate() != null && request.issueDate().isAfter(request.expiryDate())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ISSUE_EXPIRY_INVALID", "Issue date must be before or equal to expiry date"));
        }
        if (request.alertEnabled() && request.expiryDate() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "EXPIRY_REQUIRED_FOR_ALERT", "Expiry date is required when alert is enabled"));
        }
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.createDocument(tenantId, employeeId, request)));
    }

    @Override
    public Mono<EmployeeDocumentView> uploadDocument(UUID employeeId, FilePart file, EmployeeDocumentUpsertRequest request) {
        if (file == null || !StringUtils.hasText(file.filename())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FILE_REQUIRED", "File is required"));
        }
        String contentType = file.headers().getContentType() == null ? "" : file.headers().getContentType().toString();
        if (!ALLOWED_DOC_TYPES.contains(contentType.toLowerCase())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FILE_TYPE_NOT_ALLOWED", "Allowed file types: pdf, png, jpg, jpeg"));
        }
        long size = file.headers().getContentLength();
        if (size > MAX_FILE_SIZE_BYTES) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "File exceeds max allowed size"));
        }

        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(documentStorageAdapter.registerUploadMetadata(new StorageRegistrationRequestDto(
                        tenantId,
                        sanitizeFileName(file.filename()),
                        contentType,
                        size,
                        "employee/" + employeeId + "/" + sanitizeFileName(file.filename()),
                        null)))
                .flatMap(storage -> addDocument(employeeId, new EmployeeDocumentUpsertRequest(
                        request.documentType(),
                        request.documentName(),
                        request.documentNumber(),
                        request.issuingCountryId(),
                        request.issuingAuthority(),
                        request.issueDate(),
                        request.expiryDate(),
                        sanitizeFileName(file.filename()),
                        storage.objectKey(),
                        storage.contentType(),
                        storage.sizeBytes(),
                        request.verificationStatus(),
                        request.remarks(),
                        request.alertEnabled(),
                        request.active(),
                        request.actor()))));
    }

    @Override public Mono<EmployeeDocumentView> replaceDocumentFile(UUID employeeId, UUID employeeDocumentId, FilePart file, String actor) { return Mono.error(notImplemented()); }
    @Override public Mono<EmployeeDocumentView> updateDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmployeeDocumentView> verifyOrRejectDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmployeeDocumentView> getDocument(UUID employeeId, UUID employeeDocumentId) { return Mono.error(notImplemented()); }
    @Override public Flux<EmployeeDocumentView> listDocuments(UUID employeeId, Boolean activeOnly) { return Flux.error(notImplemented()); }
    @Override public Mono<EmployeeDocumentView> deactivateDocument(UUID employeeId, UUID employeeDocumentId, String actor) { return Mono.error(notImplemented()); }
    @Override public Flux<EmployeeDocumentView> getExpiringDocuments(ExpiryBucket bucket, UUID employeeId, Boolean activeOnly) { return Flux.error(notImplemented()); }
    @Override public Mono<ExpiryDashboardView> getExpiryDashboard(UUID employeeId) { return Mono.error(notImplemented()); }
    @Override public Flux<EmployeeDocumentView> getExpiredDocuments(UUID employeeId, Boolean activeOnly) { return Flux.error(notImplemented()); }
    @Override public Mono<EmploymentHistoryView> addEmploymentHistory(UUID employeeId, EmploymentHistoryUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<EmploymentHistoryView> getEmploymentHistoryRecord(UUID employeeId, UUID employmentHistoryId) { return Mono.error(notImplemented()); }
    @Override public Flux<EmploymentHistoryView> getEmploymentHistory(UUID employeeId) { return Flux.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> createOnboarding(UUID employeeId, DigitalOnboardingUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> updateOnboardingProgress(UUID employeeId, DigitalOnboardingUpsertRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<PolicyAcknowledgementView> acknowledgePolicy(UUID employeeId, PolicyAcknowledgementRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> submitOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> reviewOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> approveOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> rejectOnboarding(UUID employeeId, DigitalOnboardingRejectRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> completeOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) { return Mono.error(notImplemented()); }
    @Override public Mono<DigitalOnboardingView> getOnboarding(UUID employeeId) { return Mono.error(notImplemented()); }

    private String formatEmployeeCode(Long sequence) {
        return "EMP%05d".formatted(sequence);
    }

    private void validateEmployeeUpsert(EmployeeProfileUpsertRequest request, UUID employeeId) {
        if (!StringUtils.hasText(request.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(request.lastName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LAST_NAME_REQUIRED", "Last name is required");
        }
        if (!StringUtils.hasText(request.officialEmail())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "OFFICIAL_EMAIL_REQUIRED", "Official email is required");
        }
        if (request.dateOfBirth() != null && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DOB_INVALID", "Date of birth cannot be in future");
        }
        if (request.dateOfJoining() != null && request.confirmationDate() != null && request.confirmationDate().isBefore(request.dateOfJoining())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CONFIRMATION_DATE_INVALID", "Confirmation date must be on or after joining date");
        }
        if (request.dateOfJoining() != null && request.probationEndDate() != null && request.probationEndDate().isBefore(request.dateOfJoining())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PROBATION_DATE_INVALID", "Probation end date must be on or after joining date");
        }
        if (request.workforceCategory() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WORKFORCE_CATEGORY_REQUIRED", "Workforce category is required");
        }
        if (request.employeeStatus() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_STATUS_REQUIRED", "Employee status is required");
        }
    }

    private Mono<Void> ensureEmployee(String tenantId, UUID employeeId) {
        return repository.existsEmployeeById(tenantId, employeeId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "Employee not found")));
    }

    private String sanitizeFileName(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private HrmsException notImplemented() {
        return new HrmsException(HttpStatus.NOT_IMPLEMENTED, "PENDING_IMPLEMENTATION", "This endpoint is scaffolded and will be completed in next iteration");
    }
}
