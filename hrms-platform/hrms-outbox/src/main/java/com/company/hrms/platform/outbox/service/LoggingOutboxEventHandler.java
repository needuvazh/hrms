package com.company.hrms.platform.outbox.service;

import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class LoggingOutboxEventHandler implements OutboxEventHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxEventHandler.class);

    @Override
    public boolean supports(OutboxEvent event) {
        return true;
    }

    @Override
    public Mono<Void> handle(OutboxEvent event) {
        log.info("Outbox event dispatched tenant={} eventType={} aggregateType={} aggregateId={} payload={}",
                event.tenantId(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.payload());
        return Mono.empty();
    }
}
