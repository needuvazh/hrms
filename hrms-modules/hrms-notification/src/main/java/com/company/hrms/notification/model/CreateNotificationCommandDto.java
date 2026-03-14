package com.company.hrms.notification.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateNotificationCommandDto {
    private final NotificationChannel channel;
    private final String recipient;
    private final String subject;
    private final String body;
    private final String templateCode;
    private final Map<String, String> templateVariables;
    private final String referenceType;
    private final String referenceId;
}
