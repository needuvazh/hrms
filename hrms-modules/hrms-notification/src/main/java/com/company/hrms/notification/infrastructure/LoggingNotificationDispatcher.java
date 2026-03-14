package com.company.hrms.notification.infrastructure;

import com.company.hrms.notification.domain.Notification;
import com.company.hrms.notification.domain.NotificationDispatchResult;
import com.company.hrms.notification.domain.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingNotificationDispatcher implements NotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public Mono<NotificationDispatchResult> dispatch(Notification notification) {
        LOGGER.info(
                "Dispatching notification id={}, tenantId={}, channel={}, recipient={}, referenceType={}, referenceId={}",
                notification.id(),
                notification.tenantId(),
                notification.channel(),
                notification.recipient(),
                notification.referenceType(),
                notification.referenceId());

        return Mono.just(new NotificationDispatchResult(true, "local-log", null));
    }
}
