package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;

public record TenantCountryUpsertRequest(
        @NotBlank String countryCode,
        String defaultCurrencyCode,
        String defaultTimezone,
        Boolean homeCountry,
        Boolean active
) {
}
