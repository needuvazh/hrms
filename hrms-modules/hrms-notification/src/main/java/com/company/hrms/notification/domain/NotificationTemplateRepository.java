package com.company.hrms.notification.domain;

import reactor.core.publisher.Mono;

public interface NotificationTemplateRepository {

    Mono<NotificationTemplate> findActiveTemplate(String tenantId, String templateCode, NotificationChannel channel);
}
