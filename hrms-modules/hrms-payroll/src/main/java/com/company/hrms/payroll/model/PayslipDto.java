package com.company.hrms.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PayslipDto(
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

    public PayslipDto withArtifact(UUID newDocumentRecordId, String newArtifactObjectKey, String newArtifactContentType, Instant at) {
        return new PayslipDto(
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
