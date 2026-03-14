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
public class PasiEmployeeContributionDto {
    private final UUID id;
    private final String tenantId;
    private final UUID pasiPeriodRecordId;
    private final UUID payrollEmployeeRecordId;
    private final UUID employeeId;
    private final BigDecimal contributableSalary;
    private final BigDecimal employeeContribution;
    private final BigDecimal employerContribution;
    private final BigDecimal totalContribution;
    private final Instant createdAt;
}
