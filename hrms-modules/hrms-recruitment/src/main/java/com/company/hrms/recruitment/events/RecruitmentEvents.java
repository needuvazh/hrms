package com.company.hrms.recruitment.events;

import java.time.Instant;
import java.util.UUID;

public final class RecruitmentEvents {

    private RecruitmentEvents() {
    }

    public record CandidateCreatedEvent(UUID candidateId, UUID personId, String tenantId, Instant occurredAt) {
    }

    public record CandidateHiredEvent(UUID candidateId, UUID personId, String employeeCode, String tenantId, Instant occurredAt) {
    }
}
