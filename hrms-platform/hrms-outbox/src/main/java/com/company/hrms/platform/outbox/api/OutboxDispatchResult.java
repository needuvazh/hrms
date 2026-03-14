package com.company.hrms.platform.outbox.api;

import com.company.hrms.platform.outbox.domain.OutboxEventStatus;
import java.util.UUID;

public record OutboxDispatchResult(
        UUID eventId,
        OutboxEventStatus status,
        String errorMessage
) {
}
