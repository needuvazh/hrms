package com.company.hrms.payroll.model;

import java.time.Instant;
import java.util.UUID;

public record PayrollRunDto(
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

    public PayrollRunDto submit(UUID newWorkflowInstanceId, String actor, Instant at) {
        return new PayrollRunDto(
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

    public PayrollRunDto approve(String actor, Instant at) {
        return new PayrollRunDto(
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

    public PayrollRunDto reject(String actor, Instant at) {
        return new PayrollRunDto(
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

    public PayrollRunDto finalizeRun(Instant at) {
        return new PayrollRunDto(
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
