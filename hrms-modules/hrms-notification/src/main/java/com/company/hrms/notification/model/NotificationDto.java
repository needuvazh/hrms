package com.company.hrms.notification.model;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
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

    public NotificationDto markDispatched(Instant at) {
        return new NotificationDto(
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

    public NotificationDto markFailed(String reason, Instant at) {
        return new NotificationDto(
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
