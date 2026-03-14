package com.company.hrms.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PayrollEvents {

    private PayrollEvents() {
    }

    public record PayrollRunStartedEvent(UUID payrollRunId, String tenantId, UUID payrollPeriodId, String initiatedBy, Instant occurredAt) {
    }

    public record PayrollRunSubmittedEvent(UUID payrollRunId, String tenantId, UUID workflowInstanceId, String actor, Instant occurredAt) {
    }

    public record PayrollRunReviewedEvent(UUID payrollRunId, String tenantId, String decision, String actor, Instant occurredAt) {
    }

    public record PayrollRecordAttachedEvent(UUID payrollRunId, String tenantId, UUID employeeId, BigDecimal netAmount, Instant occurredAt) {
    }

    public record PayrollRunFinalizedEvent(UUID payrollRunId, String tenantId, Instant occurredAt) {
    }
}
