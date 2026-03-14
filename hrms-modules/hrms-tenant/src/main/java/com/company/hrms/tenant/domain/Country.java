package com.company.hrms.tenant.domain;

public record Country(
        String countryCode,
        String countryName,
        String currencyCode,
        String timezone,
        String locale,
        boolean active
) {
}
