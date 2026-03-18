package com.company.hrms.employee.service.impl;

import com.company.hrms.document.model.DocumentStorageAdapter;
import com.company.hrms.document.model.StorageRegistrationRequestDto;
import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import com.company.hrms.employee.repository.EmployeeInformationRepository;
import com.company.hrms.employee.service.EmployeeInformationApi;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.math.BigDecimal;
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
        EmployeeProfileUpsertRequest normalizedRequest = withDefaultEmployeeStatus(request);
        validateEmployeeUpsert(normalizedRequest, null);
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
                                    .then(repository.createEmployee(tenantId, normalizedRequest, code)));
                });
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployee(UUID employeeId, EmployeeProfileUpsertRequest request) {
        EmployeeProfileUpsertRequest normalizedRequest = withDefaultEmployeeStatus(request);
        validateEmployeeUpsert(normalizedRequest, employeeId);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.existsOfficialEmail(tenantId, request.officialEmail(), employeeId)
                        .flatMap(exists -> exists
                                ? Mono.error(new HrmsException(HttpStatus.CONFLICT, "OFFICIAL_EMAIL_EXISTS", "Official email already exists"))
                                : Mono.empty()))
                .then(repository.updateEmployee(tenantId, employeeId, normalizedRequest)));
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

    @Override
    public Mono<EmployeeAddressView> addAddress(UUID employeeId, EmployeeAddressUpsertRequest request) {
        return requireTenant()
                .flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                        .then(resolveCountryId(request))
                        .flatMap(countryId -> {
                            validateAddress(request);
                            EmployeeAddressUpsertRequest normalized = normalizeAddressRequest(request, countryId);
                            Mono<Void> clearPrimary = normalized.primary()
                                    ? repository.clearPrimaryAddressByType(tenantId, employeeId, normalized.addressType(), null)
                                    : Mono.empty();
                            return clearPrimary.then(repository.createAddress(tenantId, employeeId, normalized));
                        }));
    }

    @Override
    public Mono<EmployeeAddressView> updateAddress(UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request) {
        return requireTenant()
                .flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                        .then(resolveCountryId(request))
                        .flatMap(countryId -> {
                            validateAddress(request);
                            EmployeeAddressUpsertRequest normalized = normalizeAddressRequest(request, countryId);
                            Mono<Void> clearPrimary = normalized.primary()
                                    ? repository.clearPrimaryAddressByType(tenantId, employeeId, normalized.addressType(), addressId)
                                    : Mono.empty();
                            return clearPrimary.then(repository.updateAddress(tenantId, employeeId, addressId, normalized));
                        }));
    }

    @Override
    public Mono<EmployeeAddressView> getAddress(UUID employeeId, UUID addressId) {
        return requireTenant()
                .flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                        .then(repository.findAddressById(tenantId, employeeId, addressId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found")))));
    }

    @Override
    public Flux<EmployeeAddressView> listAddresses(UUID employeeId) {
        return requireTenant()
                .flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                        .thenMany(repository.findAddressesByEmployeeId(tenantId, employeeId)));
    }

    @Override
    public Mono<EmployeeAddressView> deactivateAddress(UUID employeeId, UUID addressId, String actor) {
        return requireTenant()
                .flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                        .then(repository.deactivateAddress(tenantId, employeeId, addressId, actor)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND", "Address not found")))));
    }
    @Override
    public Mono<EmergencyContactView> addEmergencyContact(UUID employeeId, EmergencyContactUpsertRequest request) {
        validateEmergencyContact(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(ensureCountryOptional(request.countryId()))
                .then((request.primary() ? repository.clearPrimaryEmergencyContact(tenantId, employeeId, null) : Mono.empty()))
                .then(repository.createEmergencyContact(tenantId, employeeId, request)));
    }

    @Override
    public Mono<EmergencyContactView> updateEmergencyContact(UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request) {
        validateEmergencyContact(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(ensureCountryOptional(request.countryId()))
                .then((request.primary() ? repository.clearPrimaryEmergencyContact(tenantId, employeeId, emergencyContactId) : Mono.empty()))
                .then(repository.updateEmergencyContact(tenantId, employeeId, emergencyContactId, request)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMERGENCY_CONTACT_NOT_FOUND", "Emergency contact not found")))));
    }

    @Override
    public Mono<EmergencyContactView> getEmergencyContact(UUID employeeId, UUID emergencyContactId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findEmergencyContactById(tenantId, employeeId, emergencyContactId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMERGENCY_CONTACT_NOT_FOUND", "Emergency contact not found")))));
    }

    @Override
    public Flux<EmergencyContactView> listEmergencyContacts(UUID employeeId) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findEmergencyContactsByEmployeeId(tenantId, employeeId)));
    }

    @Override
    public Mono<EmergencyContactView> deactivateEmergencyContact(UUID employeeId, UUID emergencyContactId, String actor) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.deactivateEmergencyContact(tenantId, employeeId, emergencyContactId, actor)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMERGENCY_CONTACT_NOT_FOUND", "Emergency contact not found")))));
    }

    @Override
    public Mono<DependantView> addDependant(UUID employeeId, DependantUpsertRequest request) {
        validateDependant(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(ensureGenderOptional(request.genderId()))
                .then(ensureNationalityOptional(request.nationalityId()))
                .then(repository.createDependant(tenantId, employeeId, request)));
    }

    @Override
    public Mono<DependantView> updateDependant(UUID employeeId, UUID dependantId, DependantUpsertRequest request) {
        validateDependant(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(ensureGenderOptional(request.genderId()))
                .then(ensureNationalityOptional(request.nationalityId()))
                .then(repository.updateDependant(tenantId, employeeId, dependantId, request)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DEPENDANT_NOT_FOUND", "Dependant not found")))));
    }

    @Override
    public Mono<DependantView> getDependant(UUID employeeId, UUID dependantId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findDependantById(tenantId, employeeId, dependantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DEPENDANT_NOT_FOUND", "Dependant not found")))));
    }

    @Override
    public Flux<DependantView> listDependants(UUID employeeId) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findDependantsByEmployeeId(tenantId, employeeId)));
    }

    @Override
    public Mono<DependantView> deactivateDependant(UUID employeeId, UUID dependantId, String actor) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.deactivateDependant(tenantId, employeeId, dependantId, actor)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DEPENDANT_NOT_FOUND", "Dependant not found")))));
    }

    @Override
    public Mono<BeneficiaryView> addBeneficiary(UUID employeeId, BeneficiaryUpsertRequest request) {
        validateBeneficiary(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(validateBeneficiaryAllocation(tenantId, employeeId, request.allocationPercentage(), null))
                .then(repository.createBeneficiary(tenantId, employeeId, request)));
    }

    @Override
    public Mono<BeneficiaryView> updateBeneficiary(UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request) {
        validateBeneficiary(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(ensureRelationshipType(request.relationshipTypeId()))
                .then(validateBeneficiaryAllocation(tenantId, employeeId, request.allocationPercentage(), beneficiaryId))
                .then(repository.updateBeneficiary(tenantId, employeeId, beneficiaryId, request)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND", "Beneficiary not found")))));
    }

    @Override
    public Mono<BeneficiaryView> getBeneficiary(UUID employeeId, UUID beneficiaryId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findBeneficiaryById(tenantId, employeeId, beneficiaryId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND", "Beneficiary not found")))));
    }

    @Override
    public Flux<BeneficiaryView> listBeneficiaries(UUID employeeId) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findBeneficiariesByEmployeeId(tenantId, employeeId)));
    }

    @Override
    public Mono<BeneficiaryView> deactivateBeneficiary(UUID employeeId, UUID beneficiaryId, String actor) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.deactivateBeneficiary(tenantId, employeeId, beneficiaryId, actor)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND", "Beneficiary not found")))));
    }

    @Override
    public Mono<WorkforceDetailView> saveWorkforceDetail(UUID employeeId, WorkforceDetailUpsertRequest request) {
        validateWorkforce(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.upsertWorkforceDetail(tenantId, employeeId, request)));
    }

    @Override
    public Mono<WorkforceDetailView> getWorkforceDetail(UUID employeeId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findWorkforceDetailByEmployeeId(tenantId, employeeId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "WORKFORCE_DETAIL_NOT_FOUND", "Workforce detail not found")))));
    }
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

    @Override
    public Mono<EmployeeDocumentView> replaceDocumentFile(UUID employeeId, UUID employeeDocumentId, FilePart file, String actor) {
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
                .flatMap(storage -> repository.updateDocumentFile(
                        tenantId,
                        employeeId,
                        employeeDocumentId,
                        sanitizeFileName(file.filename()),
                        storage.objectKey(),
                        storage.contentType(),
                        storage.sizeBytes(),
                        actor))
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Document not found"))));
    }

    @Override
    public Mono<EmployeeDocumentView> updateDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request) {
        return validateDocumentRequest(request)
                .then(requireTenant())
                .flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                        .then(repository.updateDocument(tenantId, employeeId, employeeDocumentId, request)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Document not found")))));
    }

    @Override
    public Mono<EmployeeDocumentView> verifyOrRejectDocument(UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request) {
        if (request.verificationStatus() == null) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "VERIFICATION_STATUS_REQUIRED", "Verification status is required"));
        }
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.updateDocumentVerification(tenantId, employeeId, employeeDocumentId, request)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Document not found")))));
    }

    @Override
    public Mono<EmployeeDocumentView> getDocument(UUID employeeId, UUID employeeDocumentId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findDocumentById(tenantId, employeeId, employeeDocumentId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Document not found")))));
    }

    @Override
    public Flux<EmployeeDocumentView> listDocuments(UUID employeeId, Boolean activeOnly) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findDocumentsByEmployeeId(tenantId, employeeId, activeOnly)));
    }

    @Override
    public Mono<EmployeeDocumentView> deactivateDocument(UUID employeeId, UUID employeeDocumentId, String actor) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.deactivateDocument(tenantId, employeeId, employeeDocumentId, actor)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Document not found")))));
    }

    @Override
    public Flux<EmployeeDocumentView> getExpiringDocuments(ExpiryBucket bucket, UUID employeeId, Boolean activeOnly) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = switch (bucket) {
            case DAYS_7 -> today.plusDays(7);
            case DAYS_30 -> today.plusDays(30);
            case DAYS_60 -> today.plusDays(60);
            case DAYS_90 -> today.plusDays(90);
            case EXPIRED -> today.minusDays(1);
        };
        if (bucket == ExpiryBucket.EXPIRED) {
            return getExpiredDocuments(employeeId, activeOnly);
        }
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findDocumentsByExpiryRange(tenantId, employeeId, today, toDate, activeOnly)));
    }

    @Override
    public Mono<ExpiryDashboardView> getExpiryDashboard(UUID employeeId) {
        LocalDate today = LocalDate.now();
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(Mono.zip(
                        repository.findExpiredDocuments(tenantId, employeeId, true).count(),
                        repository.countDocumentsByExpiryRange(tenantId, employeeId, today, today.plusDays(7)),
                        repository.countDocumentsByExpiryRange(tenantId, employeeId, today, today.plusDays(30)),
                        repository.countDocumentsByExpiryRange(tenantId, employeeId, today, today.plusDays(60)),
                        repository.countDocumentsByExpiryRange(tenantId, employeeId, today, today.plusDays(90)))
                        .map(tuple -> new ExpiryDashboardView(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()))));
    }

    @Override
    public Flux<EmployeeDocumentView> getExpiredDocuments(UUID employeeId, Boolean activeOnly) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findExpiredDocuments(tenantId, employeeId, activeOnly)));
    }

    @Override
    public Mono<EmploymentHistoryView> addEmploymentHistory(UUID employeeId, EmploymentHistoryUpsertRequest request) {
        validateEmploymentHistory(request);
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.createEmploymentHistory(tenantId, employeeId, request)));
    }

    @Override
    public Mono<EmploymentHistoryView> getEmploymentHistoryRecord(UUID employeeId, UUID employmentHistoryId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findEmploymentHistoryRecord(tenantId, employeeId, employmentHistoryId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "EMPLOYMENT_HISTORY_NOT_FOUND", "Employment history not found")))));
    }

    @Override
    public Flux<EmploymentHistoryView> getEmploymentHistory(UUID employeeId) {
        return requireTenant().flatMapMany(tenantId -> ensureEmployee(tenantId, employeeId)
                .thenMany(repository.findEmploymentHistoryByEmployeeId(tenantId, employeeId)));
    }

    @Override
    public Mono<DigitalOnboardingView> createOnboarding(UUID employeeId, DigitalOnboardingUpsertRequest request) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(repository.findOnboardingByEmployeeId(tenantId, employeeId))
                .flatMap(existing -> Mono.<DigitalOnboardingView>error(new HrmsException(HttpStatus.CONFLICT, "ONBOARDING_EXISTS", "Onboarding already exists for employee")))
                .switchIfEmpty(repository.createOnboarding(tenantId, employeeId, request, DigitalOnboardingStatus.DRAFT)));
    }

    @Override
    public Mono<DigitalOnboardingView> updateOnboardingProgress(UUID employeeId, DigitalOnboardingUpsertRequest request) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(getOnboardingOrError(tenantId, employeeId))
                .flatMap(current -> repository.updateOnboarding(tenantId, employeeId, request, current.onboardingStatus(), current.rejectedReason())));
    }

    @Override
    public Mono<PolicyAcknowledgementView> acknowledgePolicy(UUID employeeId, PolicyAcknowledgementRequest request) {
        if (!request.acceptedFlag()) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "POLICY_ACCEPT_REQUIRED", "Policy acknowledgement requires acceptedFlag=true"));
        }
        if (!StringUtils.hasText(request.policyCode()) || !StringUtils.hasText(request.policyVersion())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "POLICY_DETAILS_REQUIRED", "policyCode and policyVersion are required"));
        }
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(getOnboardingOrError(tenantId, employeeId))
                .flatMap(onboarding -> repository.createPolicyAcknowledgement(tenantId, onboarding.onboardingId(), request)));
    }

    @Override
    public Mono<DigitalOnboardingView> submitOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) {
        return transitionOnboarding(employeeId, request.actor(), request.remarks(), DigitalOnboardingStatus.SUBMITTED, null);
    }

    @Override
    public Mono<DigitalOnboardingView> reviewOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) {
        return transitionOnboarding(employeeId, request.actor(), request.remarks(), DigitalOnboardingStatus.UNDER_REVIEW, null);
    }

    @Override
    public Mono<DigitalOnboardingView> approveOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) {
        return transitionOnboarding(employeeId, request.actor(), request.remarks(), DigitalOnboardingStatus.APPROVED, null);
    }

    @Override
    public Mono<DigitalOnboardingView> rejectOnboarding(UUID employeeId, DigitalOnboardingRejectRequest request) {
        if (!StringUtils.hasText(request.rejectedReason())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "REJECT_REASON_REQUIRED", "Rejected reason is required"));
        }
        return transitionOnboarding(employeeId, request.actor(), request.remarks(), DigitalOnboardingStatus.REJECTED, request.rejectedReason());
    }

    @Override
    public Mono<DigitalOnboardingView> completeOnboarding(UUID employeeId, DigitalOnboardingReviewRequest request) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(getOnboardingOrError(tenantId, employeeId))
                .flatMap(current -> {
                    if (!current.eFormCompleted() || !current.documentUploadCompleted() || !current.policyAcknowledged()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ONBOARDING_INCOMPLETE", "All onboarding steps must be completed before COMPLETED"));
                    }
                    DigitalOnboardingUpsertRequest cmd = new DigitalOnboardingUpsertRequest(
                            current.eFormCompleted(),
                            current.documentUploadCompleted(),
                            current.policyAcknowledged(),
                            request.remarks(),
                            request.actor());
                    return repository.updateOnboarding(tenantId, employeeId, cmd, DigitalOnboardingStatus.COMPLETED, null);
                }));
    }

    @Override
    public Mono<DigitalOnboardingView> getOnboarding(UUID employeeId) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(getOnboardingOrError(tenantId, employeeId)));
    }

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
    }

    private EmployeeProfileUpsertRequest withDefaultEmployeeStatus(EmployeeProfileUpsertRequest request) {
        if (request.employeeStatus() != null) {
            return request;
        }
        return new EmployeeProfileUpsertRequest(
                request.employeeCode(),
                request.firstName(),
                request.middleName(),
                request.lastName(),
                request.fullName(),
                request.arabicName(),
                request.profilePhotoPath(),
                request.genderId(),
                request.dateOfBirth(),
                request.maritalStatusId(),
                request.nationalityId(),
                request.bloodGroup(),
                request.personalEmail(),
                request.officialEmail(),
                request.primaryMobileNumber(),
                request.secondaryMobileNumber(),
                request.alternateContactNumber(),
                EmployeeStatus.ACTIVE,
                request.dateOfJoining(),
                request.confirmationDate(),
                request.probationEndDate(),
                request.retirementDate(),
                request.workforceCategory(),
                request.departmentId(),
                request.designationId(),
                request.jobTitle(),
                request.personId(),
                request.actor());
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

    private void validateAddress(EmployeeAddressUpsertRequest request) {
        if (request.addressType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ADDRESS_TYPE_REQUIRED", "Address type is required");
        }
        if (!StringUtils.hasText(request.city())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CITY_REQUIRED", "City is required");
        }
    }

    private Mono<UUID> resolveCountryId(EmployeeAddressUpsertRequest request) {
        if (request.countryId() != null) {
            return repository.existsCountry(request.countryId())
                    .flatMap(exists -> exists
                            ? Mono.just(request.countryId())
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_NOT_FOUND", "Invalid countryId")));
        }
        if (StringUtils.hasText(request.countryCode())) {
            return repository.findCountryIdByCode(request.countryCode().trim())
                    .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_NOT_FOUND", "Invalid countryCode")));
        }
        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_REQUIRED", "countryId or countryCode is required"));
    }

    private EmployeeAddressUpsertRequest normalizeAddressRequest(EmployeeAddressUpsertRequest request, UUID resolvedCountryId) {
        String stateProvince = StringUtils.hasText(request.stateCode())
                ? request.stateCode().trim()
                : request.stateProvince();
        return new EmployeeAddressUpsertRequest(
                request.addressType(),
                request.flatVillaNumber(),
                request.buildingName(),
                request.street(),
                request.area(),
                request.city(),
                stateProvince,
                request.stateCode(),
                resolvedCountryId,
                request.countryCode(),
                request.postalCode(),
                request.poBox(),
                request.landmark(),
                request.primary(),
                request.active(),
                request.actor());
    }

    private void validateEmergencyContact(EmergencyContactUpsertRequest request) {
        if (!StringUtils.hasText(request.name())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMERGENCY_CONTACT_NAME_REQUIRED", "Emergency contact name is required");
        }
        if (request.relationshipTypeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RELATIONSHIP_TYPE_REQUIRED", "Relationship type is required");
        }
        if (!StringUtils.hasText(request.primaryMobileNumber())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PRIMARY_MOBILE_REQUIRED", "Primary mobile number is required");
        }
    }

    private void validateDependant(DependantUpsertRequest request) {
        if (!StringUtils.hasText(request.fullName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DEPENDANT_NAME_REQUIRED", "Dependant fullName is required");
        }
        if (request.relationshipTypeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RELATIONSHIP_TYPE_REQUIRED", "Relationship type is required");
        }
        if (request.dateOfBirth() != null && request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DOB_INVALID", "Date of birth cannot be in future");
        }
        if (request.effectiveFrom() != null && request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EFFECTIVE_DATE_INVALID", "effectiveTo must be on or after effectiveFrom");
        }
    }

    private void validateBeneficiary(BeneficiaryUpsertRequest request) {
        if (!StringUtils.hasText(request.fullName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "BENEFICIARY_NAME_REQUIRED", "Beneficiary fullName is required");
        }
        if (request.relationshipTypeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RELATIONSHIP_TYPE_REQUIRED", "Relationship type is required");
        }
        if (request.allocationPercentage() == null || request.allocationPercentage().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ALLOCATION_INVALID", "Allocation percentage must be greater than 0");
        }
    }

    private void validateWorkforce(WorkforceDetailUpsertRequest request) {
        if (request.workforceCategory() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "WORKFORCE_CATEGORY_REQUIRED", "Workforce category is required");
        }
        if (request.workforceCategory() == WorkforceCategory.OMANI_NATIONAL && !StringUtils.hasText(request.pasiNumber())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PASI_REQUIRED", "pasiNumber is required for OMANI_NATIONAL");
        }
        if (request.workforceCategory() == WorkforceCategory.EXPATRIATE && !StringUtils.hasText(request.permitNumber())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERMIT_REQUIRED", "permitNumber is required for EXPATRIATE");
        }
    }

    private Mono<Void> validateDocumentRequest(EmployeeDocumentUpsertRequest request) {
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
        return Mono.empty();
    }

    private Mono<Void> ensureRelationshipType(UUID relationshipTypeId) {
        return repository.existsRelationshipType(relationshipTypeId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "RELATIONSHIP_TYPE_NOT_FOUND", "Invalid relationshipTypeId")));
    }

    private Mono<Void> ensureCountryOptional(UUID countryId) {
        if (countryId == null) {
            return Mono.empty();
        }
        return repository.existsCountry(countryId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_NOT_FOUND", "Invalid countryId")));
    }

    private Mono<Void> ensureGenderOptional(UUID genderId) {
        if (genderId == null) {
            return Mono.empty();
        }
        return repository.existsGender(genderId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "GENDER_NOT_FOUND", "Invalid genderId")));
    }

    private Mono<Void> ensureNationalityOptional(UUID nationalityId) {
        if (nationalityId == null) {
            return Mono.empty();
        }
        return repository.existsNationality(nationalityId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "NATIONALITY_NOT_FOUND", "Invalid nationalityId")));
    }

    private Mono<Void> validateBeneficiaryAllocation(String tenantId, UUID employeeId, BigDecimal allocation, UUID excludeBeneficiaryId) {
        return repository.activeBeneficiaryAllocationTotal(tenantId, employeeId, excludeBeneficiaryId)
                .flatMap(total -> {
                    BigDecimal effectiveTotal = (total == null ? BigDecimal.ZERO : total).add(allocation);
                    if (effectiveTotal.compareTo(new BigDecimal("100")) > 0) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "BENEFICIARY_ALLOCATION_EXCEEDED", "Total active allocation cannot exceed 100"));
                    }
                    return Mono.empty();
                });
    }

    private void validateEmploymentHistory(EmploymentHistoryUpsertRequest request) {
        if (request.actionType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTION_TYPE_REQUIRED", "actionType is required");
        }
        if (request.effectiveDate() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EFFECTIVE_DATE_REQUIRED", "effectiveDate is required");
        }
        boolean changed = !equalsNullable(request.oldDepartmentId(), request.newDepartmentId())
                || !equalsNullable(request.oldDesignationId(), request.newDesignationId())
                || !equalsNullable(request.oldLocationId(), request.newLocationId())
                || !equalsNullable(request.oldGradeId(), request.newGradeId())
                || !equalsNullable(request.oldManagerId(), request.newManagerId());
        if (!changed) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "NO_CHANGE_DETECTED", "At least one old/new field must differ");
        }
    }

    private Mono<DigitalOnboardingView> getOnboardingOrError(String tenantId, UUID employeeId) {
        return repository.findOnboardingByEmployeeId(tenantId, employeeId)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "ONBOARDING_NOT_FOUND", "Onboarding not found")));
    }

    private Mono<DigitalOnboardingView> transitionOnboarding(UUID employeeId, String actor, String remarks, DigitalOnboardingStatus status, String rejectedReason) {
        return requireTenant().flatMap(tenantId -> ensureEmployee(tenantId, employeeId)
                .then(getOnboardingOrError(tenantId, employeeId))
                .flatMap(current -> repository.updateOnboarding(tenantId, employeeId, new DigitalOnboardingUpsertRequest(
                                current.eFormCompleted(),
                                current.documentUploadCompleted(),
                                current.policyAcknowledged(),
                                remarks,
                                actor),
                        status,
                        rejectedReason)));
    }

    private boolean equalsNullable(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }
}
