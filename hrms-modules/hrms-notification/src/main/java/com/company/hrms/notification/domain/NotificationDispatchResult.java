package com.company.hrms.notification.domain;

public record NotificationDispatchResult(
        boolean success,
        String providerReference,
        String errorMessage
) {
}
