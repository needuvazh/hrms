package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.EmployeeInformationDtos.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
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
        GenericExecuteSpec employeeInsert = databaseClient.sql("""
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
                .bind("createdAt", now)
                .bind("updatedAt", now);
        employeeInsert = bindNullable(employeeInsert, "jobTitle", request.jobTitle(), String.class);

        GenericExecuteSpec profileInsert = databaseClient.sql("""
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
                .bind("lastName", request.lastName())
                .bind("employeeStatus", request.employeeStatus().name())
                .bind("workforceCategory", request.workforceCategory().name())
                .bind("createdAt", now)
                .bind("createdBy", request.actor() == null ? "system" : request.actor())
                .bind("updatedAt", now)
                .bind("updatedBy", request.actor() == null ? "system" : request.actor());

        profileInsert = bindNullable(profileInsert, "middleName", request.middleName(), String.class);
        profileInsert = bindNullable(profileInsert, "fullName", request.fullName(), String.class);
        profileInsert = bindNullable(profileInsert, "arabicName", request.arabicName(), String.class);
        profileInsert = bindNullable(profileInsert, "profilePhotoPath", request.profilePhotoPath(), String.class);
        profileInsert = bindNullable(profileInsert, "genderId", request.genderId(), UUID.class);
        profileInsert = bindNullable(profileInsert, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        profileInsert = bindNullable(profileInsert, "maritalStatusId", request.maritalStatusId(), UUID.class);
        profileInsert = bindNullable(profileInsert, "nationalityId", request.nationalityId(), UUID.class);
        profileInsert = bindNullable(profileInsert, "bloodGroup", request.bloodGroup(), String.class);
        profileInsert = bindNullable(profileInsert, "personalEmail", request.personalEmail(), String.class);
        profileInsert = bindNullable(profileInsert, "officialEmail", request.officialEmail(), String.class);
        profileInsert = bindNullable(profileInsert, "primaryMobileNumber", request.primaryMobileNumber(), String.class);
        profileInsert = bindNullable(profileInsert, "secondaryMobileNumber", request.secondaryMobileNumber(), String.class);
        profileInsert = bindNullable(profileInsert, "alternateContactNumber", request.alternateContactNumber(), String.class);
        profileInsert = bindNullable(profileInsert, "dateOfJoining", request.dateOfJoining(), LocalDate.class);
        profileInsert = bindNullable(profileInsert, "confirmationDate", request.confirmationDate(), LocalDate.class);
        profileInsert = bindNullable(profileInsert, "probationEndDate", request.probationEndDate(), LocalDate.class);
        profileInsert = bindNullable(profileInsert, "retirementDate", request.retirementDate(), LocalDate.class);
        profileInsert = bindNullable(profileInsert, "departmentId", request.departmentId(), UUID.class);
        profileInsert = bindNullable(profileInsert, "designationId", request.designationId(), UUID.class);
        profileInsert = bindNullable(profileInsert, "jobTitle", request.jobTitle(), String.class);

        return employeeInsert.fetch()
                .rowsUpdated()
                .then(profileInsert.fetch().rowsUpdated())
                .then(findEmployeeById(tenantId, employeeId));
    }

    @Override
    public Mono<EmployeeProfileView> updateEmployee(String tenantId, UUID employeeId, EmployeeProfileUpsertRequest request) {
        Instant now = Instant.now();
        GenericExecuteSpec profileUpdate = databaseClient.sql("""
                        UPDATE employee.employee_profiles
                        SET first_name = :firstName,
                            middle_name = :middleName,
                            last_name = :lastName,
                            full_name = :fullName,
                            arabic_name = :arabicName,
                            profile_photo_path = :profilePhotoPath,
                            gender_id = :genderId,
                            date_of_birth = :dateOfBirth,
                            marital_status_id = :maritalStatusId,
                            nationality_id = :nationalityId,
                            blood_group = :bloodGroup,
                            personal_email = :personalEmail,
                            official_email = :officialEmail,
                            primary_mobile_number = :primaryMobileNumber,
                            secondary_mobile_number = :secondaryMobileNumber,
                            alternate_contact_number = :alternateContactNumber,
                            employee_status = :employeeStatus,
                            date_of_joining = :dateOfJoining,
                            confirmation_date = :confirmationDate,
                            probation_end_date = :probationEndDate,
                            retirement_date = :retirementDate,
                            workforce_category = :workforceCategory,
                            department_id = :departmentId,
                            designation_id = :designationId,
                            job_title = :jobTitle,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                        """)
                .bind("firstName", request.firstName())
                .bind("lastName", request.lastName())
                .bind("employeeStatus", request.employeeStatus().name())
                .bind("workforceCategory", request.workforceCategory().name())
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId);
        profileUpdate = bindNullable(profileUpdate, "middleName", request.middleName(), String.class);
        profileUpdate = bindNullable(profileUpdate, "fullName", request.fullName(), String.class);
        profileUpdate = bindNullable(profileUpdate, "arabicName", request.arabicName(), String.class);
        profileUpdate = bindNullable(profileUpdate, "profilePhotoPath", request.profilePhotoPath(), String.class);
        profileUpdate = bindNullable(profileUpdate, "genderId", request.genderId(), UUID.class);
        profileUpdate = bindNullable(profileUpdate, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        profileUpdate = bindNullable(profileUpdate, "maritalStatusId", request.maritalStatusId(), UUID.class);
        profileUpdate = bindNullable(profileUpdate, "nationalityId", request.nationalityId(), UUID.class);
        profileUpdate = bindNullable(profileUpdate, "bloodGroup", request.bloodGroup(), String.class);
        profileUpdate = bindNullable(profileUpdate, "personalEmail", request.personalEmail(), String.class);
        profileUpdate = bindNullable(profileUpdate, "officialEmail", request.officialEmail(), String.class);
        profileUpdate = bindNullable(profileUpdate, "primaryMobileNumber", request.primaryMobileNumber(), String.class);
        profileUpdate = bindNullable(profileUpdate, "secondaryMobileNumber", request.secondaryMobileNumber(), String.class);
        profileUpdate = bindNullable(profileUpdate, "alternateContactNumber", request.alternateContactNumber(), String.class);
        profileUpdate = bindNullable(profileUpdate, "dateOfJoining", request.dateOfJoining(), LocalDate.class);
        profileUpdate = bindNullable(profileUpdate, "confirmationDate", request.confirmationDate(), LocalDate.class);
        profileUpdate = bindNullable(profileUpdate, "probationEndDate", request.probationEndDate(), LocalDate.class);
        profileUpdate = bindNullable(profileUpdate, "retirementDate", request.retirementDate(), LocalDate.class);
        profileUpdate = bindNullable(profileUpdate, "departmentId", request.departmentId(), UUID.class);
        profileUpdate = bindNullable(profileUpdate, "designationId", request.designationId(), UUID.class);
        profileUpdate = bindNullable(profileUpdate, "jobTitle", request.jobTitle(), String.class);

        GenericExecuteSpec employeeUpdate = databaseClient.sql("""
                        UPDATE employee.employees
                        SET first_name = :firstName,
                            last_name = :lastName,
                            email = :email,
                            person_id = :personId,
                            job_title = :jobTitle,
                            updated_at = :updatedAt
                        WHERE tenant_id = :tenantId
                          AND id = :employeeId
                        """)
                .bind("firstName", request.firstName())
                .bind("lastName", request.lastName())
                .bind("email", request.officialEmail())
                .bind("personId", request.personId() == null ? UUID.randomUUID() : request.personId())
                .bind("updatedAt", now)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId);
        employeeUpdate = bindNullable(employeeUpdate, "jobTitle", request.jobTitle(), String.class);

        return employeeUpdate.fetch().rowsUpdated()
                .then(profileUpdate.fetch().rowsUpdated())
                .then(findEmployeeById(tenantId, employeeId));
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
        StringBuilder sql = new StringBuilder("""
                SELECT employee_id
                FROM employee.employee_profiles
                WHERE tenant_id = :tenantId
                """);
        if (StringUtils.hasText(filter.employeeCode())) {
            sql.append(" AND employee_code ILIKE :employeeCode ");
        }
        if (StringUtils.hasText(filter.employeeName())) {
            sql.append(" AND (first_name ILIKE :employeeName OR last_name ILIKE :employeeName OR full_name ILIKE :employeeName) ");
        }
        if (StringUtils.hasText(filter.officialEmail())) {
            sql.append(" AND official_email ILIKE :officialEmail ");
        }
        if (StringUtils.hasText(filter.primaryMobileNumber())) {
            sql.append(" AND primary_mobile_number ILIKE :primaryMobileNumber ");
        }
        if (filter.workforceCategory() != null) {
            sql.append(" AND workforce_category = :workforceCategory ");
        }
        if (filter.employeeStatus() != null) {
            sql.append(" AND employee_status = :employeeStatus ");
        }
        if (filter.joiningDateFrom() != null) {
            sql.append(" AND date_of_joining >= :joiningDateFrom ");
        }
        if (filter.joiningDateTo() != null) {
            sql.append(" AND date_of_joining <= :joiningDateTo ");
        }
        if (filter.departmentId() != null) {
            sql.append(" AND department_id = :departmentId ");
        }
        if (filter.designationId() != null) {
            sql.append(" AND designation_id = :designationId ");
        }
        sql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset ");

        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("limit", filter.limit())
                .bind("offset", filter.offset());
        if (StringUtils.hasText(filter.employeeCode())) {
            spec = spec.bind("employeeCode", "%" + filter.employeeCode().trim() + "%");
        }
        if (StringUtils.hasText(filter.employeeName())) {
            spec = spec.bind("employeeName", "%" + filter.employeeName().trim() + "%");
        }
        if (StringUtils.hasText(filter.officialEmail())) {
            spec = spec.bind("officialEmail", "%" + filter.officialEmail().trim() + "%");
        }
        if (StringUtils.hasText(filter.primaryMobileNumber())) {
            spec = spec.bind("primaryMobileNumber", "%" + filter.primaryMobileNumber().trim() + "%");
        }
        if (filter.workforceCategory() != null) {
            spec = spec.bind("workforceCategory", filter.workforceCategory().name());
        }
        if (filter.employeeStatus() != null) {
            spec = spec.bind("employeeStatus", filter.employeeStatus().name());
        }
        if (filter.joiningDateFrom() != null) {
            spec = spec.bind("joiningDateFrom", filter.joiningDateFrom());
        }
        if (filter.joiningDateTo() != null) {
            spec = spec.bind("joiningDateTo", filter.joiningDateTo());
        }
        if (filter.departmentId() != null) {
            spec = spec.bind("departmentId", filter.departmentId());
        }
        if (filter.designationId() != null) {
            spec = spec.bind("designationId", filter.designationId());
        }
        return spec.map((row, metadata) -> row.get("employee_id", UUID.class))
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
        if (!StringUtils.hasText(officialEmail)) {
            return Mono.just(false);
        }
        String sql = "SELECT count(*) AS cnt FROM employee.employee_profiles WHERE tenant_id = :tenantId AND lower(official_email) = lower(:officialEmail)";
        if (excludeEmployeeId != null) {
            sql = sql + " AND employee_id <> :excludeEmployeeId";
        }
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("officialEmail", officialEmail);
        if (excludeEmployeeId != null) {
            spec = spec.bind("excludeEmployeeId", excludeEmployeeId);
        }
        return spec
                .map((row, metadata) -> row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> existsCountry(UUID countryId) { return existsById("master_data.countries", countryId); }

    @Override
    public Mono<UUID> findCountryIdByCode(String countryCode) {
        return databaseClient.sql("SELECT id FROM master_data.countries WHERE lower(country_code) = lower(:countryCode)")
                .bind("countryCode", countryCode)
                .map((row, metadata) -> row.get("id", UUID.class))
                .one();
    }

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

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }

    private EmployeeAddressView mapAddress(io.r2dbc.spi.Row row) {
        return new EmployeeAddressView(
                row.get("address_id", UUID.class),
                row.get("employee_id", UUID.class),
                AddressType.valueOf(row.get("address_type", String.class)),
                row.get("flat_villa_number", String.class),
                row.get("building_name", String.class),
                row.get("street", String.class),
                row.get("area", String.class),
                row.get("city", String.class),
                row.get("state_province", String.class),
                row.get("country_id", UUID.class),
                row.get("postal_code", String.class),
                row.get("po_box", String.class),
                row.get("landmark", String.class),
                Boolean.TRUE.equals(row.get("is_primary", Boolean.class)),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private EmergencyContactView mapEmergencyContact(io.r2dbc.spi.Row row) {
        return new EmergencyContactView(
                row.get("emergency_contact_id", UUID.class),
                row.get("employee_id", UUID.class),
                row.get("name", String.class),
                row.get("relationship_type_id", UUID.class),
                row.get("primary_mobile_number", String.class),
                row.get("secondary_mobile_number", String.class),
                row.get("email", String.class),
                row.get("address_line1", String.class),
                row.get("address_line2", String.class),
                row.get("city", String.class),
                row.get("country_id", UUID.class),
                Boolean.TRUE.equals(row.get("is_primary", Boolean.class)),
                row.get("remarks", String.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private DependantView mapDependant(io.r2dbc.spi.Row row) {
        return new DependantView(
                row.get("dependant_id", UUID.class),
                row.get("employee_id", UUID.class),
                row.get("full_name", String.class),
                row.get("relationship_type_id", UUID.class),
                row.get("gender_id", UUID.class),
                row.get("date_of_birth", LocalDate.class),
                row.get("nationality_id", UUID.class),
                row.get("passport_number", String.class),
                row.get("civil_id_number", String.class),
                Boolean.TRUE.equals(row.get("is_insurance_eligible", Boolean.class)),
                Boolean.TRUE.equals(row.get("is_minor", Boolean.class)),
                row.get("effective_from", LocalDate.class),
                row.get("effective_to", LocalDate.class),
                row.get("status", String.class),
                row.get("remarks", String.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private BeneficiaryView mapBeneficiary(io.r2dbc.spi.Row row) {
        return new BeneficiaryView(
                row.get("beneficiary_id", UUID.class),
                row.get("employee_id", UUID.class),
                row.get("full_name", String.class),
                row.get("relationship_type_id", UUID.class),
                row.get("date_of_birth", LocalDate.class),
                row.get("contact_number", String.class),
                row.get("email", String.class),
                row.get("address", String.class),
                row.get("identification_number", String.class),
                row.get("allocation_percentage", BigDecimal.class),
                row.get("priority_order", Integer.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("remarks", String.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private WorkforceDetailView mapWorkforceDetail(io.r2dbc.spi.Row row) {
        return new WorkforceDetailView(
                row.get("workforce_detail_id", UUID.class),
                row.get("employee_id", UUID.class),
                WorkforceCategory.valueOf(row.get("workforce_category", String.class)),
                row.get("pasi_number", String.class),
                row.get("pasi_registration_date", LocalDate.class),
                row.get("permit_number", String.class),
                row.get("permit_type", String.class),
                row.get("sponsor_name", String.class),
                row.get("sponsor_id", String.class),
                row.get("visa_status", String.class),
                row.get("work_permit_issue_date", LocalDate.class),
                row.get("work_permit_expiry_date", LocalDate.class),
                row.get("remarks", String.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private EmployeeDocumentView mapDocument(io.r2dbc.spi.Row row) {
        return new EmployeeDocumentView(
                row.get("employee_document_id", UUID.class),
                row.get("employee_id", UUID.class),
                EmployeeDocumentType.valueOf(row.get("document_type", String.class)),
                row.get("document_name", String.class),
                row.get("document_number", String.class),
                row.get("issuing_country_id", UUID.class),
                row.get("issuing_authority", String.class),
                row.get("issue_date", LocalDate.class),
                row.get("expiry_date", LocalDate.class),
                row.get("file_name", String.class),
                row.get("file_path", String.class),
                row.get("file_type", String.class),
                row.get("file_size", Long.class),
                EmployeeDocumentVerificationStatus.valueOf(row.get("verification_status", String.class)),
                row.get("verified_by", String.class),
                row.get("verified_date", Instant.class),
                Boolean.TRUE.equals(row.get("alert_enabled", Boolean.class)),
                row.get("remarks", String.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private EmploymentHistoryView mapEmploymentHistory(io.r2dbc.spi.Row row) {
        return new EmploymentHistoryView(
                row.get("employment_history_id", UUID.class),
                row.get("employee_id", UUID.class),
                EmploymentActionType.valueOf(row.get("action_type", String.class)),
                row.get("old_department_id", UUID.class),
                row.get("new_department_id", UUID.class),
                row.get("old_designation_id", UUID.class),
                row.get("new_designation_id", UUID.class),
                row.get("old_location_id", UUID.class),
                row.get("new_location_id", UUID.class),
                row.get("old_grade_id", UUID.class),
                row.get("new_grade_id", UUID.class),
                row.get("old_manager_id", UUID.class),
                row.get("new_manager_id", UUID.class),
                row.get("effective_date", LocalDate.class),
                row.get("remarks", String.class),
                row.get("supporting_document_path", String.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class));
    }

    private PolicyAcknowledgementView mapPolicyAcknowledgement(io.r2dbc.spi.Row row) {
        return new PolicyAcknowledgementView(
                row.get("acknowledgement_id", UUID.class),
                row.get("onboarding_id", UUID.class),
                row.get("policy_code", String.class),
                row.get("policy_version", String.class),
                Boolean.TRUE.equals(row.get("accepted_flag", Boolean.class)),
                row.get("accepted_date_time", Instant.class),
                row.get("accepted_by", String.class),
                row.get("remarks", String.class),
                row.get("created_at", Instant.class));
    }

    private DigitalOnboardingView mapOnboardingBase(io.r2dbc.spi.Row row) {
        return new DigitalOnboardingView(
                row.get("onboarding_id", UUID.class),
                row.get("employee_id", UUID.class),
                DigitalOnboardingStatus.valueOf(row.get("onboarding_status", String.class)),
                Boolean.TRUE.equals(row.get("eform_completed", Boolean.class)),
                Boolean.TRUE.equals(row.get("document_upload_completed", Boolean.class)),
                Boolean.TRUE.equals(row.get("policy_acknowledged", Boolean.class)),
                row.get("submitted_date", Instant.class),
                row.get("reviewed_date", Instant.class),
                row.get("approved_date", Instant.class),
                row.get("rejected_reason", String.class),
                row.get("remarks", String.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                java.util.List.of());
    }

    private DigitalOnboardingView withAcknowledgements(DigitalOnboardingView base, java.util.List<PolicyAcknowledgementView> acknowledgements) {
        return new DigitalOnboardingView(
                base.onboardingId(),
                base.employeeId(),
                base.onboardingStatus(),
                base.eFormCompleted(),
                base.documentUploadCompleted(),
                base.policyAcknowledged(),
                base.submittedDate(),
                base.reviewedDate(),
                base.approvedDate(),
                base.rejectedReason(),
                base.remarks(),
                base.createdAt(),
                base.updatedAt(),
                acknowledgements);
    }

    @Override
    public Mono<EmployeeAddressView> createAddress(String tenantId, UUID employeeId, EmployeeAddressUpsertRequest request) {
        UUID addressId = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO employee.employee_addresses(
                            address_id, tenant_id, employee_id, address_type, flat_villa_number, building_name,
                            street, area, city, state_province, country_id, postal_code, po_box, landmark,
                            is_primary, is_active, created_at, created_by, updated_at, updated_by
                        ) VALUES (
                            :addressId, :tenantId, :employeeId, :addressType, :flatVillaNumber, :buildingName,
                            :street, :area, :city, :stateProvince, :countryId, :postalCode, :poBox, :landmark,
                            :isPrimary, :isActive, :createdAt, :createdBy, :updatedAt, :updatedBy
                        )
                        RETURNING *
                        """)
                .bind("addressId", addressId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("addressType", request.addressType().name())
                .bind("city", request.city())
                .bind("countryId", request.countryId())
                .bind("isPrimary", request.primary())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "flatVillaNumber", request.flatVillaNumber(), String.class);
        spec = bindNullable(spec, "buildingName", request.buildingName(), String.class);
        spec = bindNullable(spec, "street", request.street(), String.class);
        spec = bindNullable(spec, "area", request.area(), String.class);
        spec = bindNullable(spec, "stateProvince", request.stateProvince(), String.class);
        spec = bindNullable(spec, "postalCode", request.postalCode(), String.class);
        spec = bindNullable(spec, "poBox", request.poBox(), String.class);
        spec = bindNullable(spec, "landmark", request.landmark(), String.class);
        return spec.map((row, metadata) -> mapAddress(row)).one();
    }

    @Override
    public Mono<EmployeeAddressView> updateAddress(String tenantId, UUID employeeId, UUID addressId, EmployeeAddressUpsertRequest request) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE employee.employee_addresses
                        SET address_type = :addressType,
                            flat_villa_number = :flatVillaNumber,
                            building_name = :buildingName,
                            street = :street,
                            area = :area,
                            city = :city,
                            state_province = :stateProvince,
                            country_id = :countryId,
                            postal_code = :postalCode,
                            po_box = :poBox,
                            landmark = :landmark,
                            is_primary = :isPrimary,
                            is_active = :isActive,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND address_id = :addressId
                        RETURNING *
                        """)
                .bind("addressType", request.addressType().name())
                .bind("city", request.city())
                .bind("countryId", request.countryId())
                .bind("isPrimary", request.primary())
                .bind("isActive", request.active())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("addressId", addressId);
        spec = bindNullable(spec, "flatVillaNumber", request.flatVillaNumber(), String.class);
        spec = bindNullable(spec, "buildingName", request.buildingName(), String.class);
        spec = bindNullable(spec, "street", request.street(), String.class);
        spec = bindNullable(spec, "area", request.area(), String.class);
        spec = bindNullable(spec, "stateProvince", request.stateProvince(), String.class);
        spec = bindNullable(spec, "postalCode", request.postalCode(), String.class);
        spec = bindNullable(spec, "poBox", request.poBox(), String.class);
        spec = bindNullable(spec, "landmark", request.landmark(), String.class);
        return spec.map((row, metadata) -> mapAddress(row)).one();
    }

    @Override
    public Mono<EmployeeAddressView> findAddressById(String tenantId, UUID employeeId, UUID addressId) {
        return databaseClient.sql("""
                        SELECT *
                        FROM employee.employee_addresses
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND address_id = :addressId
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("addressId", addressId)
                .map((row, metadata) -> mapAddress(row))
                .one();
    }

    @Override
    public Flux<EmployeeAddressView> findAddressesByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                        SELECT *
                        FROM employee.employee_addresses
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                        ORDER BY created_at DESC
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapAddress(row))
                .all();
    }

    @Override
    public Mono<EmployeeAddressView> deactivateAddress(String tenantId, UUID employeeId, UUID addressId, String actor) {
        return databaseClient.sql("""
                        UPDATE employee.employee_addresses
                        SET is_active = FALSE,
                            is_primary = FALSE,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND address_id = :addressId
                        RETURNING *
                        """)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("addressId", addressId)
                .map((row, metadata) -> mapAddress(row))
                .one();
    }

    @Override
    public Mono<Void> clearPrimaryAddressByType(String tenantId, UUID employeeId, AddressType addressType, UUID excludeAddressId) {
        String sql = """
                UPDATE employee.employee_addresses
                SET is_primary = FALSE, updated_at = :updatedAt
                WHERE tenant_id = :tenantId
                  AND employee_id = :employeeId
                  AND address_type = :addressType
                """;
        GenericExecuteSpec spec = databaseClient.sql(excludeAddressId == null ? sql : sql + " AND address_id <> :excludeAddressId")
                .bind("updatedAt", Instant.now())
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("addressType", addressType.name());
        if (excludeAddressId != null) {
            spec = spec.bind("excludeAddressId", excludeAddressId);
        }
        return spec.fetch().rowsUpdated().then();
    }
    @Override
    public Mono<EmergencyContactView> createEmergencyContact(String tenantId, UUID employeeId, EmergencyContactUpsertRequest request) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_emergency_contacts(
                    emergency_contact_id, tenant_id, employee_id, name, relationship_type_id, primary_mobile_number,
                    secondary_mobile_number, email, address_line1, address_line2, city, country_id,
                    is_primary, remarks, is_active, created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :name, :relationshipTypeId, :primaryMobileNumber,
                    :secondaryMobileNumber, :email, :addressLine1, :addressLine2, :city, :countryId,
                    :isPrimary, :remarks, :isActive, :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("name", request.name())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("primaryMobileNumber", request.primaryMobileNumber())
                .bind("isPrimary", request.primary())
                .bind("remarks", request.remarks())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "secondaryMobileNumber", request.secondaryMobileNumber(), String.class);
        spec = bindNullable(spec, "email", request.email(), String.class);
        spec = bindNullable(spec, "addressLine1", request.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", request.addressLine2(), String.class);
        spec = bindNullable(spec, "city", request.city(), String.class);
        spec = bindNullable(spec, "countryId", request.countryId(), UUID.class);
        return spec.map((row, metadata) -> mapEmergencyContact(row)).one();
    }

    @Override
    public Mono<EmergencyContactView> updateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, EmergencyContactUpsertRequest request) {
        GenericExecuteSpec spec = databaseClient.sql("""
                UPDATE employee.employee_emergency_contacts
                SET name = :name,
                    relationship_type_id = :relationshipTypeId,
                    primary_mobile_number = :primaryMobileNumber,
                    secondary_mobile_number = :secondaryMobileNumber,
                    email = :email,
                    address_line1 = :addressLine1,
                    address_line2 = :addressLine2,
                    city = :city,
                    country_id = :countryId,
                    is_primary = :isPrimary,
                    remarks = :remarks,
                    is_active = :isActive,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND emergency_contact_id = :emergencyContactId
                RETURNING *
                """)
                .bind("name", request.name())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("primaryMobileNumber", request.primaryMobileNumber())
                .bind("isPrimary", request.primary())
                .bind("remarks", request.remarks())
                .bind("isActive", request.active())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("emergencyContactId", emergencyContactId);
        spec = bindNullable(spec, "secondaryMobileNumber", request.secondaryMobileNumber(), String.class);
        spec = bindNullable(spec, "email", request.email(), String.class);
        spec = bindNullable(spec, "addressLine1", request.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", request.addressLine2(), String.class);
        spec = bindNullable(spec, "city", request.city(), String.class);
        spec = bindNullable(spec, "countryId", request.countryId(), UUID.class);
        return spec.map((row, metadata) -> mapEmergencyContact(row)).one();
    }

    @Override
    public Mono<EmergencyContactView> findEmergencyContactById(String tenantId, UUID employeeId, UUID emergencyContactId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_emergency_contacts
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND emergency_contact_id = :emergencyContactId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("emergencyContactId", emergencyContactId)
                .map((row, metadata) -> mapEmergencyContact(row))
                .one();
    }

    @Override
    public Flux<EmergencyContactView> findEmergencyContactsByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_emergency_contacts
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                ORDER BY created_at DESC
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapEmergencyContact(row))
                .all();
    }

    @Override
    public Mono<EmergencyContactView> deactivateEmergencyContact(String tenantId, UUID employeeId, UUID emergencyContactId, String actor) {
        return databaseClient.sql("""
                UPDATE employee.employee_emergency_contacts
                SET is_active = FALSE, is_primary = FALSE, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND emergency_contact_id = :emergencyContactId
                RETURNING *
                """)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("emergencyContactId", emergencyContactId)
                .map((row, metadata) -> mapEmergencyContact(row))
                .one();
    }

    @Override
    public Mono<Void> clearPrimaryEmergencyContact(String tenantId, UUID employeeId, UUID excludeEmergencyContactId) {
        String sql = """
                UPDATE employee.employee_emergency_contacts
                SET is_primary = FALSE, updated_at = :updatedAt
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                """;
        GenericExecuteSpec spec = databaseClient.sql(excludeEmergencyContactId == null ? sql : sql + " AND emergency_contact_id <> :excludeId")
                .bind("updatedAt", Instant.now())
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId);
        if (excludeEmergencyContactId != null) {
            spec = spec.bind("excludeId", excludeEmergencyContactId);
        }
        return spec.fetch().rowsUpdated().then();
    }
    @Override
    public Mono<DependantView> createDependant(String tenantId, UUID employeeId, DependantUpsertRequest request) {
        UUID dependantId = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_dependants(
                    dependant_id, tenant_id, employee_id, full_name, relationship_type_id, gender_id, date_of_birth,
                    nationality_id, passport_number, civil_id_number, is_insurance_eligible, is_minor,
                    effective_from, effective_to, status, remarks, is_active, created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :fullName, :relationshipTypeId, :genderId, :dateOfBirth,
                    :nationalityId, :passportNumber, :civilIdNumber, :isInsuranceEligible, :isMinor,
                    :effectiveFrom, :effectiveTo, :status, :remarks, :isActive, :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("id", dependantId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("fullName", request.fullName())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("isInsuranceEligible", request.insuranceEligible())
                .bind("isMinor", request.minor())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "genderId", request.genderId(), UUID.class);
        spec = bindNullable(spec, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        spec = bindNullable(spec, "nationalityId", request.nationalityId(), UUID.class);
        spec = bindNullable(spec, "passportNumber", request.passportNumber(), String.class);
        spec = bindNullable(spec, "civilIdNumber", request.civilIdNumber(), String.class);
        spec = bindNullable(spec, "effectiveFrom", request.effectiveFrom(), LocalDate.class);
        spec = bindNullable(spec, "effectiveTo", request.effectiveTo(), LocalDate.class);
        spec = bindNullable(spec, "status", request.status(), String.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapDependant(row)).one();
    }

    @Override
    public Mono<DependantView> updateDependant(String tenantId, UUID employeeId, UUID dependantId, DependantUpsertRequest request) {
        GenericExecuteSpec spec = databaseClient.sql("""
                UPDATE employee.employee_dependants
                SET full_name = :fullName, relationship_type_id = :relationshipTypeId, gender_id = :genderId,
                    date_of_birth = :dateOfBirth, nationality_id = :nationalityId, passport_number = :passportNumber,
                    civil_id_number = :civilIdNumber, is_insurance_eligible = :isInsuranceEligible, is_minor = :isMinor,
                    effective_from = :effectiveFrom, effective_to = :effectiveTo, status = :status, remarks = :remarks,
                    is_active = :isActive, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND dependant_id = :dependantId
                RETURNING *
                """)
                .bind("fullName", request.fullName())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("isInsuranceEligible", request.insuranceEligible())
                .bind("isMinor", request.minor())
                .bind("isActive", request.active())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("dependantId", dependantId);
        spec = bindNullable(spec, "genderId", request.genderId(), UUID.class);
        spec = bindNullable(spec, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        spec = bindNullable(spec, "nationalityId", request.nationalityId(), UUID.class);
        spec = bindNullable(spec, "passportNumber", request.passportNumber(), String.class);
        spec = bindNullable(spec, "civilIdNumber", request.civilIdNumber(), String.class);
        spec = bindNullable(spec, "effectiveFrom", request.effectiveFrom(), LocalDate.class);
        spec = bindNullable(spec, "effectiveTo", request.effectiveTo(), LocalDate.class);
        spec = bindNullable(spec, "status", request.status(), String.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapDependant(row)).one();
    }

    @Override
    public Mono<DependantView> findDependantById(String tenantId, UUID employeeId, UUID dependantId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_dependants
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND dependant_id = :dependantId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("dependantId", dependantId)
                .map((row, metadata) -> mapDependant(row))
                .one();
    }

    @Override
    public Flux<DependantView> findDependantsByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_dependants
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                ORDER BY created_at DESC
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapDependant(row))
                .all();
    }

    @Override
    public Mono<DependantView> deactivateDependant(String tenantId, UUID employeeId, UUID dependantId, String actor) {
        return databaseClient.sql("""
                UPDATE employee.employee_dependants
                SET is_active = FALSE, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND dependant_id = :dependantId
                RETURNING *
                """)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("dependantId", dependantId)
                .map((row, metadata) -> mapDependant(row))
                .one();
    }
    @Override
    public Mono<BeneficiaryView> createBeneficiary(String tenantId, UUID employeeId, BeneficiaryUpsertRequest request) {
        UUID beneficiaryId = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_beneficiaries(
                    beneficiary_id, tenant_id, employee_id, full_name, relationship_type_id, date_of_birth,
                    contact_number, email, address, identification_number, allocation_percentage, priority_order,
                    is_active, remarks, created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :fullName, :relationshipTypeId, :dateOfBirth,
                    :contactNumber, :email, :address, :identificationNumber, :allocationPercentage, :priorityOrder,
                    :isActive, :remarks, :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("id", beneficiaryId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("fullName", request.fullName())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("allocationPercentage", request.allocationPercentage())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        spec = bindNullable(spec, "contactNumber", request.contactNumber(), String.class);
        spec = bindNullable(spec, "email", request.email(), String.class);
        spec = bindNullable(spec, "address", request.address(), String.class);
        spec = bindNullable(spec, "identificationNumber", request.identificationNumber(), String.class);
        spec = bindNullable(spec, "priorityOrder", request.priorityOrder(), Integer.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapBeneficiary(row)).one();
    }

    @Override
    public Mono<BeneficiaryView> updateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, BeneficiaryUpsertRequest request) {
        GenericExecuteSpec spec = databaseClient.sql("""
                UPDATE employee.employee_beneficiaries
                SET full_name = :fullName, relationship_type_id = :relationshipTypeId, date_of_birth = :dateOfBirth,
                    contact_number = :contactNumber, email = :email, address = :address, identification_number = :identificationNumber,
                    allocation_percentage = :allocationPercentage, priority_order = :priorityOrder, is_active = :isActive,
                    remarks = :remarks, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND beneficiary_id = :beneficiaryId
                RETURNING *
                """)
                .bind("fullName", request.fullName())
                .bind("relationshipTypeId", request.relationshipTypeId())
                .bind("allocationPercentage", request.allocationPercentage())
                .bind("isActive", request.active())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("beneficiaryId", beneficiaryId);
        spec = bindNullable(spec, "dateOfBirth", request.dateOfBirth(), LocalDate.class);
        spec = bindNullable(spec, "contactNumber", request.contactNumber(), String.class);
        spec = bindNullable(spec, "email", request.email(), String.class);
        spec = bindNullable(spec, "address", request.address(), String.class);
        spec = bindNullable(spec, "identificationNumber", request.identificationNumber(), String.class);
        spec = bindNullable(spec, "priorityOrder", request.priorityOrder(), Integer.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapBeneficiary(row)).one();
    }

    @Override
    public Mono<BeneficiaryView> findBeneficiaryById(String tenantId, UUID employeeId, UUID beneficiaryId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_beneficiaries
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND beneficiary_id = :beneficiaryId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("beneficiaryId", beneficiaryId)
                .map((row, metadata) -> mapBeneficiary(row))
                .one();
    }

    @Override
    public Flux<BeneficiaryView> findBeneficiariesByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_beneficiaries
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                ORDER BY priority_order NULLS LAST, created_at DESC
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapBeneficiary(row))
                .all();
    }

    @Override
    public Mono<BeneficiaryView> deactivateBeneficiary(String tenantId, UUID employeeId, UUID beneficiaryId, String actor) {
        return databaseClient.sql("""
                UPDATE employee.employee_beneficiaries
                SET is_active = FALSE, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND beneficiary_id = :beneficiaryId
                RETURNING *
                """)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("beneficiaryId", beneficiaryId)
                .map((row, metadata) -> mapBeneficiary(row))
                .one();
    }

    @Override
    public Mono<BigDecimal> activeBeneficiaryAllocationTotal(String tenantId, UUID employeeId, UUID excludeBeneficiaryId) {
        String sql = """
                SELECT COALESCE(SUM(allocation_percentage), 0) AS total
                FROM employee.employee_beneficiaries
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND is_active = TRUE
                """;
        GenericExecuteSpec spec = databaseClient.sql(excludeBeneficiaryId == null ? sql : sql + " AND beneficiary_id <> :excludeId")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId);
        if (excludeBeneficiaryId != null) {
            spec = spec.bind("excludeId", excludeBeneficiaryId);
        }
        return spec.map((row, metadata) -> row.get("total", BigDecimal.class)).one();
    }

    @Override
    public Mono<WorkforceDetailView> upsertWorkforceDetail(String tenantId, UUID employeeId, WorkforceDetailUpsertRequest request) {
        UUID detailId = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_workforce_details(
                    workforce_detail_id, tenant_id, employee_id, workforce_category, pasi_number, pasi_registration_date,
                    permit_number, permit_type, sponsor_name, sponsor_id, visa_status, work_permit_issue_date, work_permit_expiry_date,
                    remarks, created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :workforceCategory, :pasiNumber, :pasiRegistrationDate,
                    :permitNumber, :permitType, :sponsorName, :sponsorId, :visaStatus, :workPermitIssueDate, :workPermitExpiryDate,
                    :remarks, :createdAt, :createdBy, :updatedAt, :updatedBy
                )
                ON CONFLICT (tenant_id, employee_id)
                DO UPDATE SET workforce_category = EXCLUDED.workforce_category,
                              pasi_number = EXCLUDED.pasi_number,
                              pasi_registration_date = EXCLUDED.pasi_registration_date,
                              permit_number = EXCLUDED.permit_number,
                              permit_type = EXCLUDED.permit_type,
                              sponsor_name = EXCLUDED.sponsor_name,
                              sponsor_id = EXCLUDED.sponsor_id,
                              visa_status = EXCLUDED.visa_status,
                              work_permit_issue_date = EXCLUDED.work_permit_issue_date,
                              work_permit_expiry_date = EXCLUDED.work_permit_expiry_date,
                              remarks = EXCLUDED.remarks,
                              updated_at = EXCLUDED.updated_at,
                              updated_by = EXCLUDED.updated_by
                RETURNING *
                """)
                .bind("id", detailId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("workforceCategory", request.workforceCategory().name())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "pasiNumber", request.pasiNumber(), String.class);
        spec = bindNullable(spec, "pasiRegistrationDate", request.pasiRegistrationDate(), LocalDate.class);
        spec = bindNullable(spec, "permitNumber", request.permitNumber(), String.class);
        spec = bindNullable(spec, "permitType", request.permitType(), String.class);
        spec = bindNullable(spec, "sponsorName", request.sponsorName(), String.class);
        spec = bindNullable(spec, "sponsorId", request.sponsorId(), String.class);
        spec = bindNullable(spec, "visaStatus", request.visaStatus(), String.class);
        spec = bindNullable(spec, "workPermitIssueDate", request.workPermitIssueDate(), LocalDate.class);
        spec = bindNullable(spec, "workPermitExpiryDate", request.workPermitExpiryDate(), LocalDate.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapWorkforceDetail(row)).one();
    }

    @Override
    public Mono<WorkforceDetailView> findWorkforceDetailByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_workforce_details
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapWorkforceDetail(row))
                .one();
    }
    @Override
    public Mono<EmployeeDocumentView> createDocument(String tenantId, UUID employeeId, EmployeeDocumentUpsertRequest request) {
        UUID documentId = UUID.randomUUID();
        Instant now = Instant.now();
        EmployeeDocumentVerificationStatus status = request.verificationStatus() == null ? EmployeeDocumentVerificationStatus.PENDING : request.verificationStatus();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_documents(
                    employee_document_id, tenant_id, employee_id, document_type, document_name, document_number,
                    issuing_country_id, issuing_authority, issue_date, expiry_date, file_name, file_path, file_type, file_size,
                    verification_status, verified_by, verified_date, alert_enabled, remarks, is_active,
                    created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :documentType, :documentName, :documentNumber,
                    :issuingCountryId, :issuingAuthority, :issueDate, :expiryDate, :fileName, :filePath, :fileType, :fileSize,
                    :verificationStatus, NULL, NULL, :alertEnabled, :remarks, :isActive,
                    :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("id", documentId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("documentType", request.documentType().name())
                .bind("documentName", request.documentName())
                .bind("verificationStatus", status.name())
                .bind("alertEnabled", request.alertEnabled())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "documentNumber", request.documentNumber(), String.class);
        spec = bindNullable(spec, "issuingCountryId", request.issuingCountryId(), UUID.class);
        spec = bindNullable(spec, "issuingAuthority", request.issuingAuthority(), String.class);
        spec = bindNullable(spec, "issueDate", request.issueDate(), LocalDate.class);
        spec = bindNullable(spec, "expiryDate", request.expiryDate(), LocalDate.class);
        spec = bindNullable(spec, "fileName", request.fileName(), String.class);
        spec = bindNullable(spec, "filePath", request.filePath(), String.class);
        spec = bindNullable(spec, "fileType", request.fileType(), String.class);
        spec = bindNullable(spec, "fileSize", request.fileSize(), Long.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapDocument(row)).one();
    }

    @Override
    public Mono<EmployeeDocumentView> updateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentUpsertRequest request) {
        EmployeeDocumentVerificationStatus status = request.verificationStatus() == null ? EmployeeDocumentVerificationStatus.PENDING : request.verificationStatus();
        GenericExecuteSpec spec = databaseClient.sql("""
                UPDATE employee.employee_documents
                SET document_type = :documentType, document_name = :documentName, document_number = :documentNumber,
                    issuing_country_id = :issuingCountryId, issuing_authority = :issuingAuthority,
                    issue_date = :issueDate, expiry_date = :expiryDate, file_name = :fileName, file_path = :filePath,
                    file_type = :fileType, file_size = :fileSize, verification_status = :verificationStatus,
                    alert_enabled = :alertEnabled, remarks = :remarks, is_active = :isActive,
                    updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employee_document_id = :employeeDocumentId
                RETURNING *
                """)
                .bind("documentType", request.documentType().name())
                .bind("documentName", request.documentName())
                .bind("verificationStatus", status.name())
                .bind("alertEnabled", request.alertEnabled())
                .bind("isActive", request.active())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employeeDocumentId", employeeDocumentId);
        spec = bindNullable(spec, "documentNumber", request.documentNumber(), String.class);
        spec = bindNullable(spec, "issuingCountryId", request.issuingCountryId(), UUID.class);
        spec = bindNullable(spec, "issuingAuthority", request.issuingAuthority(), String.class);
        spec = bindNullable(spec, "issueDate", request.issueDate(), LocalDate.class);
        spec = bindNullable(spec, "expiryDate", request.expiryDate(), LocalDate.class);
        spec = bindNullable(spec, "fileName", request.fileName(), String.class);
        spec = bindNullable(spec, "filePath", request.filePath(), String.class);
        spec = bindNullable(spec, "fileType", request.fileType(), String.class);
        spec = bindNullable(spec, "fileSize", request.fileSize(), Long.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapDocument(row)).one();
    }

    @Override
    public Mono<EmployeeDocumentView> updateDocumentFile(String tenantId, UUID employeeId, UUID employeeDocumentId, String fileName, String filePath, String fileType, long fileSize, String actor) {
        return databaseClient.sql("""
                UPDATE employee.employee_documents
                SET file_name = :fileName, file_path = :filePath, file_type = :fileType, file_size = :fileSize,
                    updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employee_document_id = :employeeDocumentId
                RETURNING *
                """)
                .bind("fileName", fileName)
                .bind("filePath", filePath)
                .bind("fileType", fileType)
                .bind("fileSize", fileSize)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employeeDocumentId", employeeDocumentId)
                .map((row, metadata) -> mapDocument(row))
                .one();
    }

    @Override
    public Mono<EmployeeDocumentView> updateDocumentVerification(String tenantId, UUID employeeId, UUID employeeDocumentId, EmployeeDocumentVerificationRequest request) {
        return databaseClient.sql("""
                UPDATE employee.employee_documents
                SET verification_status = :verificationStatus,
                    verified_by = :verifiedBy,
                    verified_date = :verifiedDate,
                    remarks = COALESCE(:remarks, remarks),
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employee_document_id = :employeeDocumentId
                RETURNING *
                """)
                .bind("verificationStatus", request.verificationStatus().name())
                .bind("verifiedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("verifiedDate", Instant.now())
                .bind("remarks", request.remarks())
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employeeDocumentId", employeeDocumentId)
                .map((row, metadata) -> mapDocument(row))
                .one();
    }

    @Override
    public Mono<EmployeeDocumentView> findDocumentById(String tenantId, UUID employeeId, UUID employeeDocumentId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_documents
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employee_document_id = :employeeDocumentId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employeeDocumentId", employeeDocumentId)
                .map((row, metadata) -> mapDocument(row))
                .one();
    }

    @Override
    public Flux<EmployeeDocumentView> findDocumentsByEmployeeId(String tenantId, UUID employeeId, Boolean activeOnly) {
        String sql = """
                SELECT * FROM employee.employee_documents
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                """ + (Boolean.TRUE.equals(activeOnly) ? " AND is_active = TRUE " : "") + " ORDER BY created_at DESC";
        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapDocument(row))
                .all();
    }

    @Override
    public Mono<EmployeeDocumentView> deactivateDocument(String tenantId, UUID employeeId, UUID employeeDocumentId, String actor) {
        return databaseClient.sql("""
                UPDATE employee.employee_documents
                SET is_active = FALSE, updated_at = :updatedAt, updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employee_document_id = :employeeDocumentId
                RETURNING *
                """)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", StringUtils.hasText(actor) ? actor : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employeeDocumentId", employeeDocumentId)
                .map((row, metadata) -> mapDocument(row))
                .one();
    }

    @Override
    public Flux<EmployeeDocumentView> findDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM employee.employee_documents
                WHERE tenant_id = :tenantId
                  AND expiry_date IS NOT NULL
                  AND expiry_date BETWEEN :fromDate AND :toDate
                """);
        if (employeeId != null) {
            sql.append(" AND employee_id = :employeeId ");
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append(" AND is_active = TRUE ");
        }
        sql.append(" ORDER BY expiry_date ASC ");
        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("fromDate", from)
                .bind("toDate", to);
        if (employeeId != null) {
            spec = spec.bind("employeeId", employeeId);
        }
        return spec.map((row, metadata) -> mapDocument(row)).all();
    }

    @Override
    public Mono<Long> countDocumentsByExpiryRange(String tenantId, UUID employeeId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("""
                SELECT count(*) AS cnt
                FROM employee.employee_documents
                WHERE tenant_id = :tenantId
                  AND expiry_date IS NOT NULL
                  AND expiry_date BETWEEN :fromDate AND :toDate
                """);
        if (employeeId != null) {
            sql.append(" AND employee_id = :employeeId ");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("fromDate", from)
                .bind("toDate", to);
        if (employeeId != null) {
            spec = spec.bind("employeeId", employeeId);
        }
        return spec.map((row, metadata) -> {
            Number count = row.get("cnt", Number.class);
            return count == null ? 0L : count.longValue();
        }).one();
    }

    @Override
    public Flux<EmployeeDocumentView> findExpiredDocuments(String tenantId, UUID employeeId, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("""
                SELECT * FROM employee.employee_documents
                WHERE tenant_id = :tenantId
                  AND expiry_date IS NOT NULL
                  AND expiry_date < :today
                """);
        if (employeeId != null) {
            sql.append(" AND employee_id = :employeeId ");
        }
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append(" AND is_active = TRUE ");
        }
        sql.append(" ORDER BY expiry_date ASC ");
        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("today", LocalDate.now());
        if (employeeId != null) {
            spec = spec.bind("employeeId", employeeId);
        }
        return spec.map((row, metadata) -> mapDocument(row)).all();
    }
    @Override
    public Mono<EmploymentHistoryView> createEmploymentHistory(String tenantId, UUID employeeId, EmploymentHistoryUpsertRequest request) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_employment_history(
                    employment_history_id, tenant_id, employee_id, action_type,
                    old_department_id, new_department_id, old_designation_id, new_designation_id,
                    old_location_id, new_location_id, old_grade_id, new_grade_id, old_manager_id, new_manager_id,
                    effective_date, remarks, supporting_document_path, is_active, created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :id, :tenantId, :employeeId, :actionType,
                    :oldDepartmentId, :newDepartmentId, :oldDesignationId, :newDesignationId,
                    :oldLocationId, :newLocationId, :oldGradeId, :newGradeId, :oldManagerId, :newManagerId,
                    :effectiveDate, :remarks, :supportingDocumentPath, :isActive, :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("actionType", request.actionType().name())
                .bind("effectiveDate", request.effectiveDate())
                .bind("isActive", request.active())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "oldDepartmentId", request.oldDepartmentId(), UUID.class);
        spec = bindNullable(spec, "newDepartmentId", request.newDepartmentId(), UUID.class);
        spec = bindNullable(spec, "oldDesignationId", request.oldDesignationId(), UUID.class);
        spec = bindNullable(spec, "newDesignationId", request.newDesignationId(), UUID.class);
        spec = bindNullable(spec, "oldLocationId", request.oldLocationId(), UUID.class);
        spec = bindNullable(spec, "newLocationId", request.newLocationId(), UUID.class);
        spec = bindNullable(spec, "oldGradeId", request.oldGradeId(), UUID.class);
        spec = bindNullable(spec, "newGradeId", request.newGradeId(), UUID.class);
        spec = bindNullable(spec, "oldManagerId", request.oldManagerId(), UUID.class);
        spec = bindNullable(spec, "newManagerId", request.newManagerId(), UUID.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        spec = bindNullable(spec, "supportingDocumentPath", request.supportingDocumentPath(), String.class);
        return spec.map((row, metadata) -> mapEmploymentHistory(row)).one();
    }

    @Override
    public Mono<EmploymentHistoryView> findEmploymentHistoryRecord(String tenantId, UUID employeeId, UUID employmentHistoryId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_employment_history
                WHERE tenant_id = :tenantId AND employee_id = :employeeId AND employment_history_id = :employmentHistoryId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("employmentHistoryId", employmentHistoryId)
                .map((row, metadata) -> mapEmploymentHistory(row))
                .one();
    }

    @Override
    public Flux<EmploymentHistoryView> findEmploymentHistoryByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_employment_history
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                ORDER BY effective_date DESC, created_at DESC
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapEmploymentHistory(row))
                .all();
    }

    @Override
    public Mono<DigitalOnboardingView> createOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status) {
        UUID onboardingId = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_digital_onboarding(
                    onboarding_id, tenant_id, employee_id, onboarding_status, eform_completed, document_upload_completed, policy_acknowledged,
                    submitted_date, reviewed_date, approved_date, rejected_reason, remarks,
                    created_at, created_by, updated_at, updated_by
                ) VALUES (
                    :onboardingId, :tenantId, :employeeId, :status, :eformCompleted, :documentUploadCompleted, :policyAcknowledged,
                    :submittedDate, :reviewedDate, :approvedDate, :rejectedReason, :remarks,
                    :createdAt, :createdBy, :updatedAt, :updatedBy
                ) RETURNING *
                """)
                .bind("onboardingId", onboardingId)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("status", status.name())
                .bind("eformCompleted", request.eFormCompleted())
                .bind("documentUploadCompleted", request.documentUploadCompleted())
                .bind("policyAcknowledged", request.policyAcknowledged())
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "submittedDate", status == DigitalOnboardingStatus.SUBMITTED ? now : null, Instant.class);
        spec = bindNullable(spec, "reviewedDate", null, Instant.class);
        spec = bindNullable(spec, "approvedDate", null, Instant.class);
        spec = bindNullable(spec, "rejectedReason", null, String.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.fetch().rowsUpdated().then(findOnboardingByEmployeeId(tenantId, employeeId));
    }

    @Override
    public Mono<DigitalOnboardingView> updateOnboarding(String tenantId, UUID employeeId, DigitalOnboardingUpsertRequest request, DigitalOnboardingStatus status, String rejectedReason) {
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                UPDATE employee.employee_digital_onboarding
                SET onboarding_status = :status,
                    eform_completed = :eformCompleted,
                    document_upload_completed = :documentUploadCompleted,
                    policy_acknowledged = :policyAcknowledged,
                    submitted_date = :submittedDate,
                    reviewed_date = :reviewedDate,
                    approved_date = :approvedDate,
                    rejected_reason = :rejectedReason,
                    remarks = :remarks,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                RETURNING *
                """)
                .bind("status", status.name())
                .bind("eformCompleted", request.eFormCompleted())
                .bind("documentUploadCompleted", request.documentUploadCompleted())
                .bind("policyAcknowledged", request.policyAcknowledged())
                .bind("updatedAt", now)
                .bind("updatedBy", StringUtils.hasText(request.actor()) ? request.actor() : "system")
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId);
        spec = bindNullable(spec, "submittedDate", status == DigitalOnboardingStatus.SUBMITTED ? now : null, Instant.class);
        spec = bindNullable(spec, "reviewedDate", status == DigitalOnboardingStatus.UNDER_REVIEW ? now : null, Instant.class);
        spec = bindNullable(spec, "approvedDate", (status == DigitalOnboardingStatus.APPROVED || status == DigitalOnboardingStatus.COMPLETED) ? now : null, Instant.class);
        spec = bindNullable(spec, "rejectedReason", rejectedReason, String.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.fetch().rowsUpdated().then(findOnboardingByEmployeeId(tenantId, employeeId));
    }

    @Override
    public Mono<DigitalOnboardingView> findOnboardingByEmployeeId(String tenantId, UUID employeeId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_digital_onboarding
                WHERE tenant_id = :tenantId AND employee_id = :employeeId
                """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .map((row, metadata) -> mapOnboardingBase(row))
                .one()
                .flatMap(base -> findPolicyAcknowledgements(tenantId, base.onboardingId())
                        .collectList()
                        .map(acks -> withAcknowledgements(base, acks)));
    }

    @Override
    public Mono<PolicyAcknowledgementView> createPolicyAcknowledgement(String tenantId, UUID onboardingId, PolicyAcknowledgementRequest request) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO employee.employee_policy_acknowledgements(
                    acknowledgement_id, tenant_id, onboarding_id, policy_code, policy_version, accepted_flag,
                    accepted_date_time, accepted_by, remarks, created_at, created_by
                ) VALUES (
                    :id, :tenantId, :onboardingId, :policyCode, :policyVersion, :acceptedFlag,
                    :acceptedDateTime, :acceptedBy, :remarks, :createdAt, :createdBy
                ) RETURNING *
                """)
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("onboardingId", onboardingId)
                .bind("policyCode", request.policyCode())
                .bind("policyVersion", request.policyVersion())
                .bind("acceptedFlag", request.acceptedFlag())
                .bind("acceptedDateTime", now)
                .bind("createdAt", now)
                .bind("createdBy", StringUtils.hasText(request.actor()) ? request.actor() : "system");
        spec = bindNullable(spec, "acceptedBy", request.acceptedBy(), String.class);
        spec = bindNullable(spec, "remarks", request.remarks(), String.class);
        return spec.map((row, metadata) -> mapPolicyAcknowledgement(row)).one();
    }

    @Override
    public Flux<PolicyAcknowledgementView> findPolicyAcknowledgements(String tenantId, UUID onboardingId) {
        return databaseClient.sql("""
                SELECT * FROM employee.employee_policy_acknowledgements
                WHERE tenant_id = :tenantId AND onboarding_id = :onboardingId
                ORDER BY accepted_date_time DESC
                """)
                .bind("tenantId", tenantId)
                .bind("onboardingId", onboardingId)
                .map((row, metadata) -> mapPolicyAcknowledgement(row))
                .all();
    }
}
