package com.company.hrms.platform.audit.api;

import reactor.core.publisher.Mono;

public class NoopAuditEventPublisher implements AuditEventPublisher {

    @Override
    public Mono<Void> publish(AuditEvent event) {
        return Mono.empty();
    }
}
