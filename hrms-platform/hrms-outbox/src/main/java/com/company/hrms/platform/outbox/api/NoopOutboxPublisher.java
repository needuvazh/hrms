package com.company.hrms.platform.outbox.api;

import reactor.core.publisher.Mono;

public class NoopOutboxPublisher implements OutboxPublisher {

    @Override
    public Mono<Void> publish(OutboxEvent event) {
        return Mono.empty();
    }
}
