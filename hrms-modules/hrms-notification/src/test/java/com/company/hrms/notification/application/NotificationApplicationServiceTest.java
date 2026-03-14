package com.company.hrms.notification.application;

import com.company.hrms.notification.api.CreateNotificationCommand;
import com.company.hrms.notification.domain.Notification;
import com.company.hrms.notification.api.NotificationChannel;
import com.company.hrms.notification.domain.NotificationDispatchResult;
import com.company.hrms.notification.domain.NotificationDispatcher;
import com.company.hrms.notification.domain.NotificationRepository;
import com.company.hrms.notification.api.NotificationStatus;
import com.company.hrms.notification.domain.NotificationTemplate;
import com.company.hrms.notification.domain.NotificationTemplateRepository;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NotificationApplicationServiceTest {

    private final InMemoryNotificationRepository notificationRepository = new InMemoryNotificationRepository();
    private final InMemoryNotificationTemplateRepository templateRepository = new InMemoryNotificationTemplateRepository();
    private final RecordingNotificationDispatcher notificationDispatcher = new RecordingNotificationDispatcher();
    private final NotificationApplicationService notificationApplicationService = new NotificationApplicationService(
            notificationRepository,
            templateRepository,
            notificationDispatcher,
            new DefaultTenantContextAccessor(),
            new SimpleMeterRegistry());

    @Test
    void createNotificationUsesTemplateAndQueuesForCurrentTenant() {
        templateRepository.seed(new NotificationTemplate(
                null,
                "leave.submitted",
                com.company.hrms.notification.domain.NotificationChannel.EMAIL,
                "Leave request submitted",
                "Request {{requestId}} submitted by {{employeeName}}",
                true,
                Instant.now(),
                Instant.now()));

        CreateNotificationCommand command = new CreateNotificationCommand(
                NotificationChannel.EMAIL,
                "manager@hrms.local",
                null,
                null,
                "leave.submitted",
                Map.of("requestId", "REQ-101", "employeeName", "Alice"),
                "LEAVE_REQUEST",
                "REQ-101");

        StepVerifier.create(notificationApplicationService.createNotification(command)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view ->
                        "tenant-a".equals(view.tenantId())
                                && NotificationStatus.QUEUED == view.status()
                                && view.body().contains("REQ-101")
                                && view.body().contains("Alice"))
                .verifyComplete();
    }

    @Test
    void dispatchQueuedNotificationsUsesTenantIsolation() {
        Notification tenantANotification = queuedNotification("tenant-a", "alice@a.hrms");
        Notification tenantBNotification = queuedNotification("tenant-b", "bob@b.hrms");

        notificationRepository.seed(tenantANotification);
        notificationRepository.seed(tenantBNotification);

        StepVerifier.create(notificationApplicationService.dispatchQueuedNotifications(10)
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-a")))
                .expectNextMatches(view -> NotificationStatus.DISPATCHED == view.status()
                        && "tenant-a".equals(view.tenantId()))
                .verifyComplete();

        assertEquals(1, notificationDispatcher.dispatchedNotificationIds.size());
        assertEquals(tenantANotification.id(), notificationDispatcher.dispatchedNotificationIds.getFirst());

        StepVerifier.create(notificationApplicationService.getNotification(tenantBNotification.id())
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .expectNextMatches(view -> NotificationStatus.QUEUED == view.status())
                .verifyComplete();
    }

    @Test
    void createNotificationFailsWithoutTenant() {
        CreateNotificationCommand command = new CreateNotificationCommand(
                NotificationChannel.SMS,
                "+971500000001",
                null,
                "Test body",
                null,
                Collections.emptyMap(),
                "TEST",
                "1");

        StepVerifier.create(notificationApplicationService.createNotification(command))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException hrmsException = (HrmsException) error;
                    assertEquals("TENANT_REQUIRED", hrmsException.getErrorCode());
                })
                .verify();
    }

    private Notification queuedNotification(String tenantId, String recipient) {
        Instant now = Instant.now();
        return new Notification(
                UUID.randomUUID(),
                tenantId,
                com.company.hrms.notification.domain.NotificationChannel.EMAIL,
                recipient,
                "Hello",
                "Queued notification",
                null,
                "REFERENCE",
                "REF-1",
                com.company.hrms.notification.domain.NotificationStatus.QUEUED,
                null,
                null,
                now,
                now);
    }

    static class InMemoryNotificationRepository implements NotificationRepository {

        private final Map<UUID, Notification> storage = new ConcurrentHashMap<>();

        @Override
        public Mono<Notification> save(Notification notification) {
            storage.put(notification.id(), notification);
            return Mono.just(notification);
        }

        @Override
        public Mono<Notification> update(Notification notification) {
            storage.put(notification.id(), notification);
            return Mono.just(notification);
        }

        @Override
        public Mono<Notification> findById(UUID notificationId, String tenantId) {
            Notification notification = storage.get(notificationId);
            if (notification == null || !tenantId.equals(notification.tenantId())) {
                return Mono.empty();
            }
            return Mono.just(notification);
        }

        @Override
        public Flux<Notification> findByStatus(String tenantId, com.company.hrms.notification.domain.NotificationStatus status, int limit) {
            return Flux.fromIterable(storage.values())
                    .filter(notification -> tenantId.equals(notification.tenantId()))
                    .filter(notification -> status == notification.status())
                    .take(limit);
        }

        void seed(Notification notification) {
            storage.put(notification.id(), notification);
        }
    }

    static class InMemoryNotificationTemplateRepository implements NotificationTemplateRepository {

        private final List<NotificationTemplate> templates = new ArrayList<>();

        @Override
        public Mono<NotificationTemplate> findActiveTemplate(String tenantId, String templateCode, com.company.hrms.notification.domain.NotificationChannel channel) {
            return Flux.fromIterable(templates)
                    .filter(template -> template.active())
                    .filter(template -> templateCode.equals(template.templateCode()))
                    .filter(template -> channel == template.channel())
                    .filter(template -> template.tenantId() == null || tenantId.equals(template.tenantId()))
                    .sort((first, second) -> {
                        if (first.tenantId() == null && second.tenantId() != null) {
                            return 1;
                        }
                        if (first.tenantId() != null && second.tenantId() == null) {
                            return -1;
                        }
                        return 0;
                    })
                    .next();
        }

        void seed(NotificationTemplate template) {
            templates.add(template);
        }
    }

    static class RecordingNotificationDispatcher implements NotificationDispatcher {

        private final List<UUID> dispatchedNotificationIds = new ArrayList<>();

        @Override
        public Mono<NotificationDispatchResult> dispatch(Notification notification) {
            dispatchedNotificationIds.add(notification.id());
            return Mono.just(new NotificationDispatchResult(true, "in-memory", null));
        }
    }
}
