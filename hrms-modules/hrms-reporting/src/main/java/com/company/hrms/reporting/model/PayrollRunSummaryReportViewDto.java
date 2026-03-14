package com.company.hrms.reporting.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
public class PayrollRunSummaryReportViewDto {
    private final String tenantId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final long totalRuns;
    private final long draftRuns;
    private final long submittedRuns;
    private final long approvedRuns;
    private final long rejectedRuns;
    private final long finalizedRuns;
    private final BigDecimal totalGrossAmount;
    private final BigDecimal totalNetAmount;
    private final Instant generatedAt;
}
