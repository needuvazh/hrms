package com.company.hrms.masterdata.saas.api;

import java.time.Instant;
import java.util.UUID;

public record TenantCountryViewDto(
        UUID id,
        String tenantCode,
        String countryCode,
        String countryName,
        String defaultCurrencyCode,
        String defaultTimezone,
        boolean homeCountry,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
