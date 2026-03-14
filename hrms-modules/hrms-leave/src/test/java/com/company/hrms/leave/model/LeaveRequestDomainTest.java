package com.company.hrms.leave.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaveRequestDomainTest {

    @Test
    void decideUpdatesStatusReviewerAndTimestamp() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant decisionAt = Instant.parse("2026-01-02T00:00:00Z");
        LeaveRequestDto request = new LeaveRequestDto(
                UUID.randomUUID(),
                "default",
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.parse("2026-01-10"),
                LocalDate.parse("2026-01-12"),
                3,
                "Family leave",
                LeaveStatus.SUBMITTED,
                UUID.randomUUID(),
                "employee-1",
                null,
                createdAt,
                createdAt);

        LeaveRequestDto decided = request.decide(LeaveStatus.APPROVED, "manager-1", decisionAt);

        assertEquals(LeaveStatus.APPROVED, decided.status());
        assertEquals("manager-1", decided.reviewedBy());
        assertEquals(decisionAt, decided.updatedAt());
        assertEquals(createdAt, decided.createdAt());
    }
}
