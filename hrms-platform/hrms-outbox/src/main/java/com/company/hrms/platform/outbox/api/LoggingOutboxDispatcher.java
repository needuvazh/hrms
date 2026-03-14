package com.company.hrms.platform.outbox.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class LoggingOutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxDispatcher.class);

    private final OutboxDispatcher outboxDispatcher;

    public LoggingOutboxDispatcher(OutboxDispatcher outboxDispatcher) {
        this.outboxDispatcher = outboxDispatcher;
    }

    public Flux<OutboxDispatchResult> dispatchAndLog(int limit) {
        int batchSize = limit > 0 ? limit : 100;
        return outboxDispatcher.dispatchPending(batchSize)
                .doOnNext(result -> log.info("Outbox dispatch result eventId={} status={} error={}",
                        result.eventId(), result.status(), result.errorMessage()))
                .doOnError(error -> log.error("Outbox dispatch failed", error));
    }
}
