package com.company.hrms.payroll.model;

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
public class PayrollRunViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollPeriodId;
    private final PayrollRunStatus status;
    private final UUID workflowInstanceId;
    private final String initiatedBy;
    private final String submittedBy;
    private final String reviewedBy;
    private final String notes;
    private final Instant submittedAt;
    private final Instant reviewedAt;
    private final Instant finalizedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
