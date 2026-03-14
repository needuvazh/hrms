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
public class AttendanceSummaryReportViewDto {
    private final String tenantId;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final long totalRecords;
    private final long presentCount;
    private final long absentCount;
    private final long missedPunchCount;
    private final long inProgressCount;
    private final Instant generatedAt;
}
