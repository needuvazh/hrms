package com.company.hrms.payroll.model;

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
public class EarningComponentDto {
    private final UUID id;
    private final UUID payrollEmployeeRecordId;
    private final String code;
    private final String name;
    private final BigDecimal amount;
    private final Instant createdAt;
}
