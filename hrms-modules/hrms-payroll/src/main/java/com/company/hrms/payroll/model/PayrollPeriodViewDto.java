package com.company.hrms.payroll.model;

import java.time.Instant;
import java.time.LocalDate;
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
public class PayrollPeriodViewDto {
    private final UUID id;
    private final String tenantId;
    private final String periodCode;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final PayrollPeriodStatus status;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;
}
