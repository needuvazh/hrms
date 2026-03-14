package com.company.hrms.pasi.model;

import com.company.hrms.pasi.model.PasiStatus;
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
public class PasiPeriodRecordViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollRunId;
    private final String periodCode;
    private final UUID contributionRuleId;
    private final PasiStatus status;
    private final int totalEmployees;
    private final BigDecimal totalEmployeeContribution;
    private final BigDecimal totalEmployerContribution;
    private final BigDecimal totalContribution;
    private final String calculatedBy;
    private final Instant calculatedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
