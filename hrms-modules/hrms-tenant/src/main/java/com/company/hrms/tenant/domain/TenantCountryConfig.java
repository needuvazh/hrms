package com.company.hrms.tenant.domain;

import java.time.LocalDate;

public record TenantCountryConfig(
        String tenantCode,
        String countryCode,
        boolean primaryCountry,
        String complianceProfile,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {
}
