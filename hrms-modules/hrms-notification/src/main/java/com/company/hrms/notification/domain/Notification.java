package com.company.hrms.notification.domain;

import java.time.Instant;
import java.util.UUID;

public record Notification(
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

    public Notification markDispatched(Instant at) {
        return new Notification(
                id,
                tenantId,
                channel,
                recipient,
                subject,
                body,
                templateCode,
                referenceType,
                referenceId,
                NotificationStatus.DISPATCHED,
                null,
                at,
                createdAt,
                at);
    }

    public Notification markFailed(String reason, Instant at) {
        return new Notification(
                id,
                tenantId,
                channel,
                recipient,
                subject,
                body,
                templateCode,
                referenceType,
                referenceId,
                NotificationStatus.FAILED,
                reason,
                null,
                createdAt,
                at);
    }
}
