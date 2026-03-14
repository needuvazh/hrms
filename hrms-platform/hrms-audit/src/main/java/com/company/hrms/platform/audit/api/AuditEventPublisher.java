package com.company.hrms.platform.audit.api;

import reactor.core.publisher.Mono;

public interface AuditEventPublisher {

    Mono<Void> publish(AuditEvent event);
}
