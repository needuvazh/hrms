package com.company.hrms.payroll.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Payslip(
        UUID id,
        String tenantId,
        UUID payrollRunId,
        UUID payrollEmployeeRecordId,
        UUID employeeId,
        UUID documentRecordId,
        String artifactObjectKey,
        String artifactContentType,
        BigDecimal grossAmount,
        BigDecimal totalDeductionAmount,
        BigDecimal netAmount,
        Instant generatedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public Payslip withArtifact(UUID newDocumentRecordId, String newArtifactObjectKey, String newArtifactContentType, Instant at) {
        return new Payslip(
                id,
                tenantId,
                payrollRunId,
                payrollEmployeeRecordId,
                employeeId,
                newDocumentRecordId,
                newArtifactObjectKey,
                newArtifactContentType,
                grossAmount,
                totalDeductionAmount,
                netAmount,
                generatedAt,
                createdAt,
                at);
    }
}
