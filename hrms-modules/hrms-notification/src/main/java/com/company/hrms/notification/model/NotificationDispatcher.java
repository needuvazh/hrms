package com.company.hrms.notification.model;

import reactor.core.publisher.Mono;

public interface NotificationDispatcher {

    Mono<NotificationDispatchResultDto> dispatch(NotificationDto notification);
}
