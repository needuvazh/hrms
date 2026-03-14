package com.company.hrms.notification.repository;

import com.company.hrms.notification.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepository {

    Mono<NotificationDto> save(NotificationDto notification);

    Mono<NotificationDto> update(NotificationDto notification);

    Mono<NotificationDto> findById(UUID notificationId, String tenantId);

    Flux<NotificationDto> findByStatus(String tenantId, NotificationStatus status, int limit);
}
