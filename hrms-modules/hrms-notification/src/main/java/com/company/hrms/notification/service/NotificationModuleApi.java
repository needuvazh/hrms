package com.company.hrms.notification.service;

import com.company.hrms.notification.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationModuleApi {

    Mono<NotificationViewDto> createNotification(CreateNotificationCommandDto command);

    Flux<NotificationViewDto> dispatchQueuedNotifications(int limit);

    Mono<NotificationViewDto> getNotification(UUID notificationId);
}
