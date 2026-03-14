package com.company.hrms.wps.model;

import com.company.hrms.wps.model.WpsStatus;
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
public class WpsBatchViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID payrollRunId;
    private final WpsStatus status;
    private final String validationSummary;
    private final String createdBy;
    private final String exportedBy;
    private final Instant exportedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
