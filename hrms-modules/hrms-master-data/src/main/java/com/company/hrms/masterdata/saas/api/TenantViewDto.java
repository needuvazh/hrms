package com.company.hrms.masterdata.saas.api;

import java.time.Instant;
import java.time.LocalDate;

public record TenantViewDto(
        String tenantCode,
        String tenantName,
        String legalName,
        String contactEmail,
        String contactPhone,
        String defaultTimezone,
        LocalDate goLiveDate,
        String defaultLanguageCode,
        String homeCountryCode,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
