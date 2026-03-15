package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record TenantUpsertRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9_-]{2,64}$")
        String tenantCode,
        @NotBlank String tenantName,
        @NotBlank String legalName,
        @Email String contactEmail,
        String contactPhone,
        @NotBlank String defaultTimezone,
        LocalDate goLiveDate,
        String defaultLanguageCode,
        String homeCountryCode
) {
}
