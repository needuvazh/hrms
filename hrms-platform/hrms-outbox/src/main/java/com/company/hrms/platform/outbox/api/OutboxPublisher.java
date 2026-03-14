package com.company.hrms.platform.outbox.api;

import reactor.core.publisher.Mono;

public interface OutboxPublisher {

    Mono<Void> publish(OutboxEvent event);
}
