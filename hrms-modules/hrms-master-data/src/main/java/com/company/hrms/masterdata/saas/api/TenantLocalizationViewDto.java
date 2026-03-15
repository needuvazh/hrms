package com.company.hrms.masterdata.saas.api;

import java.time.Instant;

public record TenantLocalizationViewDto(
        String id,
        String tenantCode,
        String countryCode,
        String defaultLanguageCode,
        String dateFormat,
        String timeFormat,
        String weekStartDay,
        String currencyCode,
        String numberFormat,
        boolean rtlEnabled,
        String publicHolidayCalendarCode,
        String calendarType,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
