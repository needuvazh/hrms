package com.company.hrms.notification.infrastructure.outbox;

import com.company.hrms.platform.outbox.api.OutboxEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationOutboxEventHandlerTest {

    private final NotificationOutboxEventHandler handler = new NotificationOutboxEventHandler();

    @Test
    void supportsSelectedEventTypes() {
        assertTrue(handler.supports(new OutboxEvent("default", "EMPLOYEE", "1", "EmployeeCreated", "{}", Instant.now())));
        assertTrue(handler.supports(new OutboxEvent("default", "LEAVE_REQUEST", "1", "LeaveRequested", "{}", Instant.now())));
        assertTrue(handler.supports(new OutboxEvent("default", "PAYROLL_RUN", "1", "PayrollFinalized", "{}", Instant.now())));
        assertFalse(handler.supports(new OutboxEvent("default", "EMPLOYEE", "1", "EmployeeRead", "{}", Instant.now())));
    }

    @Test
    void handleCompletesForSupportedEvent() {
        StepVerifier.create(handler.handle(new OutboxEvent(
                        "default",
                        "EMPLOYEE",
                        "1",
                        "EmployeeCreated",
                        "{\"employeeId\":\"1\"}",
                        Instant.now())))
                .verifyComplete();
    }
}
