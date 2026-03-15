package com.company.hrms.organization.controller;

import com.company.hrms.organization.model.OrganizationModels;
import com.company.hrms.organization.service.OrganizationModuleApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/organization")
@Tag(name = "Organization", description = "Organization structure master APIs")
public class OrganizationController {

    private final OrganizationModuleApi organizationModuleApi;

    public OrganizationController(OrganizationModuleApi organizationModuleApi) {
        this.organizationModuleApi = organizationModuleApi;
    }

    @PostMapping("/legal-entities")
    public Mono<OrganizationModels.LegalEntityDto> createLegalEntity(@Valid @RequestBody LegalEntityRequest request) {
        return organizationModuleApi.createLegalEntity(request.toCommand());
    }

    @GetMapping("/legal-entities")
    public Flux<OrganizationModels.LegalEntityDto> legalEntities(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchLegalEntities(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/legal-entities/{id}")
    public Mono<OrganizationModels.LegalEntityDto> legalEntity(@PathVariable UUID id) {
        return organizationModuleApi.getLegalEntity(id);
    }

    @PutMapping("/legal-entities/{id}")
    public Mono<OrganizationModels.LegalEntityDto> updateLegalEntity(@PathVariable UUID id, @Valid @RequestBody LegalEntityRequest request) {
        return organizationModuleApi.updateLegalEntity(id, request.toCommand());
    }

    @PatchMapping("/legal-entities/{id}/status")
    public Mono<OrganizationModels.LegalEntityDto> legalEntityStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateLegalEntityStatus(id, request.toCommand());
    }

    @GetMapping("/legal-entities/options")
    public Flux<OrganizationModels.OptionViewDto> legalEntityOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.legalEntityOptions(q, limit);
    }

    @PostMapping("/branches")
    public Mono<OrganizationModels.BranchDto> createBranch(@Valid @RequestBody BranchRequest request) {
        return organizationModuleApi.createBranch(request.toCommand());
    }

    @GetMapping("/branches")
    public Flux<OrganizationModels.BranchDto> branches(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchBranches(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/branches/{id}")
    public Mono<OrganizationModels.BranchDto> branch(@PathVariable UUID id) {
        return organizationModuleApi.getBranch(id);
    }

    @PutMapping("/branches/{id}")
    public Mono<OrganizationModels.BranchDto> updateBranch(@PathVariable UUID id, @Valid @RequestBody BranchRequest request) {
        return organizationModuleApi.updateBranch(id, request.toCommand());
    }

    @PatchMapping("/branches/{id}/status")
    public Mono<OrganizationModels.BranchDto> branchStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateBranchStatus(id, request.toCommand());
    }

    @GetMapping("/branches/options")
    public Flux<OrganizationModels.OptionViewDto> branchOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.branchOptions(q, limit);
    }

    @PostMapping("/business-units")
    public Mono<OrganizationModels.BusinessUnitDto> createBusinessUnit(@Valid @RequestBody BusinessUnitRequest request) {
        return organizationModuleApi.createBusinessUnit(request.toCommand());
    }

    @GetMapping("/business-units")
    public Flux<OrganizationModels.BusinessUnitDto> businessUnits(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchBusinessUnits(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/business-units/{id}")
    public Mono<OrganizationModels.BusinessUnitDto> businessUnit(@PathVariable UUID id) {
        return organizationModuleApi.getBusinessUnit(id);
    }

    @PutMapping("/business-units/{id}")
    public Mono<OrganizationModels.BusinessUnitDto> updateBusinessUnit(@PathVariable UUID id, @Valid @RequestBody BusinessUnitRequest request) {
        return organizationModuleApi.updateBusinessUnit(id, request.toCommand());
    }

    @PatchMapping("/business-units/{id}/status")
    public Mono<OrganizationModels.BusinessUnitDto> businessUnitStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateBusinessUnitStatus(id, request.toCommand());
    }

    @GetMapping("/business-units/options")
    public Flux<OrganizationModels.OptionViewDto> businessUnitOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.businessUnitOptions(q, limit);
    }

