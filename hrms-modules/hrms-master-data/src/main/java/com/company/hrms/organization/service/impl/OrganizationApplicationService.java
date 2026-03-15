package com.company.hrms.organization.service.impl;

import com.company.hrms.organization.model.OrganizationModels;
import com.company.hrms.organization.repository.OrganizationRepository;
import com.company.hrms.organization.service.OrganizationModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrganizationApplicationService implements OrganizationModuleApi {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9()\\-\\s]{6,30}$");

    private final OrganizationRepository organizationRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;

    public OrganizationApplicationService(
            OrganizationRepository organizationRepository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher
    ) {
        this.organizationRepository = organizationRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> createLegalEntity(OrganizationModels.LegalEntityUpsertCommand command) {
        validateLegalEntity(command);
        return withTenant("organization")
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    OrganizationModels.LegalEntityDto dto = new OrganizationModels.LegalEntityDto(
                            UUID.randomUUID(), tenantId, normalize(command.legalEntityCode()), command.legalEntityName().trim(),
                            trimToNull(command.shortName()), trimToNull(command.registrationNo()), trimToNull(command.taxNo()),
                            trimToNull(command.countryCode()), trimToNull(command.baseCurrencyCode()), trimToNull(command.defaultLanguageCode()),
                            trimToNull(command.contactEmail()), trimToNull(command.contactPhone()),
                            trimToNull(command.addressLine1()), trimToNull(command.addressLine2()), trimToNull(command.city()),
                            trimToNull(command.state()), trimToNull(command.postalCode()), command.active(), now, now, "system", "system");
                    return organizationRepository.createLegalEntity(dto)
                            .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("LEGAL_ENTITY_CODE_ALREADY_EXISTS", "Legal entity code already exists"))
                            .flatMap(saved -> audit("LEGAL_ENTITY_CREATED", "LEGAL_ENTITY", saved.id().toString(), tenantId, Map.of("code", saved.legalEntityCode())).thenReturn(saved));
                });
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> updateLegalEntity(UUID id, OrganizationModels.LegalEntityUpsertCommand command) {
        validateLegalEntity(command);
        return withTenant("organization")
                .flatMap(tenantId -> organizationRepository.findLegalEntityById(tenantId, id)
                        .switchIfEmpty(notFound("LEGAL_ENTITY_NOT_FOUND", "Legal entity not found"))
                        .flatMap(existing -> organizationRepository.updateLegalEntity(new OrganizationModels.LegalEntityDto(
                                        existing.id(), tenantId, normalize(command.legalEntityCode()), command.legalEntityName().trim(),
                                        trimToNull(command.shortName()), trimToNull(command.registrationNo()), trimToNull(command.taxNo()),
                                        trimToNull(command.countryCode()), trimToNull(command.baseCurrencyCode()), trimToNull(command.defaultLanguageCode()),
                                        trimToNull(command.contactEmail()), trimToNull(command.contactPhone()),
                                        trimToNull(command.addressLine1()), trimToNull(command.addressLine2()), trimToNull(command.city()),
                                        trimToNull(command.state()), trimToNull(command.postalCode()), command.active(), existing.createdAt(), Instant.now(),
                                        existing.createdBy(), "system"))
                                .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("LEGAL_ENTITY_CODE_ALREADY_EXISTS", "Legal entity code already exists"))
                                .flatMap(saved -> audit("LEGAL_ENTITY_UPDATED", "LEGAL_ENTITY", saved.id().toString(), tenantId, Map.of("code", saved.legalEntityCode())).thenReturn(saved))));
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> getLegalEntity(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findLegalEntityById(tenantId, id)
                .switchIfEmpty(notFound("LEGAL_ENTITY_NOT_FOUND", "Legal entity not found")));
    }

    @Override
    public Flux<OrganizationModels.LegalEntityDto> searchLegalEntities(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchLegalEntities(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.LegalEntityDto> updateLegalEntityStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateLegalEntityStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("LEGAL_ENTITY_NOT_FOUND", "Legal entity not found"))
                .flatMap(saved -> audit("LEGAL_ENTITY_STATUS_UPDATED", "LEGAL_ENTITY", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> legalEntityOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.legalEntityOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.BranchDto> createBranch(OrganizationModels.BranchUpsertCommand command) {
        validateBranch(command);
        return withTenant("organization").flatMap(tenantId -> requireLegalEntity(tenantId, command.legalEntityId())
                .then(Mono.defer(() -> {
                    Instant now = Instant.now();
                    OrganizationModels.BranchDto dto = new OrganizationModels.BranchDto(
                            UUID.randomUUID(), tenantId, command.legalEntityId(), normalize(command.branchCode()), command.branchName().trim(),
                            trimToNull(command.branchShortName()), trimToNull(command.addressLine1()), trimToNull(command.addressLine2()),
                            trimToNull(command.city()), trimToNull(command.state()), trimToNull(command.countryCode()), trimToNull(command.postalCode()),
                            trimToNull(command.phone()), trimToNull(command.fax()), trimToNull(command.email()), command.active(), now, now, "system", "system");
                    return organizationRepository.createBranch(dto)
                            .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("BRANCH_CODE_ALREADY_EXISTS", "Branch code already exists"))
                            .flatMap(saved -> audit("BRANCH_CREATED", "BRANCH", saved.id().toString(), tenantId, Map.of("code", saved.branchCode())).thenReturn(saved));
                })));
    }

    @Override
    public Mono<OrganizationModels.BranchDto> updateBranch(UUID id, OrganizationModels.BranchUpsertCommand command) {
        validateBranch(command);
        return withTenant("organization")
                .flatMap(tenantId -> requireLegalEntity(tenantId, command.legalEntityId()).then(
                        organizationRepository.findBranchById(tenantId, id)
                                .switchIfEmpty(notFound("BRANCH_NOT_FOUND", "Branch not found"))
                                .flatMap(existing -> organizationRepository.updateBranch(new OrganizationModels.BranchDto(
                                                existing.id(), tenantId, command.legalEntityId(), normalize(command.branchCode()), command.branchName().trim(),
                                                trimToNull(command.branchShortName()), trimToNull(command.addressLine1()), trimToNull(command.addressLine2()),
                                                trimToNull(command.city()), trimToNull(command.state()), trimToNull(command.countryCode()), trimToNull(command.postalCode()),
                                                trimToNull(command.phone()), trimToNull(command.fax()), trimToNull(command.email()), command.active(),
                                                existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("BRANCH_CODE_ALREADY_EXISTS", "Branch code already exists"))
                                        .flatMap(saved -> audit("BRANCH_UPDATED", "BRANCH", saved.id().toString(), tenantId, Map.of("code", saved.branchCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.BranchDto> getBranch(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findBranchById(tenantId, id)
                .switchIfEmpty(notFound("BRANCH_NOT_FOUND", "Branch not found")));
    }

    @Override
    public Flux<OrganizationModels.BranchDto> searchBranches(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchBranches(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.BranchDto> updateBranchStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateBranchStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("BRANCH_NOT_FOUND", "Branch not found"))
                .flatMap(saved -> audit("BRANCH_STATUS_UPDATED", "BRANCH", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> branchOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.branchOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> createBusinessUnit(OrganizationModels.BusinessUnitUpsertCommand command) {
        validateBusinessUnit(command);
        return withTenant("organization").flatMap(tenantId -> optionalLegalEntity(tenantId, command.legalEntityId()).then(Mono.defer(() -> {
            Instant now = Instant.now();
            OrganizationModels.BusinessUnitDto dto = new OrganizationModels.BusinessUnitDto(
                    UUID.randomUUID(), tenantId, command.legalEntityId(), normalize(command.businessUnitCode()), command.businessUnitName().trim(),
                    trimToNull(command.description()), command.active(), now, now, "system", "system");
            return organizationRepository.createBusinessUnit(dto)
                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("BUSINESS_UNIT_CODE_ALREADY_EXISTS", "Business unit code already exists"))
                    .flatMap(saved -> audit("BUSINESS_UNIT_CREATED", "BUSINESS_UNIT", saved.id().toString(), tenantId, Map.of("code", saved.businessUnitCode())).thenReturn(saved));
        })));
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnit(UUID id, OrganizationModels.BusinessUnitUpsertCommand command) {
        validateBusinessUnit(command);
        return withTenant("organization").flatMap(tenantId -> optionalLegalEntity(tenantId, command.legalEntityId()).then(
                organizationRepository.findBusinessUnitById(tenantId, id)
                        .switchIfEmpty(notFound("BUSINESS_UNIT_NOT_FOUND", "Business unit not found"))
                        .flatMap(existing -> organizationRepository.updateBusinessUnit(new OrganizationModels.BusinessUnitDto(
                                        existing.id(), tenantId, command.legalEntityId(), normalize(command.businessUnitCode()), command.businessUnitName().trim(),
                                        trimToNull(command.description()), command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("BUSINESS_UNIT_CODE_ALREADY_EXISTS", "Business unit code already exists"))
                                .flatMap(saved -> audit("BUSINESS_UNIT_UPDATED", "BUSINESS_UNIT", saved.id().toString(), tenantId, Map.of("code", saved.businessUnitCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> getBusinessUnit(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findBusinessUnitById(tenantId, id)
                .switchIfEmpty(notFound("BUSINESS_UNIT_NOT_FOUND", "Business unit not found")));
    }

    @Override
    public Flux<OrganizationModels.BusinessUnitDto> searchBusinessUnits(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchBusinessUnits(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnitStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateBusinessUnitStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("BUSINESS_UNIT_NOT_FOUND", "Business unit not found"))
                .flatMap(saved -> audit("BUSINESS_UNIT_STATUS_UPDATED", "BUSINESS_UNIT", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> businessUnitOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.businessUnitOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> createDivision(OrganizationModels.DivisionUpsertCommand command) {
        validateDivision(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(
                                optionalLegalEntity(tenantId, command.legalEntityId()),
                                optionalBusinessUnit(tenantId, command.businessUnitId()),
                                optionalBranch(tenantId, command.branchId()))
                        .then(Mono.defer(() -> {
                            Instant now = Instant.now();
                            OrganizationModels.DivisionDto dto = new OrganizationModels.DivisionDto(
                                    UUID.randomUUID(), tenantId, command.legalEntityId(), command.businessUnitId(), command.branchId(),
                                    normalize(command.divisionCode()), command.divisionName().trim(), trimToNull(command.description()),
                                    command.active(), now, now, "system", "system");
                            return organizationRepository.createDivision(dto)
                                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("DIVISION_CODE_ALREADY_EXISTS", "Division code already exists"))
                                    .flatMap(saved -> audit("DIVISION_CREATED", "DIVISION", saved.id().toString(), tenantId, Map.of("code", saved.divisionCode())).thenReturn(saved));
                        })));
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> updateDivision(UUID id, OrganizationModels.DivisionUpsertCommand command) {
        validateDivision(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(
                                optionalLegalEntity(tenantId, command.legalEntityId()),
                                optionalBusinessUnit(tenantId, command.businessUnitId()),
                                optionalBranch(tenantId, command.branchId()))
                        .then(organizationRepository.findDivisionById(tenantId, id)
                                .switchIfEmpty(notFound("DIVISION_NOT_FOUND", "Division not found"))
                                .flatMap(existing -> organizationRepository.updateDivision(new OrganizationModels.DivisionDto(
                                                existing.id(), tenantId, command.legalEntityId(), command.businessUnitId(), command.branchId(),
                                                normalize(command.divisionCode()), command.divisionName().trim(), trimToNull(command.description()),
                                                command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("DIVISION_CODE_ALREADY_EXISTS", "Division code already exists"))
                                        .flatMap(saved -> audit("DIVISION_UPDATED", "DIVISION", saved.id().toString(), tenantId, Map.of("code", saved.divisionCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> getDivision(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findDivisionById(tenantId, id)
                .switchIfEmpty(notFound("DIVISION_NOT_FOUND", "Division not found")));
    }

    @Override
    public Flux<OrganizationModels.DivisionDto> searchDivisions(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchDivisions(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.DivisionDto> updateDivisionStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateDivisionStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("DIVISION_NOT_FOUND", "Division not found"))
                .flatMap(saved -> audit("DIVISION_STATUS_UPDATED", "DIVISION", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> divisionOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.divisionOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> createDepartment(OrganizationModels.DepartmentUpsertCommand command) {
        validateDepartment(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(
                                optionalLegalEntity(tenantId, command.legalEntityId()),
                                optionalBusinessUnit(tenantId, command.businessUnitId()),
                                optionalDivision(tenantId, command.divisionId()),
                                optionalBranch(tenantId, command.branchId()))
                        .then(Mono.defer(() -> {
                            Instant now = Instant.now();
                            OrganizationModels.DepartmentDto dto = new OrganizationModels.DepartmentDto(
                                    UUID.randomUUID(), tenantId, command.legalEntityId(), command.businessUnitId(), command.divisionId(), command.branchId(),
                                    normalize(command.departmentCode()), command.departmentName().trim(), trimToNull(command.shortName()),
                                    trimToNull(command.description()), command.active(), now, now, "system", "system");
                            return organizationRepository.createDepartment(dto)
                                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("DEPARTMENT_CODE_ALREADY_EXISTS", "Department code already exists"))
                                    .flatMap(saved -> audit("DEPARTMENT_CREATED", "DEPARTMENT", saved.id().toString(), tenantId, Map.of("code", saved.departmentCode())).thenReturn(saved));
                        })));
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> updateDepartment(UUID id, OrganizationModels.DepartmentUpsertCommand command) {
        validateDepartment(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(
                                optionalLegalEntity(tenantId, command.legalEntityId()),
                                optionalBusinessUnit(tenantId, command.businessUnitId()),
                                optionalDivision(tenantId, command.divisionId()),
                                optionalBranch(tenantId, command.branchId()))
                        .then(organizationRepository.findDepartmentById(tenantId, id)
                                .switchIfEmpty(notFound("DEPARTMENT_NOT_FOUND", "Department not found"))
                                .flatMap(existing -> organizationRepository.updateDepartment(new OrganizationModels.DepartmentDto(
                                                existing.id(), tenantId, command.legalEntityId(), command.businessUnitId(), command.divisionId(), command.branchId(),
                                                normalize(command.departmentCode()), command.departmentName().trim(), trimToNull(command.shortName()),
                                                trimToNull(command.description()), command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("DEPARTMENT_CODE_ALREADY_EXISTS", "Department code already exists"))
                                        .flatMap(saved -> audit("DEPARTMENT_UPDATED", "DEPARTMENT", saved.id().toString(), tenantId, Map.of("code", saved.departmentCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> getDepartment(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findDepartmentById(tenantId, id)
                .switchIfEmpty(notFound("DEPARTMENT_NOT_FOUND", "Department not found")));
    }

    @Override
    public Flux<OrganizationModels.DepartmentDto> searchDepartments(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchDepartments(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.DepartmentDto> updateDepartmentStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateDepartmentStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("DEPARTMENT_NOT_FOUND", "Department not found"))
                .flatMap(saved -> audit("DEPARTMENT_STATUS_UPDATED", "DEPARTMENT", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> departmentOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.departmentOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.SectionDto> createSection(OrganizationModels.SectionUpsertCommand command) {
        validateSection(command);
        return withTenant("organization").flatMap(tenantId -> requireDepartment(tenantId, command.departmentId()).then(Mono.defer(() -> {
            Instant now = Instant.now();
            OrganizationModels.SectionDto dto = new OrganizationModels.SectionDto(
                    UUID.randomUUID(), tenantId, command.departmentId(), normalize(command.sectionCode()), command.sectionName().trim(),
                    trimToNull(command.description()), command.active(), now, now, "system", "system");
            return organizationRepository.createSection(dto)
                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("SECTION_CODE_ALREADY_EXISTS", "Section code already exists"))
                    .flatMap(saved -> audit("SECTION_CREATED", "SECTION", saved.id().toString(), tenantId, Map.of("code", saved.sectionCode())).thenReturn(saved));
        })));
    }

    @Override
    public Mono<OrganizationModels.SectionDto> updateSection(UUID id, OrganizationModels.SectionUpsertCommand command) {
        validateSection(command);
        return withTenant("organization").flatMap(tenantId -> requireDepartment(tenantId, command.departmentId()).then(
                organizationRepository.findSectionById(tenantId, id)
                        .switchIfEmpty(notFound("SECTION_NOT_FOUND", "Section not found"))
                        .flatMap(existing -> organizationRepository.updateSection(new OrganizationModels.SectionDto(
                                        existing.id(), tenantId, command.departmentId(), normalize(command.sectionCode()), command.sectionName().trim(),
                                        trimToNull(command.description()), command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("SECTION_CODE_ALREADY_EXISTS", "Section code already exists"))
                                .flatMap(saved -> audit("SECTION_UPDATED", "SECTION", saved.id().toString(), tenantId, Map.of("code", saved.sectionCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.SectionDto> getSection(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findSectionById(tenantId, id)
                .switchIfEmpty(notFound("SECTION_NOT_FOUND", "Section not found")));
    }

    @Override
    public Flux<OrganizationModels.SectionDto> searchSections(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchSections(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.SectionDto> updateSectionStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateSectionStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("SECTION_NOT_FOUND", "Section not found"))
                .flatMap(saved -> audit("SECTION_STATUS_UPDATED", "SECTION", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> sectionOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.sectionOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> createWorkLocation(OrganizationModels.WorkLocationUpsertCommand command) {
        validateWorkLocation(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(optionalLegalEntity(tenantId, command.legalEntityId()), optionalBranch(tenantId, command.branchId()))
                        .then(Mono.defer(() -> {
                            Instant now = Instant.now();
                            OrganizationModels.WorkLocationDto dto = new OrganizationModels.WorkLocationDto(
                                    UUID.randomUUID(), tenantId, command.legalEntityId(), command.branchId(), normalize(command.locationCode()),
                                    command.locationName().trim(), command.locationType(),
                                    trimToNull(command.addressLine1()), trimToNull(command.addressLine2()), trimToNull(command.city()),
                                    trimToNull(command.state()), trimToNull(command.countryCode()), trimToNull(command.postalCode()),
                                    command.latitude(), command.longitude(), command.geofenceRadius(), command.active(), now, now, "system", "system");
                            return organizationRepository.createWorkLocation(dto)
                                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("WORK_LOCATION_CODE_ALREADY_EXISTS", "Work location code already exists"))
                                    .flatMap(saved -> audit("WORK_LOCATION_CREATED", "WORK_LOCATION", saved.id().toString(), tenantId, Map.of("code", saved.locationCode())).thenReturn(saved));
                        })));
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> updateWorkLocation(UUID id, OrganizationModels.WorkLocationUpsertCommand command) {
        validateWorkLocation(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(optionalLegalEntity(tenantId, command.legalEntityId()), optionalBranch(tenantId, command.branchId()))
                        .then(organizationRepository.findWorkLocationById(tenantId, id)
                                .switchIfEmpty(notFound("WORK_LOCATION_NOT_FOUND", "Work location not found"))
                                .flatMap(existing -> organizationRepository.updateWorkLocation(new OrganizationModels.WorkLocationDto(
                                                existing.id(), tenantId, command.legalEntityId(), command.branchId(), normalize(command.locationCode()),
                                                command.locationName().trim(), command.locationType(),
                                                trimToNull(command.addressLine1()), trimToNull(command.addressLine2()), trimToNull(command.city()),
                                                trimToNull(command.state()), trimToNull(command.countryCode()), trimToNull(command.postalCode()),
                                                command.latitude(), command.longitude(), command.geofenceRadius(), command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("WORK_LOCATION_CODE_ALREADY_EXISTS", "Work location code already exists"))
                                        .flatMap(saved -> audit("WORK_LOCATION_UPDATED", "WORK_LOCATION", saved.id().toString(), tenantId, Map.of("code", saved.locationCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> getWorkLocation(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findWorkLocationById(tenantId, id)
                .switchIfEmpty(notFound("WORK_LOCATION_NOT_FOUND", "Work location not found")));
    }

    @Override
    public Flux<OrganizationModels.WorkLocationDto> searchWorkLocations(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchWorkLocations(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.WorkLocationDto> updateWorkLocationStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateWorkLocationStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("WORK_LOCATION_NOT_FOUND", "Work location not found"))
                .flatMap(saved -> audit("WORK_LOCATION_STATUS_UPDATED", "WORK_LOCATION", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> workLocationOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.workLocationOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> createCostCenter(OrganizationModels.CostCenterUpsertCommand command) {
        validateCostCenter(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(optionalLegalEntity(tenantId, command.legalEntityId()), validateCostCenterParent(tenantId, null, command.parentCostCenterId()))
                        .then(Mono.defer(() -> {
                            Instant now = Instant.now();
                            OrganizationModels.CostCenterDto dto = new OrganizationModels.CostCenterDto(
                                    UUID.randomUUID(), tenantId, command.legalEntityId(), normalize(command.costCenterCode()), command.costCenterName().trim(),
                                    trimToNull(command.description()), trimToNull(command.glAccountCode()), command.parentCostCenterId(),
                                    command.active(), now, now, "system", "system");
                            return organizationRepository.createCostCenter(dto)
                                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("COST_CENTER_CODE_ALREADY_EXISTS", "Cost center code already exists"))
                                    .flatMap(saved -> audit("COST_CENTER_CREATED", "COST_CENTER", saved.id().toString(), tenantId, Map.of("code", saved.costCenterCode())).thenReturn(saved));
                        })));
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> updateCostCenter(UUID id, OrganizationModels.CostCenterUpsertCommand command) {
        validateCostCenter(command);
        return withTenant("organization")
                .flatMap(tenantId -> Mono.when(optionalLegalEntity(tenantId, command.legalEntityId()), validateCostCenterParent(tenantId, id, command.parentCostCenterId()))
                        .then(organizationRepository.findCostCenterById(tenantId, id)
                                .switchIfEmpty(notFound("COST_CENTER_NOT_FOUND", "Cost center not found"))
                                .flatMap(existing -> organizationRepository.updateCostCenter(new OrganizationModels.CostCenterDto(
                                                existing.id(), tenantId, command.legalEntityId(), normalize(command.costCenterCode()), command.costCenterName().trim(),
                                                trimToNull(command.description()), trimToNull(command.glAccountCode()), command.parentCostCenterId(), command.active(),
                                                existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("COST_CENTER_CODE_ALREADY_EXISTS", "Cost center code already exists"))
                                        .flatMap(saved -> audit("COST_CENTER_UPDATED", "COST_CENTER", saved.id().toString(), tenantId, Map.of("code", saved.costCenterCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> getCostCenter(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findCostCenterById(tenantId, id)
                .switchIfEmpty(notFound("COST_CENTER_NOT_FOUND", "Cost center not found")));
    }

    @Override
    public Flux<OrganizationModels.CostCenterDto> searchCostCenters(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchCostCenters(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.CostCenterDto> updateCostCenterStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateCostCenterStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("COST_CENTER_NOT_FOUND", "Cost center not found"))
                .flatMap(saved -> audit("COST_CENTER_STATUS_UPDATED", "COST_CENTER", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> costCenterOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.costCenterOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> createReportingUnit(OrganizationModels.ReportingUnitUpsertCommand command) {
        validateReportingUnit(command);
        return withTenant("organization")
                .flatMap(tenantId -> validateReportingUnitParent(tenantId, null, command.parentReportingUnitId())
                        .then(Mono.defer(() -> {
                            Instant now = Instant.now();
                            OrganizationModels.ReportingUnitDto dto = new OrganizationModels.ReportingUnitDto(
                                    UUID.randomUUID(), tenantId, normalize(command.reportingUnitCode()), command.reportingUnitName().trim(),
                                    command.parentReportingUnitId(), trimToNull(command.description()), command.active(), now, now, "system", "system");
                            return organizationRepository.createReportingUnit(dto)
                                    .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("REPORTING_UNIT_CODE_ALREADY_EXISTS", "Reporting unit code already exists"))
                                    .flatMap(saved -> audit("REPORTING_UNIT_CREATED", "REPORTING_UNIT", saved.id().toString(), tenantId, Map.of("code", saved.reportingUnitCode())).thenReturn(saved));
                        })));
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> updateReportingUnit(UUID id, OrganizationModels.ReportingUnitUpsertCommand command) {
        validateReportingUnit(command);
        return withTenant("organization")
                .flatMap(tenantId -> validateReportingUnitParent(tenantId, id, command.parentReportingUnitId())
                        .then(organizationRepository.findReportingUnitById(tenantId, id)
                                .switchIfEmpty(notFound("REPORTING_UNIT_NOT_FOUND", "Reporting unit not found"))
                                .flatMap(existing -> organizationRepository.updateReportingUnit(new OrganizationModels.ReportingUnitDto(
                                                existing.id(), tenantId, normalize(command.reportingUnitCode()), command.reportingUnitName().trim(),
                                                command.parentReportingUnitId(), trimToNull(command.description()), command.active(), existing.createdAt(), Instant.now(), existing.createdBy(), "system"))
                                        .onErrorMap(DataIntegrityViolationException.class, ex -> duplicate("REPORTING_UNIT_CODE_ALREADY_EXISTS", "Reporting unit code already exists"))
                                        .flatMap(saved -> audit("REPORTING_UNIT_UPDATED", "REPORTING_UNIT", saved.id().toString(), tenantId, Map.of("code", saved.reportingUnitCode())).thenReturn(saved)))));
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> getReportingUnit(UUID id) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.findReportingUnitById(tenantId, id)
                .switchIfEmpty(notFound("REPORTING_UNIT_NOT_FOUND", "Reporting unit not found")));
    }

    @Override
    public Flux<OrganizationModels.ReportingUnitDto> searchReportingUnits(OrganizationModels.SearchQuery query) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.searchReportingUnits(tenantId, normalizeQuery(query)));
    }

    @Override
    public Mono<OrganizationModels.ReportingUnitDto> updateReportingUnitStatus(UUID id, OrganizationModels.StatusUpdateCommand command) {
        return withTenant("organization").flatMap(tenantId -> organizationRepository.updateReportingUnitStatus(tenantId, id, command.active(), "system")
                .switchIfEmpty(notFound("REPORTING_UNIT_NOT_FOUND", "Reporting unit not found"))
                .flatMap(saved -> audit("REPORTING_UNIT_STATUS_UPDATED", "REPORTING_UNIT", saved.id().toString(), tenantId, Map.of("active", command.active())).thenReturn(saved)));
    }

    @Override
    public Flux<OrganizationModels.OptionViewDto> reportingUnitOptions(String q, int limit) {
        return withTenant("organization").flatMapMany(tenantId -> organizationRepository.reportingUnitOptions(tenantId, q, normalizeLimit(limit)));
    }

    @Override
    public Mono<OrganizationModels.OrganizationTreeViewDto> organizationTree() {
        return buildTree(false);
    }

    @Override
    public Mono<OrganizationModels.OrganizationTreeViewDto> organizationChart() {
        return buildTree(true);
    }

    private Mono<OrganizationModels.OrganizationTreeViewDto> buildTree(boolean activeOnly) {
        OrganizationModels.SearchQuery query = new OrganizationModels.SearchQuery(null, activeOnly ? Boolean.TRUE : null, 5000, 0);
        return withTenant("organization").flatMap(tenantId -> Mono.zip(values -> {
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.LegalEntityDto> legalEntities = (List<OrganizationModels.LegalEntityDto>) values[0];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.BranchDto> branches = (List<OrganizationModels.BranchDto>) values[1];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.BusinessUnitDto> businessUnits = (List<OrganizationModels.BusinessUnitDto>) values[2];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.DivisionDto> divisions = (List<OrganizationModels.DivisionDto>) values[3];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.DepartmentDto> departments = (List<OrganizationModels.DepartmentDto>) values[4];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.SectionDto> sections = (List<OrganizationModels.SectionDto>) values[5];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.WorkLocationDto> locations = (List<OrganizationModels.WorkLocationDto>) values[6];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.CostCenterDto> costCenters = (List<OrganizationModels.CostCenterDto>) values[7];
                    @SuppressWarnings("unchecked")
                    List<OrganizationModels.ReportingUnitDto> reportingUnits = (List<OrganizationModels.ReportingUnitDto>) values[8];

                    List<OrganizationModels.OrganizationNodeViewDto> roots = new ArrayList<>();
                    for (OrganizationModels.LegalEntityDto legalEntity : legalEntities) {
                        List<OrganizationModels.OrganizationNodeViewDto> branchNodes = branches.stream()
                                .filter(branch -> legalEntity.id().equals(branch.legalEntityId()))
                                .map(branch -> buildBranchNode(branch, businessUnits, divisions, departments, sections))
                                .toList();
                        roots.add(new OrganizationModels.OrganizationNodeViewDto(
                                "LEGAL_ENTITY", legalEntity.id(), legalEntity.legalEntityCode(), legalEntity.legalEntityName(), legalEntity.active(), branchNodes));
                    }

                    roots.add(new OrganizationModels.OrganizationNodeViewDto(
                            "WORK_LOCATION_DIMENSION", null, "WORK_LOCATIONS", "Work Locations", true,
                            locations.stream().map(location -> new OrganizationModels.OrganizationNodeViewDto(
                                    "WORK_LOCATION", location.id(), location.locationCode(), location.locationName(), location.active(), List.of())).toList()));
                    roots.add(new OrganizationModels.OrganizationNodeViewDto(
                            "COST_CENTER_DIMENSION", null, "COST_CENTERS", "Cost Centers", true,
                            buildCostCenterNodes(costCenters, null)));
                    roots.add(new OrganizationModels.OrganizationNodeViewDto(
                            "REPORTING_UNIT_DIMENSION", null, "REPORTING_UNITS", "Reporting Units", true,
                            buildReportingUnitNodes(reportingUnits, null)));

                    return new OrganizationModels.OrganizationTreeViewDto(roots);
                },
                organizationRepository.searchLegalEntities(tenantId, query).collectList(),
                organizationRepository.searchBranches(tenantId, query).collectList(),
                organizationRepository.searchBusinessUnits(tenantId, query).collectList(),
                organizationRepository.searchDivisions(tenantId, query).collectList(),
                organizationRepository.searchDepartments(tenantId, query).collectList(),
                organizationRepository.searchSections(tenantId, query).collectList(),
                organizationRepository.searchWorkLocations(tenantId, query).collectList(),
                organizationRepository.searchCostCenters(tenantId, query).collectList(),
                organizationRepository.searchReportingUnits(tenantId, query).collectList()));
    }

    private OrganizationModels.OrganizationNodeViewDto buildBranchNode(
            OrganizationModels.BranchDto branch,
            List<OrganizationModels.BusinessUnitDto> businessUnits,
            List<OrganizationModels.DivisionDto> divisions,
            List<OrganizationModels.DepartmentDto> departments,
            List<OrganizationModels.SectionDto> sections
    ) {
        List<OrganizationModels.OrganizationNodeViewDto> buNodes = businessUnits.stream()
                .filter(bu -> bu.legalEntityId() != null)
                .map(bu -> buildBusinessUnitNode(bu, divisions, departments, sections))
                .toList();

        List<OrganizationModels.OrganizationNodeViewDto> divisionNodes = divisions.stream()
                .filter(division -> branch.id().equals(division.branchId()))
                .map(division -> buildDivisionNode(division, departments, sections))
                .toList();

        List<OrganizationModels.OrganizationNodeViewDto> children = new ArrayList<>();
        children.addAll(buNodes);
        children.addAll(divisionNodes);

        return new OrganizationModels.OrganizationNodeViewDto(
                "BRANCH", branch.id(), branch.branchCode(), branch.branchName(), branch.active(), children);
    }

    private OrganizationModels.OrganizationNodeViewDto buildBusinessUnitNode(
            OrganizationModels.BusinessUnitDto businessUnit,
            List<OrganizationModels.DivisionDto> divisions,
            List<OrganizationModels.DepartmentDto> departments,
            List<OrganizationModels.SectionDto> sections
    ) {
        List<OrganizationModels.OrganizationNodeViewDto> children = divisions.stream()
                .filter(division -> businessUnit.id().equals(division.businessUnitId()))
                .map(division -> buildDivisionNode(division, departments, sections))
                .toList();

        return new OrganizationModels.OrganizationNodeViewDto(
                "BUSINESS_UNIT", businessUnit.id(), businessUnit.businessUnitCode(), businessUnit.businessUnitName(), businessUnit.active(), children);
    }

    private OrganizationModels.OrganizationNodeViewDto buildDivisionNode(
            OrganizationModels.DivisionDto division,
            List<OrganizationModels.DepartmentDto> departments,
            List<OrganizationModels.SectionDto> sections
    ) {
        List<OrganizationModels.OrganizationNodeViewDto> children = departments.stream()
                .filter(department -> division.id().equals(department.divisionId()))
                .map(department -> buildDepartmentNode(department, sections))
                .toList();

        return new OrganizationModels.OrganizationNodeViewDto(
                "DIVISION", division.id(), division.divisionCode(), division.divisionName(), division.active(), children);
    }

    private OrganizationModels.OrganizationNodeViewDto buildDepartmentNode(
            OrganizationModels.DepartmentDto department,
            List<OrganizationModels.SectionDto> sections
    ) {
        List<OrganizationModels.OrganizationNodeViewDto> children = sections.stream()
                .filter(section -> department.id().equals(section.departmentId()))
                .map(section -> new OrganizationModels.OrganizationNodeViewDto(
                        "SECTION", section.id(), section.sectionCode(), section.sectionName(), section.active(), List.of()))
                .toList();

        return new OrganizationModels.OrganizationNodeViewDto(
                "DEPARTMENT", department.id(), department.departmentCode(), department.departmentName(), department.active(), children);
    }

    private List<OrganizationModels.OrganizationNodeViewDto> buildCostCenterNodes(List<OrganizationModels.CostCenterDto> costCenters, UUID parentId) {
        return costCenters.stream()
                .filter(costCenter -> java.util.Objects.equals(parentId, costCenter.parentCostCenterId()))
                .map(costCenter -> new OrganizationModels.OrganizationNodeViewDto(
                        "COST_CENTER",
                        costCenter.id(),
                        costCenter.costCenterCode(),
                        costCenter.costCenterName(),
                        costCenter.active(),
                        buildCostCenterNodes(costCenters, costCenter.id())))
                .toList();
    }

    private List<OrganizationModels.OrganizationNodeViewDto> buildReportingUnitNodes(List<OrganizationModels.ReportingUnitDto> reportingUnits, UUID parentId) {
        return reportingUnits.stream()
                .filter(reportingUnit -> java.util.Objects.equals(parentId, reportingUnit.parentReportingUnitId()))
                .map(reportingUnit -> new OrganizationModels.OrganizationNodeViewDto(
                        "REPORTING_UNIT",
                        reportingUnit.id(),
                        reportingUnit.reportingUnitCode(),
                        reportingUnit.reportingUnitName(),
                        reportingUnit.active(),
                        buildReportingUnitNodes(reportingUnits, reportingUnit.id())))
                .toList();
    }

    private Mono<Void> validateCostCenterParent(String tenantId, UUID currentId, UUID parentId) {
        if (parentId == null) {
            return Mono.empty();
        }
        if (currentId != null && currentId.equals(parentId)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COST_CENTER_CYCLE", "Cost center parent cannot be itself"));
        }
        return organizationRepository.findCostCenterById(tenantId, parentId)
                .switchIfEmpty(notFound("PARENT_COST_CENTER_NOT_FOUND", "Parent cost center not found"))
                .flatMap(parent -> {
                    if (currentId == null) {
                        return Mono.empty();
                    }
                    return detectCostCenterCycle(tenantId, currentId, parent.parentCostCenterId());
                });
    }

    private Mono<Void> detectCostCenterCycle(String tenantId, UUID currentId, UUID nextParentId) {
        if (nextParentId == null) {
            return Mono.empty();
        }
        if (currentId.equals(nextParentId)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COST_CENTER_CYCLE", "Cost center circular hierarchy detected"));
        }
        return organizationRepository.findCostCenterById(tenantId, nextParentId)
                .flatMap(node -> detectCostCenterCycle(tenantId, currentId, node.parentCostCenterId()));
    }

    private Mono<Void> validateReportingUnitParent(String tenantId, UUID currentId, UUID parentId) {
        if (parentId == null) {
            return Mono.empty();
        }
        if (currentId != null && currentId.equals(parentId)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "REPORTING_UNIT_CYCLE", "Reporting unit parent cannot be itself"));
        }
        return organizationRepository.findReportingUnitById(tenantId, parentId)
                .switchIfEmpty(notFound("PARENT_REPORTING_UNIT_NOT_FOUND", "Parent reporting unit not found"))
                .flatMap(parent -> {
                    if (currentId == null) {
                        return Mono.empty();
                    }
                    return detectReportingUnitCycle(tenantId, currentId, parent.parentReportingUnitId());
                });
    }

    private Mono<Void> detectReportingUnitCycle(String tenantId, UUID currentId, UUID nextParentId) {
        if (nextParentId == null) {
            return Mono.empty();
        }
        if (currentId.equals(nextParentId)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "REPORTING_UNIT_CYCLE", "Reporting unit circular hierarchy detected"));
        }
        return organizationRepository.findReportingUnitById(tenantId, nextParentId)
                .flatMap(node -> detectReportingUnitCycle(tenantId, currentId, node.parentReportingUnitId()));
    }

    private void validateLegalEntity(OrganizationModels.LegalEntityUpsertCommand command) {
        requireText(command.legalEntityCode(), "LEGAL_ENTITY_CODE_REQUIRED", "Legal entity code is required");
        requireText(command.legalEntityName(), "LEGAL_ENTITY_NAME_REQUIRED", "Legal entity name is required");
        validateEmail(command.contactEmail(), "CONTACT_EMAIL_INVALID");
        validatePhone(command.contactPhone(), "CONTACT_PHONE_INVALID");
    }

    private void validateBranch(OrganizationModels.BranchUpsertCommand command) {
        if (command.legalEntityId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEGAL_ENTITY_REQUIRED", "Legal entity is required");
        }
        requireText(command.branchCode(), "BRANCH_CODE_REQUIRED", "Branch code is required");
        requireText(command.branchName(), "BRANCH_NAME_REQUIRED", "Branch name is required");
        validateEmail(command.email(), "BRANCH_EMAIL_INVALID");
        validatePhone(command.phone(), "BRANCH_PHONE_INVALID");
    }

    private void validateBusinessUnit(OrganizationModels.BusinessUnitUpsertCommand command) {
        requireText(command.businessUnitCode(), "BUSINESS_UNIT_CODE_REQUIRED", "Business unit code is required");
        requireText(command.businessUnitName(), "BUSINESS_UNIT_NAME_REQUIRED", "Business unit name is required");
    }

    private void validateDivision(OrganizationModels.DivisionUpsertCommand command) {
        requireText(command.divisionCode(), "DIVISION_CODE_REQUIRED", "Division code is required");
        requireText(command.divisionName(), "DIVISION_NAME_REQUIRED", "Division name is required");
    }

    private void validateDepartment(OrganizationModels.DepartmentUpsertCommand command) {
        requireText(command.departmentCode(), "DEPARTMENT_CODE_REQUIRED", "Department code is required");
        requireText(command.departmentName(), "DEPARTMENT_NAME_REQUIRED", "Department name is required");
    }

    private void validateSection(OrganizationModels.SectionUpsertCommand command) {
        if (command.departmentId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "DEPARTMENT_REQUIRED", "Department is required");
        }
        requireText(command.sectionCode(), "SECTION_CODE_REQUIRED", "Section code is required");
        requireText(command.sectionName(), "SECTION_NAME_REQUIRED", "Section name is required");
    }

    private void validateWorkLocation(OrganizationModels.WorkLocationUpsertCommand command) {
        requireText(command.locationCode(), "WORK_LOCATION_CODE_REQUIRED", "Work location code is required");
        requireText(command.locationName(), "WORK_LOCATION_NAME_REQUIRED", "Work location name is required");
        if (command.locationType() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LOCATION_TYPE_REQUIRED", "Location type is required");
        }
        validateLatitude(command.latitude());
        validateLongitude(command.longitude());
        validateGeofence(command.geofenceRadius());
    }

    private void validateCostCenter(OrganizationModels.CostCenterUpsertCommand command) {
        requireText(command.costCenterCode(), "COST_CENTER_CODE_REQUIRED", "Cost center code is required");
        requireText(command.costCenterName(), "COST_CENTER_NAME_REQUIRED", "Cost center name is required");
    }

    private void validateReportingUnit(OrganizationModels.ReportingUnitUpsertCommand command) {
        requireText(command.reportingUnitCode(), "REPORTING_UNIT_CODE_REQUIRED", "Reporting unit code is required");
        requireText(command.reportingUnitName(), "REPORTING_UNIT_NAME_REQUIRED", "Reporting unit name is required");
    }

    private void validateLatitude(BigDecimal latitude) {
        if (latitude != null && (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LATITUDE_INVALID", "Latitude must be between -90 and 90");
        }
    }

    private void validateLongitude(BigDecimal longitude) {
        if (longitude != null && (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LONGITUDE_INVALID", "Longitude must be between -180 and 180");
        }
    }

    private void validateGeofence(BigDecimal geofenceRadius) {
        if (geofenceRadius != null && geofenceRadius.compareTo(BigDecimal.ZERO) < 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "GEOFENCE_RADIUS_INVALID", "Geofence radius must be >= 0");
        }
    }

    private void validateEmail(String email, String errorCode) {
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, errorCode, "Email format is invalid");
        }
    }

    private void validatePhone(String phone, String errorCode) {
        if (StringUtils.hasText(phone) && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, errorCode, "Phone format is invalid");
        }
    }

    private Mono<Void> requireLegalEntity(String tenantId, UUID legalEntityId) {
        return organizationRepository.findLegalEntityById(tenantId, legalEntityId)
                .switchIfEmpty(notFound("LEGAL_ENTITY_NOT_FOUND", "Legal entity not found"))
                .then();
    }

    private Mono<Void> optionalLegalEntity(String tenantId, UUID legalEntityId) {
        if (legalEntityId == null) {
            return Mono.empty();
        }
        return requireLegalEntity(tenantId, legalEntityId);
    }

    private Mono<Void> optionalBusinessUnit(String tenantId, UUID businessUnitId) {
        if (businessUnitId == null) {
            return Mono.empty();
        }
        return organizationRepository.findBusinessUnitById(tenantId, businessUnitId)
                .switchIfEmpty(notFound("BUSINESS_UNIT_NOT_FOUND", "Business unit not found"))
                .then();
    }

    private Mono<Void> optionalDivision(String tenantId, UUID divisionId) {
        if (divisionId == null) {
            return Mono.empty();
        }
        return organizationRepository.findDivisionById(tenantId, divisionId)
                .switchIfEmpty(notFound("DIVISION_NOT_FOUND", "Division not found"))
                .then();
    }

    private Mono<Void> optionalBranch(String tenantId, UUID branchId) {
        if (branchId == null) {
            return Mono.empty();
        }
        return organizationRepository.findBranchById(tenantId, branchId)
                .switchIfEmpty(notFound("BRANCH_NOT_FOUND", "Branch not found"))
                .then();
    }

    private Mono<Void> requireDepartment(String tenantId, UUID departmentId) {
        return organizationRepository.findDepartmentById(tenantId, departmentId)
                .switchIfEmpty(notFound("DEPARTMENT_NOT_FOUND", "Department not found"))
                .then();
    }

    private Mono<String> withTenant(String module) {
        return enablementGuard.requireModuleEnabled(module)
                .then(tenantContextAccessor.currentTenantId()
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required"))));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, 500);
    }

    private OrganizationModels.SearchQuery normalizeQuery(OrganizationModels.SearchQuery query) {
        if (query == null) {
            return new OrganizationModels.SearchQuery(null, null, 50, 0);
        }
        return new OrganizationModels.SearchQuery(query.q(), query.active(), normalizeLimit(query.limit()), Math.max(0, query.offset()));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void requireText(String value, String code, String message) {
        if (!StringUtils.hasText(value)) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, code, message);
        }
    }

    private Mono<Void> audit(String action, String targetType, String targetId, String tenantId, Map<String, ?> metadata) {
        return auditEventPublisher.publish(AuditEvent.of("system", tenantId, action, targetType, targetId, metadata));
    }

    private <T> Mono<T> notFound(String code, String message) {
        return Mono.error(new HrmsException(HttpStatus.NOT_FOUND, code, message));
    }

    private HrmsException duplicate(String code, String message) {
        return new HrmsException(HttpStatus.CONFLICT, code, message);
    }
}
