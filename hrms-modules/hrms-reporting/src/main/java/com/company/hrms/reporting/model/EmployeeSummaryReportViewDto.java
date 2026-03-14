package com.company.hrms.reporting.model;

import java.time.Instant;
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
public class EmployeeSummaryReportViewDto {
    private final String tenantId;
    private final long totalEmployees;
    private final long joinedLast30Days;
    private final Instant generatedAt;
}
