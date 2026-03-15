package com.company.hrms.organization.service;

import com.company.hrms.organization.model.OrganizationModels;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationModuleApi {

    Mono<OrganizationModels.LegalEntityDto> createLegalEntity(OrganizationModels.LegalEntityUpsertCommand command);

    Mono<OrganizationModels.LegalEntityDto> updateLegalEntity(UUID id, OrganizationModels.LegalEntityUpsertCommand command);

    Mono<OrganizationModels.LegalEntityDto> getLegalEntity(UUID id);

    Flux<OrganizationModels.LegalEntityDto> searchLegalEntities(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.LegalEntityDto> updateLegalEntityStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> legalEntityOptions(String q, int limit);

    Mono<OrganizationModels.BranchDto> createBranch(OrganizationModels.BranchUpsertCommand command);

    Mono<OrganizationModels.BranchDto> updateBranch(UUID id, OrganizationModels.BranchUpsertCommand command);

    Mono<OrganizationModels.BranchDto> getBranch(UUID id);

    Flux<OrganizationModels.BranchDto> searchBranches(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.BranchDto> updateBranchStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> branchOptions(String q, int limit);

    Mono<OrganizationModels.BusinessUnitDto> createBusinessUnit(OrganizationModels.BusinessUnitUpsertCommand command);

    Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnit(UUID id, OrganizationModels.BusinessUnitUpsertCommand command);

    Mono<OrganizationModels.BusinessUnitDto> getBusinessUnit(UUID id);

    Flux<OrganizationModels.BusinessUnitDto> searchBusinessUnits(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnitStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> businessUnitOptions(String q, int limit);

    Mono<OrganizationModels.DivisionDto> createDivision(OrganizationModels.DivisionUpsertCommand command);

    Mono<OrganizationModels.DivisionDto> updateDivision(UUID id, OrganizationModels.DivisionUpsertCommand command);

    Mono<OrganizationModels.DivisionDto> getDivision(UUID id);

    Flux<OrganizationModels.DivisionDto> searchDivisions(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.DivisionDto> updateDivisionStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> divisionOptions(String q, int limit);

    Mono<OrganizationModels.DepartmentDto> createDepartment(OrganizationModels.DepartmentUpsertCommand command);

    Mono<OrganizationModels.DepartmentDto> updateDepartment(UUID id, OrganizationModels.DepartmentUpsertCommand command);

    Mono<OrganizationModels.DepartmentDto> getDepartment(UUID id);

    Flux<OrganizationModels.DepartmentDto> searchDepartments(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.DepartmentDto> updateDepartmentStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> departmentOptions(String q, int limit);

    Mono<OrganizationModels.SectionDto> createSection(OrganizationModels.SectionUpsertCommand command);

    Mono<OrganizationModels.SectionDto> updateSection(UUID id, OrganizationModels.SectionUpsertCommand command);

    Mono<OrganizationModels.SectionDto> getSection(UUID id);

    Flux<OrganizationModels.SectionDto> searchSections(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.SectionDto> updateSectionStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> sectionOptions(String q, int limit);

    Mono<OrganizationModels.WorkLocationDto> createWorkLocation(OrganizationModels.WorkLocationUpsertCommand command);

    Mono<OrganizationModels.WorkLocationDto> updateWorkLocation(UUID id, OrganizationModels.WorkLocationUpsertCommand command);

    Mono<OrganizationModels.WorkLocationDto> getWorkLocation(UUID id);

    Flux<OrganizationModels.WorkLocationDto> searchWorkLocations(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.WorkLocationDto> updateWorkLocationStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> workLocationOptions(String q, int limit);

    Mono<OrganizationModels.CostCenterDto> createCostCenter(OrganizationModels.CostCenterUpsertCommand command);

    Mono<OrganizationModels.CostCenterDto> updateCostCenter(UUID id, OrganizationModels.CostCenterUpsertCommand command);

    Mono<OrganizationModels.CostCenterDto> getCostCenter(UUID id);

    Flux<OrganizationModels.CostCenterDto> searchCostCenters(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.CostCenterDto> updateCostCenterStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> costCenterOptions(String q, int limit);

    Mono<OrganizationModels.ReportingUnitDto> createReportingUnit(OrganizationModels.ReportingUnitUpsertCommand command);

    Mono<OrganizationModels.ReportingUnitDto> updateReportingUnit(UUID id, OrganizationModels.ReportingUnitUpsertCommand command);

    Mono<OrganizationModels.ReportingUnitDto> getReportingUnit(UUID id);

    Flux<OrganizationModels.ReportingUnitDto> searchReportingUnits(OrganizationModels.SearchQuery query);

    Mono<OrganizationModels.ReportingUnitDto> updateReportingUnitStatus(UUID id, OrganizationModels.StatusUpdateCommand command);

    Flux<OrganizationModels.OptionViewDto> reportingUnitOptions(String q, int limit);

    Mono<OrganizationModels.OrganizationTreeViewDto> organizationTree();

    Mono<OrganizationModels.OrganizationTreeViewDto> organizationChart();
}
