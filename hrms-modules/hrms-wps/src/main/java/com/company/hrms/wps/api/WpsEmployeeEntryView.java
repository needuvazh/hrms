package com.company.hrms.wps.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WpsEmployeeEntryView(
        UUID id,
        String tenantId,
        UUID wpsBatchId,
        UUID employeeId,
        BigDecimal netAmount,
        String paymentReference,
        Instant createdAt
) {
}