    @PostMapping("/divisions")
    public Mono<OrganizationModels.DivisionDto> createDivision(@Valid @RequestBody DivisionRequest request) {
        return organizationModuleApi.createDivision(request.toCommand());
    }

    @GetMapping("/divisions")
    public Flux<OrganizationModels.DivisionDto> divisions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchDivisions(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/divisions/{id}")
    public Mono<OrganizationModels.DivisionDto> division(@PathVariable UUID id) {
        return organizationModuleApi.getDivision(id);
    }

    @PutMapping("/divisions/{id}")
    public Mono<OrganizationModels.DivisionDto> updateDivision(@PathVariable UUID id, @Valid @RequestBody DivisionRequest request) {
        return organizationModuleApi.updateDivision(id, request.toCommand());
    }

    @PatchMapping("/divisions/{id}/status")
    public Mono<OrganizationModels.DivisionDto> divisionStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateDivisionStatus(id, request.toCommand());
    }

    @GetMapping("/divisions/options")
    public Flux<OrganizationModels.OptionViewDto> divisionOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.divisionOptions(q, limit);
    }

    @PostMapping("/departments")
    public Mono<OrganizationModels.DepartmentDto> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return organizationModuleApi.createDepartment(request.toCommand());
    }

    @GetMapping("/departments")
    public Flux<OrganizationModels.DepartmentDto> departments(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchDepartments(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/departments/{id}")
    public Mono<OrganizationModels.DepartmentDto> department(@PathVariable UUID id) {
        return organizationModuleApi.getDepartment(id);
    }

    @PutMapping("/departments/{id}")
    public Mono<OrganizationModels.DepartmentDto> updateDepartment(@PathVariable UUID id, @Valid @RequestBody DepartmentRequest request) {
        return organizationModuleApi.updateDepartment(id, request.toCommand());
    }

    @PatchMapping("/departments/{id}/status")
    public Mono<OrganizationModels.DepartmentDto> departmentStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateDepartmentStatus(id, request.toCommand());
    }

    @GetMapping("/departments/options")
    public Flux<OrganizationModels.OptionViewDto> departmentOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.departmentOptions(q, limit);
    }

    @PostMapping("/sections")
    public Mono<OrganizationModels.SectionDto> createSection(@Valid @RequestBody SectionRequest request) {
        return organizationModuleApi.createSection(request.toCommand());
    }

    @GetMapping("/sections")
    public Flux<OrganizationModels.SectionDto> sections(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchSections(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/sections/{id}")
    public Mono<OrganizationModels.SectionDto> section(@PathVariable UUID id) {
        return organizationModuleApi.getSection(id);
    }

    @PutMapping("/sections/{id}")
    public Mono<OrganizationModels.SectionDto> updateSection(@PathVariable UUID id, @Valid @RequestBody SectionRequest request) {
        return organizationModuleApi.updateSection(id, request.toCommand());
    }

    @PatchMapping("/sections/{id}/status")
    public Mono<OrganizationModels.SectionDto> sectionStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateSectionStatus(id, request.toCommand());
    }

    @GetMapping("/sections/options")
    public Flux<OrganizationModels.OptionViewDto> sectionOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.sectionOptions(q, limit);
    }

    @PostMapping("/work-locations")
    public Mono<OrganizationModels.WorkLocationDto> createWorkLocation(@Valid @RequestBody WorkLocationRequest request) {
        return organizationModuleApi.createWorkLocation(request.toCommand());
    }

    @GetMapping("/work-locations")
    public Flux<OrganizationModels.WorkLocationDto> workLocations(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchWorkLocations(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/work-locations/{id}")
    public Mono<OrganizationModels.WorkLocationDto> workLocation(@PathVariable UUID id) {
        return organizationModuleApi.getWorkLocation(id);
    }

    @PutMapping("/work-locations/{id}")
    public Mono<OrganizationModels.WorkLocationDto> updateWorkLocation(@PathVariable UUID id, @Valid @RequestBody WorkLocationRequest request) {
        return organizationModuleApi.updateWorkLocation(id, request.toCommand());
    }

    @PatchMapping("/work-locations/{id}/status")
    public Mono<OrganizationModels.WorkLocationDto> workLocationStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateWorkLocationStatus(id, request.toCommand());
    }

    @GetMapping("/work-locations/options")
    public Flux<OrganizationModels.OptionViewDto> workLocationOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.workLocationOptions(q, limit);
    }

    @PostMapping("/cost-centers")
    public Mono<OrganizationModels.CostCenterDto> createCostCenter(@Valid @RequestBody CostCenterRequest request) {
        return organizationModuleApi.createCostCenter(request.toCommand());
    }

    @GetMapping("/cost-centers")
    public Flux<OrganizationModels.CostCenterDto> costCenters(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchCostCenters(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/cost-centers/{id}")
    public Mono<OrganizationModels.CostCenterDto> costCenter(@PathVariable UUID id) {
        return organizationModuleApi.getCostCenter(id);
    }

    @PutMapping("/cost-centers/{id}")
    public Mono<OrganizationModels.CostCenterDto> updateCostCenter(@PathVariable UUID id, @Valid @RequestBody CostCenterRequest request) {
        return organizationModuleApi.updateCostCenter(id, request.toCommand());
    }

    @PatchMapping("/cost-centers/{id}/status")
    public Mono<OrganizationModels.CostCenterDto> costCenterStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateCostCenterStatus(id, request.toCommand());
    }

    @GetMapping("/cost-centers/options")
    public Flux<OrganizationModels.OptionViewDto> costCenterOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.costCenterOptions(q, limit);
    }

    @PostMapping("/reporting-units")
    public Mono<OrganizationModels.ReportingUnitDto> createReportingUnit(@Valid @RequestBody ReportingUnitRequest request) {
        return organizationModuleApi.createReportingUnit(request.toCommand());
    }

    @GetMapping("/reporting-units")
    public Flux<OrganizationModels.ReportingUnitDto> reportingUnits(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset
    ) {
        return organizationModuleApi.searchReportingUnits(new OrganizationModels.SearchQuery(q, active, limit, offset));
    }

    @GetMapping("/reporting-units/{id}")
    public Mono<OrganizationModels.ReportingUnitDto> reportingUnit(@PathVariable UUID id) {
        return organizationModuleApi.getReportingUnit(id);
    }

    @PutMapping("/reporting-units/{id}")
    public Mono<OrganizationModels.ReportingUnitDto> updateReportingUnit(@PathVariable UUID id, @Valid @RequestBody ReportingUnitRequest request) {
        return organizationModuleApi.updateReportingUnit(id, request.toCommand());
    }

    @PatchMapping("/reporting-units/{id}/status")
    public Mono<OrganizationModels.ReportingUnitDto> reportingUnitStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request) {
        return organizationModuleApi.updateReportingUnitStatus(id, request.toCommand());
    }

    @GetMapping("/reporting-units/options")
    public Flux<OrganizationModels.OptionViewDto> reportingUnitOptions(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return organizationModuleApi.reportingUnitOptions(q, limit);
    }

    @GetMapping("/tree")
    @Operation(summary = "Organization tree")
    public Mono<OrganizationModels.OrganizationTreeViewDto> tree() {
        return organizationModuleApi.organizationTree();
    }

    @GetMapping("/chart")
    @Operation(summary = "Organization chart")
    public Mono<OrganizationModels.OrganizationTreeViewDto> chart() {
        return organizationModuleApi.organizationChart();
    }

    public record StatusRequest(@NotNull Boolean active) {
        OrganizationModels.StatusUpdateCommand toCommand() {
            return new OrganizationModels.StatusUpdateCommand(active);
        }
    }

    public record LegalEntityRequest(
            @NotBlank String legalEntityCode,
            @NotBlank String legalEntityName,
            String shortName,
            String registrationNo,
            String taxNo,
            String countryCode,
            String baseCurrencyCode,
            String defaultLanguageCode,
            @Email String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            @NotNull Boolean active
    ) {
        OrganizationModels.LegalEntityUpsertCommand toCommand() {
            return new OrganizationModels.LegalEntityUpsertCommand(
                    legalEntityCode, legalEntityName, shortName, registrationNo, taxNo, countryCode,
                    baseCurrencyCode, defaultLanguageCode, contactEmail, contactPhone,
                    addressLine1, addressLine2, city, state, postalCode, active);
        }
    }

    public record BranchRequest(
            @NotNull UUID legalEntityId,
            @NotBlank String branchCode,
            @NotBlank String branchName,
            String branchShortName,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            String phone,
            String fax,
            @Email String email,
            @NotNull Boolean active
    ) {
        OrganizationModels.BranchUpsertCommand toCommand() {
            return new OrganizationModels.BranchUpsertCommand(
                    legalEntityId, branchCode, branchName, branchShortName, addressLine1, addressLine2, city, state,
                    countryCode, postalCode, phone, fax, email, active);
        }
    }

    public record BusinessUnitRequest(
            UUID legalEntityId,
            @NotBlank String businessUnitCode,
            @NotBlank String businessUnitName,
            String description,
            @NotNull Boolean active
    ) {
        OrganizationModels.BusinessUnitUpsertCommand toCommand() {
            return new OrganizationModels.BusinessUnitUpsertCommand(legalEntityId, businessUnitCode, businessUnitName, description, active);
        }
    }

    public record DivisionRequest(
            UUID legalEntityId,
            UUID businessUnitId,
            UUID branchId,
            @NotBlank String divisionCode,
            @NotBlank String divisionName,
            String description,
            @NotNull Boolean active
    ) {
        OrganizationModels.DivisionUpsertCommand toCommand() {
            return new OrganizationModels.DivisionUpsertCommand(
                    legalEntityId, businessUnitId, branchId, divisionCode, divisionName, description, active);
        }
    }

    public record DepartmentRequest(
            UUID legalEntityId,
            UUID businessUnitId,
            UUID divisionId,
            UUID branchId,
            @NotBlank String departmentCode,
            @NotBlank String departmentName,
            String shortName,
            String description,
            @NotNull Boolean active
    ) {
        OrganizationModels.DepartmentUpsertCommand toCommand() {
            return new OrganizationModels.DepartmentUpsertCommand(
                    legalEntityId, businessUnitId, divisionId, branchId,
                    departmentCode, departmentName, shortName, description, active);
        }
    }

    public record SectionRequest(
            @NotNull UUID departmentId,
            @NotBlank String sectionCode,
            @NotBlank String sectionName,
            String description,
            @NotNull Boolean active
    ) {
        OrganizationModels.SectionUpsertCommand toCommand() {
            return new OrganizationModels.SectionUpsertCommand(departmentId, sectionCode, sectionName, description, active);
        }
    }

    public record WorkLocationRequest(
            UUID legalEntityId,
            UUID branchId,
            @NotBlank String locationCode,
            @NotBlank String locationName,
            @NotNull OrganizationModels.LocationType locationType,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
            @DecimalMin("0.0") BigDecimal geofenceRadius,
            @NotNull Boolean active
    ) {
        OrganizationModels.WorkLocationUpsertCommand toCommand() {
            return new OrganizationModels.WorkLocationUpsertCommand(
                    legalEntityId, branchId, locationCode, locationName, locationType,
                    addressLine1, addressLine2, city, state, countryCode, postalCode,
                    latitude, longitude, geofenceRadius, active);
        }
    }

    public record CostCenterRequest(
            UUID legalEntityId,
            @NotBlank String costCenterCode,
            @NotBlank String costCenterName,
            String description,
            String glAccountCode,
            UUID parentCostCenterId,
            @NotNull Boolean active
    ) {
        OrganizationModels.CostCenterUpsertCommand toCommand() {
            return new OrganizationModels.CostCenterUpsertCommand(
                    legalEntityId, costCenterCode, costCenterName, description, glAccountCode, parentCostCenterId, active);
        }
    }

    public record ReportingUnitRequest(
            @NotBlank String reportingUnitCode,
            @NotBlank String reportingUnitName,
            UUID parentReportingUnitId,
            String description,
            @NotNull Boolean active
    ) {
        OrganizationModels.ReportingUnitUpsertCommand toCommand() {
            return new OrganizationModels.ReportingUnitUpsertCommand(
                    reportingUnitCode, reportingUnitName, parentReportingUnitId, description, active);
        }
    }
}
