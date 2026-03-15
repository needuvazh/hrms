package com.company.hrms.organization.repository;

import com.company.hrms.organization.model.OrganizationModels;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class OrganizationR2dbcRepository implements OrganizationRepository {

    private final DatabaseClient databaseClient;

    public OrganizationR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> createLegalEntity(OrganizationModels.LegalEntityDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.legal_entities(
                            id, tenant_id, legal_entity_code, legal_entity_name, short_name, registration_no, tax_no,
                            country_code, base_currency_code, default_language_code, contact_email, contact_phone,
                            address_line1, address_line2, city, state, postal_code, active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :code, :name, :shortName, :registrationNo, :taxNo,
                            :countryCode, :baseCurrencyCode, :defaultLanguageCode, :contactEmail, :contactPhone,
                            :addressLine1, :addressLine2, :city, :state, :postalCode, :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        )
                        RETURNING *
                        """)
                .bind("id", dto.id())
                .bind("tenantId", dto.tenantId())
                .bind("code", dto.legalEntityCode())
                .bind("name", dto.legalEntityName())
                .bind("active", dto.active())
                .bind("createdAt", dto.createdAt())
                .bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy())
                .bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "shortName", dto.shortName(), String.class);
        spec = bindNullable(spec, "registrationNo", dto.registrationNo(), String.class);
        spec = bindNullable(spec, "taxNo", dto.taxNo(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "baseCurrencyCode", dto.baseCurrencyCode(), String.class);
        spec = bindNullable(spec, "defaultLanguageCode", dto.defaultLanguageCode(), String.class);
        spec = bindNullable(spec, "contactEmail", dto.contactEmail(), String.class);
        spec = bindNullable(spec, "contactPhone", dto.contactPhone(), String.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        return spec.map((row, meta) -> mapLegalEntity(row)).one();
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> updateLegalEntity(OrganizationModels.LegalEntityDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.legal_entities
                        SET legal_entity_code = :code,
                            legal_entity_name = :name,
                            short_name = :shortName,
                            registration_no = :registrationNo,
                            tax_no = :taxNo,
                            country_code = :countryCode,
                            base_currency_code = :baseCurrencyCode,
                            default_language_code = :defaultLanguageCode,
                            contact_email = :contactEmail,
                            contact_phone = :contactPhone,
                            address_line1 = :addressLine1,
                            address_line2 = :addressLine2,
                            city = :city,
                            state = :state,
                            postal_code = :postalCode,
                            active = :active,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE id = :id AND tenant_id = :tenantId
                        RETURNING *
                        """)
                .bind("id", dto.id())
                .bind("tenantId", dto.tenantId())
                .bind("code", dto.legalEntityCode())
                .bind("name", dto.legalEntityName())
                .bind("active", dto.active())
                .bind("updatedAt", dto.updatedAt())
                .bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "shortName", dto.shortName(), String.class);
        spec = bindNullable(spec, "registrationNo", dto.registrationNo(), String.class);
        spec = bindNullable(spec, "taxNo", dto.taxNo(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "baseCurrencyCode", dto.baseCurrencyCode(), String.class);
        spec = bindNullable(spec, "defaultLanguageCode", dto.defaultLanguageCode(), String.class);
        spec = bindNullable(spec, "contactEmail", dto.contactEmail(), String.class);
        spec = bindNullable(spec, "contactPhone", dto.contactPhone(), String.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        return spec.map((row, meta) -> mapLegalEntity(row)).one();
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> findLegalEntityById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.legal_entities WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, meta) -> mapLegalEntity(row)).one();
    }

    @Override
    public Flux<OrganizationModels.LegalEntityDto> searchLegalEntities(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns(
                "organization.legal_entities",
                "legal_entity_code",
                "legal_entity_name",
                tenantId,
                query,
                this::mapLegalEntity);
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> updateLegalEntityStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.legal_entities", tenantId, id, active, actor).map((row, meta) -> mapLegalEntity(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> legalEntityOptions(String tenantId, String q, int limit) {
        return options("organization.legal_entities", "legal_entity_code", "legal_entity_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.BranchDto> createBranch(OrganizationModels.BranchDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.branches(
                            id, tenant_id, legal_entity_id, branch_code, branch_name, branch_short_name,
                            address_line1, address_line2, city, state, country_code, postal_code, phone, fax, email,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :code, :name, :shortName,
                            :addressLine1, :addressLine2, :city, :state, :countryCode, :postalCode, :phone, :fax, :email,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId()).bind("legalEntityId", dto.legalEntityId())
                .bind("code", dto.branchCode()).bind("name", dto.branchName()).bind("active", dto.active())
                .bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "shortName", dto.branchShortName(), String.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        spec = bindNullable(spec, "phone", dto.phone(), String.class);
        spec = bindNullable(spec, "fax", dto.fax(), String.class);
        spec = bindNullable(spec, "email", dto.email(), String.class);
        return spec.map((row, meta) -> mapBranch(row)).one();
    }

    @Override
    public Mono<OrganizationModels.BranchDto> updateBranch(OrganizationModels.BranchDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.branches
                           SET legal_entity_id = :legalEntityId,
                               branch_code = :code,
                               branch_name = :name,
                               branch_short_name = :shortName,
                               address_line1 = :addressLine1,
                               address_line2 = :addressLine2,
                               city = :city,
                               state = :state,
                               country_code = :countryCode,
                               postal_code = :postalCode,
                               phone = :phone,
                               fax = :fax,
                               email = :email,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId()).bind("legalEntityId", dto.legalEntityId())
                .bind("code", dto.branchCode()).bind("name", dto.branchName()).bind("active", dto.active())
                .bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "shortName", dto.branchShortName(), String.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        spec = bindNullable(spec, "phone", dto.phone(), String.class);
        spec = bindNullable(spec, "fax", dto.fax(), String.class);
        spec = bindNullable(spec, "email", dto.email(), String.class);
        return spec.map((row, meta) -> mapBranch(row)).one();
    }

    @Override
    public Mono<OrganizationModels.BranchDto> findBranchById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.branches WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapBranch(row)).one();
    }

    @Override
    public Flux<OrganizationModels.BranchDto> searchBranches(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.branches", "branch_code", "branch_name", tenantId, query, this::mapBranch);
    }

    @Override
    public Mono<OrganizationModels.BranchDto> updateBranchStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.branches", tenantId, id, active, actor).map((row, meta) -> mapBranch(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> branchOptions(String tenantId, String q, int limit) {
        return options("organization.branches", "branch_code", "branch_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> createBusinessUnit(OrganizationModels.BusinessUnitDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.business_units(
                            id, tenant_id, legal_entity_id, business_unit_code, business_unit_name, description,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :code, :name, :description,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.businessUnitCode()).bind("name", dto.businessUnitName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapBusinessUnit(row)).one();
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnit(OrganizationModels.BusinessUnitDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.business_units
                           SET legal_entity_id = :legalEntityId,
                               business_unit_code = :code,
                               business_unit_name = :name,
                               description = :description,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.businessUnitCode()).bind("name", dto.businessUnitName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapBusinessUnit(row)).one();
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> findBusinessUnitById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.business_units WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapBusinessUnit(row)).one();
    }

    @Override
    public Flux<OrganizationModels.BusinessUnitDto> searchBusinessUnits(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.business_units", "business_unit_code", "business_unit_name", tenantId, query, this::mapBusinessUnit);
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnitStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.business_units", tenantId, id, active, actor).map((row, meta) -> mapBusinessUnit(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> businessUnitOptions(String tenantId, String q, int limit) {
        return options("organization.business_units", "business_unit_code", "business_unit_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> createDivision(OrganizationModels.DivisionDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.divisions(
                            id, tenant_id, legal_entity_id, business_unit_id, branch_id, division_code, division_name,
                            description, active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :businessUnitId, :branchId, :code, :name,
                            :description, :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.divisionCode()).bind("name", dto.divisionName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "businessUnitId", dto.businessUnitId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapDivision(row)).one();
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> updateDivision(OrganizationModels.DivisionDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.divisions
                           SET legal_entity_id = :legalEntityId,
                               business_unit_id = :businessUnitId,
                               branch_id = :branchId,
                               division_code = :code,
                               division_name = :name,
                               description = :description,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.divisionCode()).bind("name", dto.divisionName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "businessUnitId", dto.businessUnitId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapDivision(row)).one();
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> findDivisionById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.divisions WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapDivision(row)).one();
    }

    @Override
    public Flux<OrganizationModels.DivisionDto> searchDivisions(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.divisions", "division_code", "division_name", tenantId, query, this::mapDivision);
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> updateDivisionStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.divisions", tenantId, id, active, actor).map((row, meta) -> mapDivision(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> divisionOptions(String tenantId, String q, int limit) {
        return options("organization.divisions", "division_code", "division_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> createDepartment(OrganizationModels.DepartmentDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.departments(
                            id, tenant_id, legal_entity_id, business_unit_id, division_id, branch_id,
                            department_code, department_name, short_name, description,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :businessUnitId, :divisionId, :branchId,
                            :code, :name, :shortName, :description,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.departmentCode()).bind("name", dto.departmentName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "businessUnitId", dto.businessUnitId(), UUID.class);
        spec = bindNullable(spec, "divisionId", dto.divisionId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "shortName", dto.shortName(), String.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapDepartment(row)).one();
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> updateDepartment(OrganizationModels.DepartmentDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.departments
                           SET legal_entity_id = :legalEntityId,
                               business_unit_id = :businessUnitId,
                               division_id = :divisionId,
                               branch_id = :branchId,
                               department_code = :code,
                               department_name = :name,
                               short_name = :shortName,
                               description = :description,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.departmentCode()).bind("name", dto.departmentName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "businessUnitId", dto.businessUnitId(), UUID.class);
        spec = bindNullable(spec, "divisionId", dto.divisionId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "shortName", dto.shortName(), String.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapDepartment(row)).one();
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> findDepartmentById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.departments WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapDepartment(row)).one();
    }

    @Override
    public Flux<OrganizationModels.DepartmentDto> searchDepartments(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.departments", "department_code", "department_name", tenantId, query, this::mapDepartment);
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> updateDepartmentStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.departments", tenantId, id, active, actor).map((row, meta) -> mapDepartment(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> departmentOptions(String tenantId, String q, int limit) {
        return options("organization.departments", "department_code", "department_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.SectionDto> createSection(OrganizationModels.SectionDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.sections(
                            id, tenant_id, department_id, section_code, section_name, description,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :departmentId, :code, :name, :description,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId()).bind("departmentId", dto.departmentId())
                .bind("code", dto.sectionCode()).bind("name", dto.sectionName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapSection(row)).one();
    }

    @Override
    public Mono<OrganizationModels.SectionDto> updateSection(OrganizationModels.SectionDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.sections
                           SET department_id = :departmentId,
                               section_code = :code,
                               section_name = :name,
                               description = :description,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId()).bind("departmentId", dto.departmentId())
                .bind("code", dto.sectionCode()).bind("name", dto.sectionName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapSection(row)).one();
    }

    @Override
    public Mono<OrganizationModels.SectionDto> findSectionById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.sections WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapSection(row)).one();
    }

    @Override
    public Flux<OrganizationModels.SectionDto> searchSections(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.sections", "section_code", "section_name", tenantId, query, this::mapSection);
    }

    @Override
    public Mono<OrganizationModels.SectionDto> updateSectionStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.sections", tenantId, id, active, actor).map((row, meta) -> mapSection(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> sectionOptions(String tenantId, String q, int limit) {
        return options("organization.sections", "section_code", "section_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> createWorkLocation(OrganizationModels.WorkLocationDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.work_locations(
                            id, tenant_id, legal_entity_id, branch_id, location_code, location_name, location_type,
                            address_line1, address_line2, city, state, country_code, postal_code,
                            latitude, longitude, geofence_radius,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :branchId, :code, :name, :locationType,
                            :addressLine1, :addressLine2, :city, :state, :countryCode, :postalCode,
                            :latitude, :longitude, :geofenceRadius,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.locationCode()).bind("name", dto.locationName())
                .bind("locationType", dto.locationType().name())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        spec = bindNullable(spec, "latitude", dto.latitude(), BigDecimal.class);
        spec = bindNullable(spec, "longitude", dto.longitude(), BigDecimal.class);
        spec = bindNullable(spec, "geofenceRadius", dto.geofenceRadius(), BigDecimal.class);
        return spec.map((row, meta) -> mapWorkLocation(row)).one();
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> updateWorkLocation(OrganizationModels.WorkLocationDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.work_locations
                           SET legal_entity_id = :legalEntityId,
                               branch_id = :branchId,
                               location_code = :code,
                               location_name = :name,
                               location_type = :locationType,
                               address_line1 = :addressLine1,
                               address_line2 = :addressLine2,
                               city = :city,
                               state = :state,
                               country_code = :countryCode,
                               postal_code = :postalCode,
                               latitude = :latitude,
                               longitude = :longitude,
                               geofence_radius = :geofenceRadius,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.locationCode()).bind("name", dto.locationName())
                .bind("locationType", dto.locationType().name())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "branchId", dto.branchId(), UUID.class);
        spec = bindNullable(spec, "addressLine1", dto.addressLine1(), String.class);
        spec = bindNullable(spec, "addressLine2", dto.addressLine2(), String.class);
        spec = bindNullable(spec, "city", dto.city(), String.class);
        spec = bindNullable(spec, "state", dto.state(), String.class);
        spec = bindNullable(spec, "countryCode", dto.countryCode(), String.class);
        spec = bindNullable(spec, "postalCode", dto.postalCode(), String.class);
        spec = bindNullable(spec, "latitude", dto.latitude(), BigDecimal.class);
        spec = bindNullable(spec, "longitude", dto.longitude(), BigDecimal.class);
        spec = bindNullable(spec, "geofenceRadius", dto.geofenceRadius(), BigDecimal.class);
        return spec.map((row, meta) -> mapWorkLocation(row)).one();
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> findWorkLocationById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.work_locations WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapWorkLocation(row)).one();
    }

    @Override
    public Flux<OrganizationModels.WorkLocationDto> searchWorkLocations(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.work_locations", "location_code", "location_name", tenantId, query, this::mapWorkLocation);
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> updateWorkLocationStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.work_locations", tenantId, id, active, actor).map((row, meta) -> mapWorkLocation(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> workLocationOptions(String tenantId, String q, int limit) {
        return options("organization.work_locations", "location_code", "location_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> createCostCenter(OrganizationModels.CostCenterDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.cost_centers(
                            id, tenant_id, legal_entity_id, cost_center_code, cost_center_name, description,
                            gl_account_code, parent_cost_center_id,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :legalEntityId, :code, :name, :description,
                            :glAccountCode, :parentCostCenterId,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.costCenterCode()).bind("name", dto.costCenterName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        spec = bindNullable(spec, "glAccountCode", dto.glAccountCode(), String.class);
        spec = bindNullable(spec, "parentCostCenterId", dto.parentCostCenterId(), UUID.class);
        return spec.map((row, meta) -> mapCostCenter(row)).one();
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> updateCostCenter(OrganizationModels.CostCenterDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.cost_centers
                           SET legal_entity_id = :legalEntityId,
                               cost_center_code = :code,
                               cost_center_name = :name,
                               description = :description,
                               gl_account_code = :glAccountCode,
                               parent_cost_center_id = :parentCostCenterId,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.costCenterCode()).bind("name", dto.costCenterName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "legalEntityId", dto.legalEntityId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        spec = bindNullable(spec, "glAccountCode", dto.glAccountCode(), String.class);
        spec = bindNullable(spec, "parentCostCenterId", dto.parentCostCenterId(), UUID.class);
        return spec.map((row, meta) -> mapCostCenter(row)).one();
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> findCostCenterById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.cost_centers WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapCostCenter(row)).one();
    }

    @Override
    public Flux<OrganizationModels.CostCenterDto> searchCostCenters(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.cost_centers", "cost_center_code", "cost_center_name", tenantId, query, this::mapCostCenter);
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> updateCostCenterStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.cost_centers", tenantId, id, active, actor).map((row, meta) -> mapCostCenter(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> costCenterOptions(String tenantId, String q, int limit) {
        return options("organization.cost_centers", "cost_center_code", "cost_center_name", tenantId, q, limit);
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> createReportingUnit(OrganizationModels.ReportingUnitDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO organization.reporting_units(
                            id, tenant_id, reporting_unit_code, reporting_unit_name,
                            parent_reporting_unit_id, description,
                            active, created_at, updated_at, created_by, updated_by
                        ) VALUES (
                            :id, :tenantId, :code, :name,
                            :parentReportingUnitId, :description,
                            :active, :createdAt, :updatedAt, :createdBy, :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.reportingUnitCode()).bind("name", dto.reportingUnitName())
                .bind("active", dto.active()).bind("createdAt", dto.createdAt()).bind("updatedAt", dto.updatedAt())
                .bind("createdBy", dto.createdBy()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "parentReportingUnitId", dto.parentReportingUnitId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapReportingUnit(row)).one();
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> updateReportingUnit(OrganizationModels.ReportingUnitDto dto) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE organization.reporting_units
                           SET reporting_unit_code = :code,
                               reporting_unit_name = :name,
                               parent_reporting_unit_id = :parentReportingUnitId,
                               description = :description,
                               active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """)
                .bind("id", dto.id()).bind("tenantId", dto.tenantId())
                .bind("code", dto.reportingUnitCode()).bind("name", dto.reportingUnitName())
                .bind("active", dto.active()).bind("updatedAt", dto.updatedAt()).bind("updatedBy", dto.updatedBy());
        spec = bindNullable(spec, "parentReportingUnitId", dto.parentReportingUnitId(), UUID.class);
        spec = bindNullable(spec, "description", dto.description(), String.class);
        return spec.map((row, meta) -> mapReportingUnit(row)).one();
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> findReportingUnitById(String tenantId, UUID id) {
        return databaseClient.sql("SELECT * FROM organization.reporting_units WHERE tenant_id = :tenantId AND id = :id")
                .bind("tenantId", tenantId).bind("id", id)
                .map((row, meta) -> mapReportingUnit(row)).one();
    }

    @Override
    public Flux<OrganizationModels.ReportingUnitDto> searchReportingUnits(String tenantId, OrganizationModels.SearchQuery query) {
        return searchTwoColumns("organization.reporting_units", "reporting_unit_code", "reporting_unit_name", tenantId, query, this::mapReportingUnit);
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> updateReportingUnitStatus(String tenantId, UUID id, boolean active, String actor) {
        return updateStatus("organization.reporting_units", tenantId, id, active, actor).map((row, meta) -> mapReportingUnit(row)).one();
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> reportingUnitOptions(String tenantId, String q, int limit) {
        return options("organization.reporting_units", "reporting_unit_code", "reporting_unit_name", tenantId, q, limit);
    }

    private <T> Flux<T> searchTwoColumns(
            String table,
            String codeColumn,
            String nameColumn,
            String tenantId,
            OrganizationModels.SearchQuery query,
            java.util.function.Function<io.r2dbc.spi.Row, T> mapper
    ) {
        String like = StringUtils.hasText(query.q()) ? "%" + query.q().trim() + "%" : "%";
        Integer activeFlag = query.active() == null ? null : (query.active() ? 1 : 0);
        GenericExecuteSpec spec = databaseClient.sql("""
                        SELECT * FROM %s
                        WHERE tenant_id = :tenantId
                          AND (lower(%s) LIKE lower(:q) OR lower(%s) LIKE lower(:q))
                          AND (:activeFlag IS NULL OR active = (:activeFlag = 1))
                        ORDER BY created_at DESC
                        LIMIT :limit OFFSET :offset
                        """.formatted(table, codeColumn, nameColumn))
                .bind("tenantId", tenantId)
                .bind("q", like)
                .bind("limit", query.limit())
                .bind("offset", query.offset());
        spec = activeFlag == null ? spec.bindNull("activeFlag", Integer.class) : spec.bind("activeFlag", activeFlag);
        return spec.map((row, meta) -> mapper.apply(row)).all();
    }

    private Flux<OrganizationModels.OptionViewDto> options(String table, String codeColumn, String nameColumn, String tenantId, String q, int limit) {
        String like = StringUtils.hasText(q) ? "%" + q.trim() + "%" : "%";
        return databaseClient.sql("""
                        SELECT id, %s AS code, %s AS name
                          FROM %s
                         WHERE tenant_id = :tenantId
                           AND active = true
                           AND (lower(%s) LIKE lower(:q) OR lower(%s) LIKE lower(:q))
                         ORDER BY %s ASC
                         LIMIT :limit
                        """.formatted(codeColumn, nameColumn, table, codeColumn, nameColumn, nameColumn))
                .bind("tenantId", tenantId)
                .bind("q", like)
                .bind("limit", limit)
                .map((row, meta) -> new OrganizationModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    private GenericExecuteSpec updateStatus(String table, String tenantId, UUID id, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE %s
                           SET active = :active,
                               updated_at = :updatedAt,
                               updated_by = :updatedBy
                         WHERE tenant_id = :tenantId AND id = :id
                         RETURNING *
                        """.formatted(table))
                .bind("active", active)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", actor)
                .bind("tenantId", tenantId)
                .bind("id", id);
    }

    private OrganizationModels.LegalEntityDto mapLegalEntity(io.r2dbc.spi.Row row) {
        return new OrganizationModels.LegalEntityDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_code", String.class),
                row.get("legal_entity_name", String.class),
                row.get("short_name", String.class),
                row.get("registration_no", String.class),
                row.get("tax_no", String.class),
                row.get("country_code", String.class),
                row.get("base_currency_code", String.class),
                row.get("default_language_code", String.class),
                row.get("contact_email", String.class),
                row.get("contact_phone", String.class),
                row.get("address_line1", String.class),
                row.get("address_line2", String.class),
                row.get("city", String.class),
                row.get("state", String.class),
                row.get("postal_code", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.BranchDto mapBranch(io.r2dbc.spi.Row row) {
        return new OrganizationModels.BranchDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("branch_code", String.class),
                row.get("branch_name", String.class),
                row.get("branch_short_name", String.class),
                row.get("address_line1", String.class),
                row.get("address_line2", String.class),
                row.get("city", String.class),
                row.get("state", String.class),
                row.get("country_code", String.class),
                row.get("postal_code", String.class),
                row.get("phone", String.class),
                row.get("fax", String.class),
                row.get("email", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.BusinessUnitDto mapBusinessUnit(io.r2dbc.spi.Row row) {
        return new OrganizationModels.BusinessUnitDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("business_unit_code", String.class),
                row.get("business_unit_name", String.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.DivisionDto mapDivision(io.r2dbc.spi.Row row) {
        return new OrganizationModels.DivisionDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("business_unit_id", UUID.class),
                row.get("branch_id", UUID.class),
                row.get("division_code", String.class),
                row.get("division_name", String.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.DepartmentDto mapDepartment(io.r2dbc.spi.Row row) {
        return new OrganizationModels.DepartmentDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("business_unit_id", UUID.class),
                row.get("division_id", UUID.class),
                row.get("branch_id", UUID.class),
                row.get("department_code", String.class),
                row.get("department_name", String.class),
                row.get("short_name", String.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.SectionDto mapSection(io.r2dbc.spi.Row row) {
        return new OrganizationModels.SectionDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("department_id", UUID.class),
                row.get("section_code", String.class),
                row.get("section_name", String.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.WorkLocationDto mapWorkLocation(io.r2dbc.spi.Row row) {
        String locationType = row.get("location_type", String.class);
        return new OrganizationModels.WorkLocationDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("branch_id", UUID.class),
                row.get("location_code", String.class),
                row.get("location_name", String.class),
                OrganizationModels.LocationType.valueOf(locationType),
                row.get("address_line1", String.class),
                row.get("address_line2", String.class),
                row.get("city", String.class),
                row.get("state", String.class),
                row.get("country_code", String.class),
                row.get("postal_code", String.class),
                row.get("latitude", BigDecimal.class),
                row.get("longitude", BigDecimal.class),
                row.get("geofence_radius", BigDecimal.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.CostCenterDto mapCostCenter(io.r2dbc.spi.Row row) {
        return new OrganizationModels.CostCenterDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("legal_entity_id", UUID.class),
                row.get("cost_center_code", String.class),
                row.get("cost_center_name", String.class),
                row.get("description", String.class),
                row.get("gl_account_code", String.class),
                row.get("parent_cost_center_id", UUID.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private OrganizationModels.ReportingUnitDto mapReportingUnit(io.r2dbc.spi.Row row) {
        return new OrganizationModels.ReportingUnitDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get("reporting_unit_code", String.class),
                row.get("reporting_unit_name", String.class),
                row.get("parent_reporting_unit_id", UUID.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
