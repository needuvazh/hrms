package com.company.hrms.organization.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrganizationModels {

    private OrganizationModels() {
    }

    public enum LocationType {
        OFFICE,
        SITE,
        PLANT,
        WAREHOUSE,
        REMOTE,
        CLIENT_SITE
    }

    public record SearchQuery(String q, Boolean active, int limit, int offset) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record LegalEntityUpsertCommand(
            String legalEntityCode,
            String legalEntityName,
            String shortName,
            String registrationNo,
            String taxNo,
            String countryCode,
            String baseCurrencyCode,
            String defaultLanguageCode,
            String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            boolean active
    ) {
    }

    public record LegalEntityDto(
            UUID id,
            String tenantId,
            String legalEntityCode,
            String legalEntityName,
            String shortName,
            String registrationNo,
            String taxNo,
            String countryCode,
            String baseCurrencyCode,
            String defaultLanguageCode,
            String contactEmail,
            String contactPhone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record BranchUpsertCommand(
            UUID legalEntityId,
            String branchCode,
            String branchName,
            String branchShortName,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            String phone,
            String fax,
            String email,
            boolean active
    ) {
    }

    public record BranchDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            String branchCode,
            String branchName,
            String branchShortName,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            String phone,
            String fax,
            String email,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record BusinessUnitUpsertCommand(
            UUID legalEntityId,
            String businessUnitCode,
            String businessUnitName,
            String description,
            boolean active
    ) {
    }

    public record BusinessUnitDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            String businessUnitCode,
            String businessUnitName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record DivisionUpsertCommand(
            UUID legalEntityId,
            UUID businessUnitId,
            UUID branchId,
            String divisionCode,
            String divisionName,
            String description,
            boolean active
    ) {
    }

    public record DivisionDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            UUID businessUnitId,
            UUID branchId,
            String divisionCode,
            String divisionName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record DepartmentUpsertCommand(
            UUID legalEntityId,
            UUID businessUnitId,
            UUID divisionId,
            UUID branchId,
            String departmentCode,
            String departmentName,
            String shortName,
            String description,
            boolean active
    ) {
    }

    public record DepartmentDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            UUID businessUnitId,
            UUID divisionId,
            UUID branchId,
            String departmentCode,
            String departmentName,
            String shortName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record SectionUpsertCommand(
            UUID departmentId,
            String sectionCode,
            String sectionName,
            String description,
            boolean active
    ) {
    }

    public record SectionDto(
            UUID id,
            String tenantId,
            UUID departmentId,
            String sectionCode,
            String sectionName,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record WorkLocationUpsertCommand(
            UUID legalEntityId,
            UUID branchId,
            String locationCode,
            String locationName,
            LocationType locationType,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal geofenceRadius,
            boolean active
    ) {
    }

    public record WorkLocationDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            UUID branchId,
            String locationCode,
            String locationName,
            LocationType locationType,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String countryCode,
            String postalCode,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal geofenceRadius,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record CostCenterUpsertCommand(
            UUID legalEntityId,
            String costCenterCode,
            String costCenterName,
            String description,
            String glAccountCode,
            UUID parentCostCenterId,
            boolean active
    ) {
    }

    public record CostCenterDto(
            UUID id,
            String tenantId,
            UUID legalEntityId,
            String costCenterCode,
            String costCenterName,
            String description,
            String glAccountCode,
            UUID parentCostCenterId,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record ReportingUnitUpsertCommand(
            String reportingUnitCode,
            String reportingUnitName,
            UUID parentReportingUnitId,
            String description,
            boolean active
    ) {
    }

    public record ReportingUnitDto(
            UUID id,
            String tenantId,
            String reportingUnitCode,
            String reportingUnitName,
            UUID parentReportingUnitId,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }

    public record OrganizationNodeViewDto(
            String type,
            UUID id,
            String code,
            String name,
            boolean active,
            List<OrganizationNodeViewDto> children
    ) {
    }

    public record OrganizationTreeViewDto(List<OrganizationNodeViewDto> nodes) {
    }
}
