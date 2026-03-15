package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;

public record TenantLocalizationUpsertRequest(
        @NotBlank String defaultLanguageCode,
        @NotBlank String dateFormat,
        @NotBlank String timeFormat,
        @NotBlank String weekStartDay,
        @NotBlank String currencyCode,
        @NotBlank String numberFormat,
        Boolean rtlEnabled,
        String publicHolidayCalendarCode,
        String calendarType,
        Boolean active
) {
}
