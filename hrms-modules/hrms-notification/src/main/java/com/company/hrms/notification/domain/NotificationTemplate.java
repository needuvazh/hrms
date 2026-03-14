package com.company.hrms.notification.domain;

import java.time.Instant;

public record NotificationTemplate(
        String tenantId,
        String templateCode,
        NotificationChannel channel,
        String subjectTemplate,
        String bodyTemplate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
