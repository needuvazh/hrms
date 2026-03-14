package com.company.hrms.platform.audit.service;

import com.company.hrms.platform.audit.api.AuditEvent;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAuditEventPublisherTest {

    @Test
    void publishesAppendOnlyAuditEvent() {
        AtomicReference<AuditEvent> captured = new AtomicReference<>();
        AuditEventRepository repository = event -> {
            captured.set(event);
            return Mono.empty();
        };

        DefaultAuditEventPublisher publisher = new DefaultAuditEventPublisher(repository);
        AuditEvent event = new AuditEvent(
                "admin",
                "default",
                "EMPLOYEE_CREATED",
                "EMPLOYEE",
                "EMP-101",
                Instant.parse("2026-03-10T10:00:00Z"),
                Map.of("email", "emp@example.com"));

        StepVerifier.create(publisher.publish(event)).verifyComplete();

        assertEquals("admin", captured.get().actor());
        assertEquals("default", captured.get().tenant());
        assertEquals("EMPLOYEE_CREATED", captured.get().action());
        assertEquals("EMPLOYEE", captured.get().targetType());
        assertEquals("EMP-101", captured.get().targetId());
        assertEquals("emp@example.com", captured.get().metadata().get("email"));
    }
}
