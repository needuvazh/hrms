package com.company.hrms.workflow.domain;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowInstanceDomainTest {

    @Test
    void transitionUpdatesDecisionStateAndTimestamp() {
        Instant submittedAt = Instant.parse("2026-03-01T08:00:00Z");
        Instant decidedAt = Instant.parse("2026-03-01T10:00:00Z");

        WorkflowInstance instance = new WorkflowInstance(
                UUID.randomUUID(),
                "default",
                UUID.randomUUID(),
                "leave.approval",
                "LEAVE_REQUEST",
                "REQ-1",
                ApprovalStatus.SUBMITTED,
                "employee-1",
                null,
                submittedAt,
                null,
                submittedAt,
                submittedAt);

        WorkflowInstance approved = instance.transition(ApprovalStatus.APPROVED, "manager-1", decidedAt);

        assertEquals(ApprovalStatus.APPROVED, approved.status());
        assertEquals("manager-1", approved.decidedBy());
        assertEquals(decidedAt, approved.decidedAt());
        assertEquals(decidedAt, approved.updatedAt());
    }
}
