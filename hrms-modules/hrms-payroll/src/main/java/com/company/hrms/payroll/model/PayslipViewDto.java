package com.company.hrms.payroll.model;

import java.time.Instant;
import java.util.List;
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
public class PayslipViewDto {
    private final UUID id;
    private final String tenantId;
    private final PayrollEmployeeRecordViewDto payrollEmployeeRecord;
    private final UUID documentRecordId;
    private final String artifactObjectKey;
    private final String artifactContentType;
    private final List<PayrollAmountComponentDto> earnings;
    private final List<PayrollAmountComponentDto> deductions;
    private final Instant generatedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
