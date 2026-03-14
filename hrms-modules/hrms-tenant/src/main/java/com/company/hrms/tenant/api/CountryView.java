package com.company.hrms.tenant.api;

public record CountryView(
        String countryCode,
        String countryName,
        String currencyCode,
        String timezone,
        String locale,
        boolean active
) {
}
