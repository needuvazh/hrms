package com.company.hrms.person.model;

import java.time.Instant;
import java.util.UUID;

public final class PersonEvents {

    private PersonEvents() {
    }

    public record PersonCreatedEvent(UUID personId, String tenantId, Instant occurredAt) {
    }
}
