package com.company.hrms.pasi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
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
public class PasiContributionRuleDto {
    private final UUID id;
    private final String tenantId;
    private final String ruleCode;
    private final String name;
    private final BigDecimal employeeRatePercent;
    private final BigDecimal employerRatePercent;
    private final BigDecimal salaryCap;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
