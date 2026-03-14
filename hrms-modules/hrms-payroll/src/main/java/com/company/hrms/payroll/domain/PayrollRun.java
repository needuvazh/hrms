package com.company.hrms.payroll.domain;

import java.time.Instant;
import java.util.UUID;

public record PayrollRun(
        UUID id,
        String tenantId,
        UUID payrollPeriodId,
        PayrollRunStatus status,
        UUID workflowInstanceId,
        String initiatedBy,
        String submittedBy,
        String reviewedBy,
        String notes,
        Instant submittedAt,
        Instant reviewedAt,
        Instant finalizedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public PayrollRun submit(UUID newWorkflowInstanceId, String actor, Instant at) {
        return new PayrollRun(
                id,
                tenantId,
                payrollPeriodId,
                PayrollRunStatus.SUBMITTED,
                newWorkflowInstanceId,
                initiatedBy,
                actor,
                reviewedBy,
                notes,
                at,
                reviewedAt,
                finalizedAt,
                createdAt,
                at);
    }

    public PayrollRun approve(String actor, Instant at) {
        return new PayrollRun(
                id,
                tenantId,
                payrollPeriodId,
                PayrollRunStatus.APPROVED,
                workflowInstanceId,
                initiatedBy,
                submittedBy,
                actor,
                notes,
                submittedAt,
                at,
                finalizedAt,
                createdAt,
                at);
    }

    public PayrollRun reject(String actor, Instant at) {
        return new PayrollRun(
                id,
                tenantId,
                payrollPeriodId,
                PayrollRunStatus.REJECTED,
                workflowInstanceId,
                initiatedBy,
                submittedBy,
                actor,
                notes,
                submittedAt,
                at,
                finalizedAt,
                createdAt,
                at);
    }

    public PayrollRun finalizeRun(Instant at) {
        return new PayrollRun(
                id,
                tenantId,
                payrollPeriodId,
                PayrollRunStatus.FINALIZED,
                workflowInstanceId,
                initiatedBy,
                submittedBy,
                reviewedBy,
                notes,
                submittedAt,
                reviewedAt,
                at,
                createdAt,
                at);
    }
}
