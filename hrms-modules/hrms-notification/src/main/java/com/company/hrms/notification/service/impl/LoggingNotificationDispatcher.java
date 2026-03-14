package com.company.hrms.notification.service.impl;

import com.company.hrms.notification.model.*;
import com.company.hrms.notification.repository.*;
import com.company.hrms.notification.service.*;

import com.company.hrms.notification.model.NotificationDto;
import com.company.hrms.notification.model.NotificationDispatchResultDto;
import com.company.hrms.notification.model.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingNotificationDispatcher implements NotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public Mono<NotificationDispatchResultDto> dispatch(NotificationDto notification) {
        LOGGER.info(
                "Dispatching notification id={}, tenantId={}, channel={}, recipient={}, referenceType={}, referenceId={}",
                notification.id(),
                notification.tenantId(),
                notification.channel(),
                notification.recipient(),
                notification.referenceType(),
                notification.referenceId());

        return Mono.just(new NotificationDispatchResultDto(true, "local-log", null));
    }
}
