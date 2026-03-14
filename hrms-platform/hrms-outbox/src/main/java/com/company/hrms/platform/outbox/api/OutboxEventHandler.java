package com.company.hrms.platform.outbox.api;

import reactor.core.publisher.Mono;

public interface OutboxEventHandler {

    boolean supports(OutboxEvent event);

    Mono<Void> handle(OutboxEvent event);
}
