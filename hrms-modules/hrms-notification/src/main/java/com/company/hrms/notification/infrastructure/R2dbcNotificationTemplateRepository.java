package com.company.hrms.notification.infrastructure;

import com.company.hrms.notification.domain.NotificationChannel;
import com.company.hrms.notification.domain.NotificationTemplate;
import com.company.hrms.notification.domain.NotificationTemplateRepository;
import java.time.Instant;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcNotificationTemplateRepository implements NotificationTemplateRepository {

    private final DatabaseClient databaseClient;

    public R2dbcNotificationTemplateRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<NotificationTemplate> findActiveTemplate(String tenantId, String templateCode, NotificationChannel channel) {
        return databaseClient.sql("""
                        SELECT tenant_id, template_code, channel, subject_template, body_template, is_active, created_at, updated_at
                        FROM notification.notification_templates
                        WHERE template_code = :templateCode
                          AND channel = :channel
                          AND is_active = true
                          AND (tenant_id = :tenantId OR tenant_id IS NULL)
                        ORDER BY tenant_id DESC NULLS LAST
                        LIMIT 1
                        """)
                .bind("templateCode", templateCode)
                .bind("channel", channel.name())
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new NotificationTemplate(
                        row.get("tenant_id", String.class),
                        row.get("template_code", String.class),
                        NotificationChannel.valueOf(row.get("channel", String.class)),
                        row.get("subject_template", String.class),
                        row.get("body_template", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }
}
