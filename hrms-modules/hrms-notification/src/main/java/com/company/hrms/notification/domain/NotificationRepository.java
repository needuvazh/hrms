package com.company.hrms.notification.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationRepository {

    Mono<Notification> save(Notification notification);

    Mono<Notification> update(Notification notification);

    Mono<Notification> findById(UUID notificationId, String tenantId);

    Flux<Notification> findByStatus(String tenantId, NotificationStatus status, int limit);
}
