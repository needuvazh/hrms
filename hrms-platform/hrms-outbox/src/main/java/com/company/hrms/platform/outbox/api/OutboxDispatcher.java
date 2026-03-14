package com.company.hrms.platform.outbox.api;

import reactor.core.publisher.Flux;

public interface OutboxDispatcher {

    Flux<OutboxDispatchResult> dispatchPending(int limit);
}
