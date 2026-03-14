package com.company.hrms.notification.model;

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
public class NotificationTemplateDto {
    private final String tenantId;
    private final String templateCode;
    private final NotificationChannel channel;
    private final String subjectTemplate;
    private final String bodyTemplate;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
