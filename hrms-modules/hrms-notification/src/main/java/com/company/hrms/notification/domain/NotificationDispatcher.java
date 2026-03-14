package com.company.hrms.notification.domain;

import reactor.core.publisher.Mono;

public interface NotificationDispatcher {

    Mono<NotificationDispatchResult> dispatch(Notification notification);
}
