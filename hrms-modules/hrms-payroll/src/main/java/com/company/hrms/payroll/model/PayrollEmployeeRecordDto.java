package com.company.hrms.payroll.model;

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
public class PayrollEmployeeRecordDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollRunId;
    private final UUID employeeId;
    private final BigDecimal grossAmount;
    private final BigDecimal totalDeductionAmount;
    private final BigDecimal netAmount;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
