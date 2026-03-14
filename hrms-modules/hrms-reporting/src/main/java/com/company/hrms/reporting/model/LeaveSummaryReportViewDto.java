package com.company.hrms.reporting.model;

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
public class LeaveSummaryReportViewDto {
    private final String tenantId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final long totalRequests;
    private final long submittedCount;
    private final long approvedCount;
    private final long rejectedCount;
    private final long cancelledCount;
    private final long totalRequestedDays;
    private final Instant generatedAt;
}
