package com.company.hrms.notification.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationModuleApi {

    Mono<NotificationView> createNotification(CreateNotificationCommand command);

    Flux<NotificationView> dispatchQueuedNotifications(int limit);

    Mono<NotificationView> getNotification(UUID notificationId);
}
