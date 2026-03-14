package com.company.hrms.platform.audit.service;

import com.company.hrms.platform.audit.api.AuditEvent;
import reactor.core.publisher.Mono;

public interface AuditEventRepository {

    Mono<Void> append(AuditEvent event);
}
