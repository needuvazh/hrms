package com.company.hrms.tenant.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CountryViewDto {
    private final String countryCode;
    private final String countryName;
    private final String currencyCode;
    private final String timezone;
    private final String locale;
    private final boolean active;
}
