package com.company.hrms.masterdata.events;

import java.time.Instant;

public record MasterDataEvents(
        String lookupType,
        String eventType,
        Instant occurredAt
) {
}
