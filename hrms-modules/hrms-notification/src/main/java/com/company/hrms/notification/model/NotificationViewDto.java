package com.company.hrms.notification.model;

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
public class NotificationViewDto {
    private final UUID id;
    private final String tenantId;
    private final NotificationChannel channel;
    private final String recipient;
    private final String subject;
    private final String body;
    private final String templateCode;
    private final String referenceType;
    private final String referenceId;
    private final NotificationStatus status;
    private final String failureReason;
    private final Instant dispatchedAt;
    private final Instant createdAt;
    private final Instant updatedAt;
}
