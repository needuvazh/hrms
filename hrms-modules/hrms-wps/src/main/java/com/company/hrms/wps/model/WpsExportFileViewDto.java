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
public class WpsExportFileViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID wpsBatchId;
    private final String exportType;
    private final String fileName;
    private final String contentType;
    private final String contentHash;
    private final String payload;
    private final WpsStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
