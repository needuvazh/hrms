package com.company.hrms.notification.model;

import java.time.Instant;
import java.util.UUID;

public final class NotificationEvents {

    private NotificationEvents() {
    }

    public record NotificationQueuedEvent(UUID notificationId, String tenantId, Instant occurredAt) {
    }

    public record NotificationDispatchedEvent(UUID notificationId, String tenantId, Instant occurredAt) {
    }

    public record NotificationFailedEvent(UUID notificationId, String tenantId, String reason, Instant occurredAt) {
    }
}
