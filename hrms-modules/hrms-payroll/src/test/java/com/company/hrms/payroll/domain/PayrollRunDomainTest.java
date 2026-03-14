package com.company.hrms.payroll.domain;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollRunDomainTest {

    @Test
    void finalizeRunTransitionsToFinalizedAndSetsTimestamp() {
        Instant now = Instant.parse("2026-02-01T10:00:00Z");
        PayrollRun run = new PayrollRun(
                UUID.randomUUID(),
                "default",
                UUID.randomUUID(),
                PayrollRunStatus.APPROVED,
                UUID.randomUUID(),
                "payroll-admin",
                "payroll-manager",
                "finance-head",
                null,
                now.minusSeconds(3600),
                now.minusSeconds(1800),
                null,
                now.minusSeconds(7200),
                now.minusSeconds(7200));

        PayrollRun finalized = run.finalizeRun(now);

        assertEquals(PayrollRunStatus.FINALIZED, finalized.status());
        assertEquals(now, finalized.finalizedAt());
        assertEquals(now, finalized.updatedAt());
        assertEquals(run.createdAt(), finalized.createdAt());
    }
}
