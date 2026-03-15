package com.company.hrms.organization.repository;

import com.company.hrms.organization.model.OrganizationModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRepository {

    Mono<OrganizationModels.LegalEntityDto> createLegalEntity(OrganizationModels.LegalEntityDto dto);

    Mono<OrganizationModels.LegalEntityDto> updateLegalEntity(OrganizationModels.LegalEntityDto dto);

    Mono<OrganizationModels.LegalEntityDto> findLegalEntityById(String tenantId, UUID id);

    Flux<OrganizationModels.LegalEntityDto> searchLegalEntities(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.LegalEntityDto> updateLegalEntityStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> legalEntityOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.BranchDto> createBranch(OrganizationModels.BranchDto dto);

    Mono<OrganizationModels.BranchDto> updateBranch(OrganizationModels.BranchDto dto);

    Mono<OrganizationModels.BranchDto> findBranchById(String tenantId, UUID id);

    Flux<OrganizationModels.BranchDto> searchBranches(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.BranchDto> updateBranchStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> branchOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.BusinessUnitDto> createBusinessUnit(OrganizationModels.BusinessUnitDto dto);

    Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnit(OrganizationModels.BusinessUnitDto dto);

    Mono<OrganizationModels.BusinessUnitDto> findBusinessUnitById(String tenantId, UUID id);

    Flux<OrganizationModels.BusinessUnitDto> searchBusinessUnits(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnitStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> businessUnitOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.DivisionDto> createDivision(OrganizationModels.DivisionDto dto);

    Mono<OrganizationModels.DivisionDto> updateDivision(OrganizationModels.DivisionDto dto);

    Mono<OrganizationModels.DivisionDto> findDivisionById(String tenantId, UUID id);

    Flux<OrganizationModels.DivisionDto> searchDivisions(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.DivisionDto> updateDivisionStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> divisionOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.DepartmentDto> createDepartment(OrganizationModels.DepartmentDto dto);

    Mono<OrganizationModels.DepartmentDto> updateDepartment(OrganizationModels.DepartmentDto dto);

    Mono<OrganizationModels.DepartmentDto> findDepartmentById(String tenantId, UUID id);

    Flux<OrganizationModels.DepartmentDto> searchDepartments(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.DepartmentDto> updateDepartmentStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> departmentOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.SectionDto> createSection(OrganizationModels.SectionDto dto);

    Mono<OrganizationModels.SectionDto> updateSection(OrganizationModels.SectionDto dto);

    Mono<OrganizationModels.SectionDto> findSectionById(String tenantId, UUID id);

    Flux<OrganizationModels.SectionDto> searchSections(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.SectionDto> updateSectionStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> sectionOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.WorkLocationDto> createWorkLocation(OrganizationModels.WorkLocationDto dto);

    Mono<OrganizationModels.WorkLocationDto> updateWorkLocation(OrganizationModels.WorkLocationDto dto);

    Mono<OrganizationModels.WorkLocationDto> findWorkLocationById(String tenantId, UUID id);

    Flux<OrganizationModels.WorkLocationDto> searchWorkLocations(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.WorkLocationDto> updateWorkLocationStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> workLocationOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.CostCenterDto> createCostCenter(OrganizationModels.CostCenterDto dto);

    Mono<OrganizationModels.CostCenterDto> updateCostCenter(OrganizationModels.CostCenterDto dto);

    Mono<OrganizationModels.CostCenterDto> findCostCenterById(String tenantId, UUID id);

    Flux<OrganizationModels.CostCenterDto> searchCostCenters(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.CostCenterDto> updateCostCenterStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> costCenterOptions(String tenantId, String q, int limit);

    Mono<OrganizationModels.ReportingUnitDto> createReportingUnit(OrganizationModels.ReportingUnitDto dto);

    Mono<OrganizationModels.ReportingUnitDto> updateReportingUnit(OrganizationModels.ReportingUnitDto dto);

    Mono<OrganizationModels.ReportingUnitDto> findReportingUnitById(String tenantId, UUID id);

    Flux<OrganizationModels.ReportingUnitDto> searchReportingUnits(String tenantId, OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.ReportingUnitDto> updateReportingUnitStatus(String tenantId, UUID id, boolean active, String actor);

    Flux<OrganizationModels.OptionViewDto> reportingUnitOptions(String tenantId, String q, int limit);
}
