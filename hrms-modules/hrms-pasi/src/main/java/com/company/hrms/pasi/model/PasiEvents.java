package com.company.hrms.pasi.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PasiEvents {

    private PasiEvents() {
    }

    public record PasiRuleDefinedEvent(UUID ruleId, String tenantId, String ruleCode, Instant occurredAt) {
    }

    public record PasiPeriodCalculatedEvent(UUID periodRecordId, String tenantId, UUID payrollRunId, BigDecimal totalContribution, Instant occurredAt) {
    }
}
