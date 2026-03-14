package com.company.hrms.notification.application;

import com.company.hrms.notification.api.CreateNotificationCommand;
import com.company.hrms.notification.api.NotificationModuleApi;
import com.company.hrms.notification.api.NotificationView;
import com.company.hrms.notification.api.NotificationStatus;
import com.company.hrms.notification.domain.Notification;
import com.company.hrms.notification.domain.NotificationDispatchResult;
import com.company.hrms.notification.domain.NotificationDispatcher;
import com.company.hrms.notification.domain.NotificationRepository;
import com.company.hrms.notification.domain.NotificationTemplate;
import com.company.hrms.notification.domain.NotificationTemplateRepository;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationApplicationService implements NotificationModuleApi {

    private static final int DEFAULT_DISPATCH_LIMIT = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final TenantContextAccessor tenantContextAccessor;
    private final MeterRegistry meterRegistry;

    public NotificationApplicationService(
            NotificationRepository notificationRepository,
            NotificationTemplateRepository notificationTemplateRepository,
            NotificationDispatcher notificationDispatcher,
            TenantContextAccessor tenantContextAccessor,
            MeterRegistry meterRegistry
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationDispatcher = notificationDispatcher;
        this.tenantContextAccessor = tenantContextAccessor;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<NotificationView> createNotification(CreateNotificationCommand command) {
        validateCommand(command);
        return requireTenant()
                .flatMap(tenantId -> resolveTemplate(command, tenantId)
                        .defaultIfEmpty(templateFromCommand(command))
                        .flatMap(resolvedTemplate -> {
                            Instant now = Instant.now();
                            Notification notification = new Notification(
                                    UUID.randomUUID(),
                                    tenantId,
                                    toDomainChannel(command.channel()),
                                    command.recipient(),
                                    resolveSubject(command.subject(), resolvedTemplate.subjectTemplate(), command.templateVariables()),
                                    resolveBody(command.body(), resolvedTemplate.bodyTemplate(), command.templateVariables()),
                                    command.templateCode(),
                                    command.referenceType(),
                                    command.referenceId(),
                                    com.company.hrms.notification.domain.NotificationStatus.QUEUED,
                                    null,
                                    null,
                                    now,
                                    now);
                            return notificationRepository.save(notification)
                                    .doOnNext(saved -> meterRegistry.counter(
                                                    "hrms.notification.created",
                                                    "channel", saved.channel().name())
                                            .increment())
                                    .map(this::toView);
                        }));
    }

    @Override
    public Flux<NotificationView> dispatchQueuedNotifications(int limit) {
        int dispatchLimit = limit > 0 ? limit : DEFAULT_DISPATCH_LIMIT;
        return requireTenant().flatMapMany(tenantId -> notificationRepository
                .findByStatus(tenantId, com.company.hrms.notification.domain.NotificationStatus.QUEUED, dispatchLimit)
                .concatMap(notification -> notificationDispatcher.dispatch(notification)
                        .flatMap(result -> updateDispatchStatus(notification, result)
                                .doOnNext(updated -> recordDispatchMetrics(notification.channel().name(), updated.status())))
                        .map(this::toView)));
    }

    @Override
    public Mono<NotificationView> getNotification(UUID notificationId) {
        return requireTenant()
                .flatMap(tenantId -> notificationRepository.findById(notificationId, tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(
                                HttpStatus.NOT_FOUND,
                                "NOTIFICATION_NOT_FOUND",
                                "Notification not found")))
                        .map(this::toView));
    }

    private Mono<Notification> updateDispatchStatus(Notification notification, NotificationDispatchResult dispatchResult) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Instant now = Instant.now();
        Notification updated = dispatchResult.success()
                ? notification.markDispatched(now)
                : notification.markFailed(
                        StringUtils.hasText(dispatchResult.errorMessage())
                                ? dispatchResult.errorMessage()
                                : "Notification dispatch failed",
                        now);
        return notificationRepository.update(updated)
                .doOnSuccess(saved -> sample.stop(Timer.builder("hrms.notification.dispatch.duration")
                        .tag("channel", notification.channel().name())
                        .tag("status", saved.status().name())
                        .register(meterRegistry)));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "TENANT_REQUIRED",
                        "Tenant is required")));
    }

    private void validateCommand(CreateNotificationCommand command) {
        if (command.channel() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "CHANNEL_REQUIRED", "Notification channel is required");
        }
        if (!StringUtils.hasText(command.recipient())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "RECIPIENT_REQUIRED", "Notification recipient is required");
        }
        boolean hasTemplate = StringUtils.hasText(command.templateCode());
        boolean hasBody = StringUtils.hasText(command.body());
        if (!hasTemplate && !hasBody) {
            throw new HrmsException(
                    HttpStatus.BAD_REQUEST,
                    "BODY_OR_TEMPLATE_REQUIRED",
                    "Notification body or template is required");
        }
    }

    private Mono<NotificationTemplate> resolveTemplate(CreateNotificationCommand command, String tenantId) {
        if (!StringUtils.hasText(command.templateCode())) {
            return Mono.empty();
        }
        return notificationTemplateRepository.findActiveTemplate(tenantId, command.templateCode(), toDomainChannel(command.channel()))
                .switchIfEmpty(Mono.error(new HrmsException(
                        HttpStatus.BAD_REQUEST,
                        "TEMPLATE_NOT_FOUND",
                        "Notification template not found: " + command.templateCode())));
    }

    private NotificationTemplate templateFromCommand(CreateNotificationCommand command) {
        return new NotificationTemplate(
                null,
                command.templateCode(),
                toDomainChannel(command.channel()),
                command.subject(),
                command.body(),
                true,
                Instant.now(),
                Instant.now());
    }

    private String resolveSubject(String subject, String subjectTemplate, Map<String, String> variables) {
        String raw = StringUtils.hasText(subject) ? subject : subjectTemplate;
        return render(raw, variables);
    }

    private String resolveBody(String body, String bodyTemplate, Map<String, String> variables) {
        String raw = StringUtils.hasText(body) ? body : bodyTemplate;
        if (!StringUtils.hasText(raw)) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "BODY_REQUIRED", "Notification body is required");
        }
        return render(raw, variables);
    }

    private String render(String template, Map<String, String> variables) {
        if (!StringUtils.hasText(template)) {
            return template;
        }

        String rendered = template;
        Map<String, String> values = variables == null ? Collections.emptyMap() : variables;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return rendered;
    }

    private NotificationView toView(Notification notification) {
        return new NotificationView(
                notification.id(),
                notification.tenantId(),
                toApiChannel(notification.channel()),
                notification.recipient(),
                notification.subject(),
                notification.body(),
                notification.templateCode(),
                notification.referenceType(),
                notification.referenceId(),
                toApiStatus(notification.status()),
                notification.failureReason(),
                notification.dispatchedAt(),
                notification.createdAt(),
                notification.updatedAt());
    }

    private void recordDispatchMetrics(String channel, com.company.hrms.notification.domain.NotificationStatus status) {
        meterRegistry.counter(
                "hrms.notification.dispatch",
                "channel", channel,
                "status", status.name())
                .increment();
    }

    private com.company.hrms.notification.domain.NotificationChannel toDomainChannel(com.company.hrms.notification.api.NotificationChannel channel) {
        return com.company.hrms.notification.domain.NotificationChannel.valueOf(channel.name());
    }

    private com.company.hrms.notification.api.NotificationChannel toApiChannel(com.company.hrms.notification.domain.NotificationChannel channel) {
        return com.company.hrms.notification.api.NotificationChannel.valueOf(channel.name());
    }

    private NotificationStatus toApiStatus(com.company.hrms.notification.domain.NotificationStatus status) {
        return NotificationStatus.valueOf(status.name());
    }
}
