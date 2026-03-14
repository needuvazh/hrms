package com.company.hrms.notification.api;

import java.time.Instant;
import java.util.UUID;

public record NotificationView(
        UUID id,
        String tenantId,
        NotificationChannel channel,
        String recipient,
        String subject,
        String body,
        String templateCode,
        String referenceType,
        String referenceId,
        NotificationStatus status,
        String failureReason,
        Instant dispatchedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
