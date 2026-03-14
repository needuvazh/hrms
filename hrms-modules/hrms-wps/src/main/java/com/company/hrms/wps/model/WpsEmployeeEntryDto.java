package com.company.hrms.wps.model;

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
public class WpsEmployeeEntryDto {
    private final UUID id;
    private final String tenantId;
    private final UUID wpsBatchId;
    private final UUID employeeId;
    private final BigDecimal netAmount;
    private final String paymentReference;
    private final Instant createdAt;
}
