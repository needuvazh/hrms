package com.company.hrms.notification.api;

import java.util.Map;

public record CreateNotificationCommand(
        NotificationChannel channel,
        String recipient,
        String subject,
        String body,
        String templateCode,
        Map<String, String> templateVariables,
        String referenceType,
        String referenceId
) {
}
