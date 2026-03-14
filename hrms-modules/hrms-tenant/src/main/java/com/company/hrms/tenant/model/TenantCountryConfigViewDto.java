package com.company.hrms.tenant.model;

import java.time.LocalDate;
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
public class TenantCountryConfigViewDto {
    private final String tenantCode;
    private final String countryCode;
    private final boolean primaryCountry;
    private final String complianceProfile;
    private final LocalDate effectiveFrom;
    private final LocalDate effectiveTo;
    private final boolean active;
}
