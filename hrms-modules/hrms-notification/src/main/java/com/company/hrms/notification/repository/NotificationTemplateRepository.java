package com.company.hrms.notification.repository;

import com.company.hrms.notification.model.*;

import reactor.core.publisher.Mono;

public interface NotificationTemplateRepository {

    Mono<NotificationTemplateDto> findActiveTemplate(String tenantId, String templateCode, NotificationChannel channel);
}
