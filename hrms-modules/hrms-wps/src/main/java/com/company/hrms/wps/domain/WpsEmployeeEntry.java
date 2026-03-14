package com.company.hrms.wps.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WpsEmployeeEntry(
        UUID id,
        String tenantId,
        UUID wpsBatchId,
        UUID employeeId,
        BigDecimal netAmount,
        String paymentReference,
        Instant createdAt
) {
}
