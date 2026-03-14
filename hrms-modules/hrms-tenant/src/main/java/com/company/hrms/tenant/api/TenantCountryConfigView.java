package com.company.hrms.tenant.api;

import java.time.LocalDate;

public record TenantCountryConfigView(
        String tenantCode,
        String countryCode,
        boolean primaryCountry,
        String complianceProfile,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active
) {
}
