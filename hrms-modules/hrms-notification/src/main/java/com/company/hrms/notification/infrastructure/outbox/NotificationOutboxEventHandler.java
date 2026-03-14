package com.company.hrms.notification.infrastructure.outbox;

import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxEventHandler;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class NotificationOutboxEventHandler implements OutboxEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxEventHandler.class);
    private static final Set<String> SUPPORTED_EVENTS = Set.of("EmployeeCreated", "LeaveRequested", "PayrollFinalized");

    @Override
    public boolean supports(OutboxEvent event) {
        return SUPPORTED_EVENTS.contains(event.eventType());
    }

    @Override
    public Mono<Void> handle(OutboxEvent event) {
        log.info("Notification outbox handler received eventType={} tenant={} aggregateId={} payload={}",
                event.eventType(),
                event.tenantId(),
                event.aggregateId(),
                event.payload());
        return Mono.empty();
    }
}
