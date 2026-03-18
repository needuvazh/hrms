package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class EmployeeInformationR2dbcRepository implements EmployeeInformationRepository {

    private final DatabaseClient databaseClient;

    public EmployeeInformationR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Long> nextEmployeeCodeSequence() {
        return databaseClient.sql("SELECT nextval('employee.employee_code_seq') AS seq")
                .map((row, metadata) -> row.get("seq", Long.class))
                .one();
    }

    @Override
    public Mono<EmployeeProfileView> createEmployee(String tenantId, EmployeeProfileUpsertRequest request, String employeeCode) {
        UUID employeeId = UUID.randomUUID();
        Instant now = Instant.now();
        return databaseClient.sql("""
                        INSERT INTO employee.employees(id, tenant_id, employee_code, person_id, first_name, last_name, email, department_code, job_title, created_at, updated_at)
                        VALUES (:id, :tenantId, :employeeCode, :personId, :firstName, :lastName, :email, NULL, :jobTitle, :createdAt, :updatedAt)
                        """)
                .bind("id", employeeId)
                .bind("tenantId", tenantId)
                .bind("employeeCode", employeeCode)
                .bind("personId", request.personId() == null ? java.util.UUID.randomUUID() : request.personId())
                .bind("firstName", request.firstName())
                .bind("lastName", request.lastName())
                .bind("email", request.officialEmail())
                .bind("jobTitle", request.jobTitle())
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("""
                                INSERT INTO employee.employee_profiles(
                                    employee_id, tenant_id, employee_code, first_name, middle_name, last_name, full_name,
                                    arabic_name, profile_photo_path, gender_id, date_of_birth, marital_status_id, nationality_id,
                                    blood_group, personal_email, official_email, primary_mobile_number, secondary_mobile_number,
                                    alternate_contact_number, employee_status, date_of_joining, confirmation_date, probation_end_date,
                                    retirement_date, workforce_category, department_id, designation_id, job_title, is_active,
                                    created_at, created_by, updated_at, updated_by
                                ) VALUES (
                                    :employeeId, :tenantId, :employeeCode, :firstName, :middleName, :lastName, :fullName,
                                    :arabicName, :profilePhotoPath, :genderId, :dateOfBirth, :maritalStatusId, :nationalityId,
                                    :bloodGroup, :personalEmail, :officialEmail, :primaryMobileNumber, :secondaryMobileNumber,
                                    :alternateContactNumber, :employeeStatus, :dateOfJoining, :confirmationDate, :probationEndDate,
                                    :retirementDate, :workforceCategory, :departmentId, :designationId, :jobTitle, TRUE,
                                    :createdAt, :createdBy, :updatedAt, :updatedBy
                                )
                                """)
                        .bind("employeeId", employeeId)
                        .bind("tenantId", tenantId)
                        .bind("employeeCode", employeeCode)
                        .bind("firstName", request.firstName())
                        .bind("middleName", request.middleName())
                        .bind("lastName", request.lastName())
                        .bind("fullName", request.fullName())
                        .bind("arabicName", request.arabicName())
                        .bind("profilePhotoPath", request.profilePhotoPath())
                        .bind("genderId", request.genderId())
                        .bind("dateOfBirth", request.dateOfBirth())
                        .bind("maritalStatusId", request.maritalStatusId())
                        .bind("nationalityId", request.nationalityId())
                        .bind("bloodGroup", request.bloodGroup())
                        .bind("personalEmail", request.personalEmail())
                        .bind("officialEmail", request.officialEmail())
                        .bind("primaryMobileNumber", request.primaryMobileNumber())
                        .bind("secondaryMobileNumber", request.secondaryMobileNumber())
                        .bind("alternateContactNumber", request.alternateContactNumber())
                        .bind("employeeStatus", request.employeeStatus().name())
                        .bind("dateOfJoining", request.dateOfJoining())
                        .bind("confirmationDate", request.confirmationDate())
                        .bind("probationEndDate", request.probationEndDate())
                        .bind("retirementDate", request.retirementDate())
                        .bind("workforceCategory", request.workforceCategory().name())
                        .bind("departmentId", request.departmentId())
                        .bind("designationId", request.designationId())
                        .bind("jobTitle", request.jobTitle())
                        .bind("createdAt", now)
                        .bind("createdBy", request.actor() == null ? "system" : request.actor())
                        .bind("updatedAt", now)
                        .bind("updatedBy", request.actor() == null ? "system" : request.actor())
                        .fetch().rowsUpdated())
                .then(findEmployeeById(tenantId, employeeId));
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployee(String tenantId, UUID employeeId, EmployeeProfileUpsertRequest request) {
        return Mono.error(new UnsupportedOperationException("updateEmployee not yet implemented in this iteration"));
    }

    @Override
    public Mono<EmployeeProfileView> findEmployeeById(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                        SELECT p.*, e.person_id
                        FROM employee.employee_profiles p
                        JOIN employee.employees e ON e.id = p.employee_id AND e.tenant_id = p.tenant_id
                        WHERE p.tenant_id = :tenantId AND p.employee_id = :employeeId
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> new EmployeeProfileView(
                        row.get("employee_id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_code", String.class),
                        row.get("first_name", String.class),
                        row.get("middle_name", String.class),
                        row.get("last_name", String.class),
                        row.get("full_name", String.class),
                        row.get("arabic_name", String.class),
                        row.get("profile_photo_path", String.class),
                        row.get("gender_id", UUID.class),
                        row.get("date_of_birth", LocalDate.class),
                        row.get("marital_status_id", UUID.class),
                        row.get("nationality_id", UUID.class),
                        row.get("blood_group", String.class),
                        row.get("personal_email", String.class),
                        row.get("official_email", String.class),
                        row.get("primary_mobile_number", String.class),
                        row.get("secondary_mobile_number", String.class),
                        row.get("alternate_contact_number", String.class),
                        EmployeeStatus.valueOf(row.get("employee_status", String.class)),
                        row.get("date_of_joining", LocalDate.class),
                        row.get("confirmation_date", LocalDate.class),
                        row.get("probation_end_date", LocalDate.class),
                        row.get("retirement_date", LocalDate.class),
                        WorkforceCategory.valueOf(row.get("workforce_category", String.class)),
                        row.get("department_id", UUID.class),
                        row.get("designation_id", UUID.class),
                        row.get("job_title", String.class),
                        row.get("person_id", UUID.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .one();
    }

    @Override
    public Mono<EmployeeProfileView> findEmployeeByCode(String tenantId, String employeeCode) {
        return databaseClient.sql("SELECT employee_id FROM employee.employee_profiles WHERE tenant_id = :tenantId AND employee_code = :employeeCode")
                .bind("tenantId", tenantId)
                .bind("employeeCode", employeeCode)
                .map((row, metadata) -> row.get("employee_id", UUID.class))
                .one()
                .flatMap(employeeId -> findEmployeeById(tenantId, employeeId));
    }

    @Override
    public Flux<EmployeeProfileView> searchEmployees(String tenantId, EmployeeSearchFilter filter) {
        return databaseClient.sql("SELECT employee_id FROM employee.employee_profiles WHERE tenant_id = :tenantId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
                .bind("tenantId", tenantId)
                .bind("limit", filter.limit())
                .bind("offset", filter.offset())
                .map((row, metadata) -> row.get("employee_id", UUID.class))
                .all()
                .concatMap(id -> findEmployeeById(tenantId, id));
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployeeStatus(String tenantId, UUID employeeId, EmployeeStatus employeeStatus, String actor) {
        return databaseClient.sql("UPDATE employee.employee_profiles SET employee_status = :employeeStatus, updated_at = :updatedAt, updated_by = :updatedBy WHERE tenant_id = :tenantId AND employee_id = :employeeId")
                .bind("employeeStatus", employeeStatus.name())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", actor == null ? "system" : actor)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .fetch().rowsUpdated()
                .then(findEmployeeById(tenantId, employeeId));
    }

    @Override
    public Mono<Boolean> existsEmployeeById(String tenantId, UUID employeeId) {
        return exists("SELECT count(*) AS cnt FROM employee.employees WHERE tenant_id = :tenantId AND id = :id", tenantId, employeeId);
    }

    @Override
    public Mono<Boolean> existsOfficialEmail(String tenantId, String officialEmail, UUID excludeEmployeeId) {
        return databaseClient.sql("SELECT count(*) AS cnt FROM employee.employee_profiles WHERE tenant_id = :tenantId AND lower(official_email) = lower(:officialEmail)")
                .bind("tenantId", tenantId)
                .bind("officialEmail", officialEmail)
                .map((row, metadata) -> row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> existsCountry(UUID countryId) { return existsById("master_data.countries", countryId); }

    @Override
    public Mono<Boolean> existsNationality(UUID nationalityId) { return existsById("master_data.nationalities", nationalityId); }

    @Override
    public Mono<Boolean> existsGender(UUID genderId) { return existsById("master_data.genders", genderId); }

    @Override
    public Mono<Boolean> existsMaritalStatus(UUID maritalStatusId) { return existsById("master_data.marital_statuses", maritalStatusId); }

    @Override
    public Mono<Boolean> existsRelationshipType(UUID relationshipTypeId) { return existsById("master_data.relationship_types", relationshipTypeId); }

    @Override
    public Mono<Boolean> existsDepartment(String tenantId, UUID departmentId) { return exists("SELECT count(*) AS cnt FROM organization.departments WHERE tenant_id = :tenantId AND id = :id", tenantId, departmentId); }

    @Override
    public Mono<Boolean> existsDesignation(String tenantId, UUID designationId) { return exists("SELECT count(*) AS cnt FROM job_architecture.designations WHERE tenant_id = :tenantId AND id = :id", tenantId, designationId); }

    @Override
    public Mono<Boolean> existsWorkLocation(String tenantId, UUID locationId) { return exists("SELECT count(*) AS cnt FROM organization.work_locations WHERE tenant_id = :tenantId AND id = :id", tenantId, locationId); }

    @Override
    public Mono<Boolean> existsGrade(String tenantId, UUID gradeId) { return exists("SELECT count(*) AS cnt FROM job_architecture.grades WHERE tenant_id = :tenantId AND id = :id", tenantId, gradeId); }

    @Override
    public Mono<Boolean> existsEmployee(String tenantId, UUID employeeId) { return existsEmployeeById(tenantId, employeeId); }

    private Mono<Boolean> existsById(String tableName, UUID id) {
        return databaseClient.sql("SELECT count(*) AS cnt FROM " + tableName + " WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> row.get("cnt", Long.class) > 0)
                .one();
    }

    private Mono<Boolean> exists(String sql, String tenantId, UUID id) {
        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> row.get("cnt", Long.class) > 0)
                .one();
    }

    private <T> Mono<T> unsupported() {
        return Mono.error(new UnsupportedOperationException("Method pending implementation"));
    }

    private <T> Flux<T> unsupportedFlux() {
        return Flux.error(new UnsupportedOperationException("Method pending implementation"));
    }

    @Override public Mono<EmployeeAddressView> createAddress(String tenantId, UUID employeeId, EmployeeAddressUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmployeeAddressView> updateAddress(String tenantId, UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmployeeAddressView> findAddressById(String tenantId, UUID employeeId, UUID addressId) { return unsupported(); }
    @Override public Flux<EmployeeAddressView> findAddressesByEmployeeId(String tenantId, UUID employeeId) { return unsupportedFlux(); }
    @Override public Mono<EmployeeAddressView> deactivateAddress(String tenantId, UUID employeeId, UUID addressId, String actor) { return unsupported(); }
    @Override public Mono<Void> clearPrimaryAddressByType(String tenantId, UUID employeeId, AddressType addressType, UUID excludeAddressId) { return Mono.empty(); }
    @Override public Mono<EmergencyContactView> createEmergencyContact(String tenantId, UUID employeeId, EmergencyContactUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmergencyContactView> updateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmergencyContactView> findEmergencyContactById(String tenantId, UUID employeeId, UUID emergencyContactId) { return unsupported(); }
    @Override public Flux<EmergencyContactView> findEmergencyContactsByEmployeeId(String tenantId, UUID employeeId) { return unsupportedFlux(); }
    @Override public Mono<EmergencyContactView> deactivateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, String actor) { return unsupported(); }
    @Override public Mono<Void> clearPrimaryEmergencyContact(String tenantId, UUID employeeId, UUID excludeEmergencyContactId) { return Mono.empty(); }
    @Override public Mono<DependantView> createDependant(String tenantId, UUID employeeId, DependantUpsertRequest request) { return unsupported(); }
    @Override public Mono<DependantView> updateDependant(String tenantId, UUID employeeId, UUID dependantId, DependantUpsertRequest request) { return unsupported(); }
    @Override public Mono<DependantView> findDependantById(String tenantId, UUID employeeId, UUID dependantId) { return unsupported(); }
    @Override public Flux<DependantView> findDependantsByEmployeeId(String tenantId, UUID employeeId) { return unsupportedFlux(); }
    @Override public Mono<DependantView> deactivateDependant(String tenantId, UUID employeeId, UUID dependantId, String actor) { return unsupported(); }
    @Override public Mono<BeneficiaryView> createBeneficiary(String tenantId, UUID employeeId, BeneficiaryUpsertRequest request) { return unsupported(); }
    @Override public Mono<BeneficiaryView> updateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request) { return unsupported(); }
    @Override public Mono<BeneficiaryView> findBeneficiaryById(String tenantId, UUID employeeId, UUID beneficiaryId) { return unsupported(); }
    @Override public Flux<BeneficiaryView> findBeneficiariesByEmployeeId(String tenantId, UUID employeeId) { return unsupportedFlux(); }
    @Override public Mono<BeneficiaryView> deactivateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, String actor) { return unsupported(); }
    @Override public Mono<java.math.BigDecimal> activeBeneficiaryAllocationTotal(String tenantId, UUID employeeId, UUID excludeBeneficiaryId) { return Mono.just(java.math.BigDecimal.ZERO); }
    @Override public Mono<WorkforceDetailView> upsertWorkforceDetail(String tenantId, UUID employeeId, WorkforceDetailUpsertRequest request) { return unsupported(); }
    @Override public Mono<WorkforceDetailView> findWorkforceDetailByEmployeeId(String tenantId, UUID employeeId) { return unsupported(); }
    @Override public Mono<EmployeeDocumentView> createDocument(String tenantId, UUID employeeId, EmployeeDocumentUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmployeeDocumentView> updateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmployeeDocumentView> updateDocumentFile(String tenantId, UUID employeeId, UUID employeeDocumentId, String fileName, String filePath, String fileType, long fileSize, String actor) { return unsupported(); }
    @Override public Mono<EmployeeDocumentView> updateDocumentVerification(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request) { return unsupported(); }
    @Override public Mono<EmployeeDocumentView> findDocumentById(String tenantId, UUID employeeId, UUID employeeDocumentId) { return unsupported(); }
    @Override public Flux<EmployeeDocumentView> findDocumentsByEmployeeId(String tenantId, UUID employeeId, Boolean activeOnly) { return unsupportedFlux(); }
    @Override public Mono<EmployeeDocumentView> deactivateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, String actor) { return unsupported(); }
    @Override public Flux<EmployeeDocumentView> findDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to, Boolean activeOnly) { return unsupportedFlux(); }
    @Override public Mono<Long> countDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to) { return Mono.just(0L); }
    @Override public Flux<EmployeeDocumentView> findExpiredDocuments(String tenantId, UUID employeeId, Boolean activeOnly) { return unsupportedFlux(); }
    @Override public Mono<EmploymentHistoryView> createEmploymentHistory(String tenantId, UUID employeeId, EmploymentHistoryUpsertRequest request) { return unsupported(); }
    @Override public Mono<EmploymentHistoryView> findEmploymentHistoryRecord(String tenantId, UUID employeeId, UUID employmentHistoryId) { return unsupported(); }
    @Override public Flux<EmploymentHistoryView> findEmploymentHistoryByEmployeeId(String tenantId, UUID employeeId) { return unsupportedFlux(); }
    @Override public Mono<DigitalOnboardingView> createOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status) { return unsupported(); }
    @Override public Mono<DigitalOnboardingView> updateOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status, String rejectedReason) { return unsupported(); }
    @Override public Mono<DigitalOnboardingView> findOnboardingByEmployeeId(String tenantId, UUID employeeId) { return unsupported(); }
    @Override public Mono<PolicyAcknowledgementView> createPolicyAcknowledgement(String tenantId, UUID onboardingId, PolicyAcknowledgementRequest request) { return unsupported(); }
    @Override public Flux<PolicyAcknowledgementView> findPolicyAcknowledgements(String tenantId, UUID onboardingId) { return unsupportedFlux(); }
}
