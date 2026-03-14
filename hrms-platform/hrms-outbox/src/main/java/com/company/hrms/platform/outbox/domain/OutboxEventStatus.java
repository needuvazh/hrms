package com.company.hrms.platform.outbox.domain;

public enum OutboxEventStatus {
    PENDING,
    DISPATCHED,
    FAILED
}
