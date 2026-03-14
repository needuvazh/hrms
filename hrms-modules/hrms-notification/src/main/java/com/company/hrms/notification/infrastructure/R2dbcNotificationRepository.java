package com.company.hrms.notification.infrastructure;

import com.company.hrms.notification.domain.Notification;
import com.company.hrms.notification.domain.NotificationChannel;
import com.company.hrms.notification.domain.NotificationRepository;
import com.company.hrms.notification.domain.NotificationStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcNotificationRepository implements NotificationRepository {

    private final DatabaseClient databaseClient;

    public R2dbcNotificationRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Notification> save(Notification notification) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO notification.notifications(
                            id,
                            tenant_id,
                            channel,
                            recipient,
                            subject,
                            body,
                            template_code,
                            reference_type,
                            reference_id,
                            status,
                            failure_reason,
                            dispatched_at,
                            created_at,
                            updated_at
                        ) VALUES (
                            :id,
                            :tenantId,
                            :channel,
                            :recipient,
                            :subject,
                            :body,
                            :templateCode,
                            :referenceType,
                            :referenceId,
                            :status,
                            :failureReason,
                            :dispatchedAt,
                            :createdAt,
                            :updatedAt
                        )
                        RETURNING id, tenant_id, channel, recipient, subject, body, template_code, reference_type, reference_id,
                                  status, failure_reason, dispatched_at, created_at, updated_at
                        """)
                .bind("id", notification.id())
                .bind("tenantId", notification.tenantId())
                .bind("channel", notification.channel().name())
                .bind("recipient", notification.recipient())
                .bind("body", notification.body())
                .bind("status", notification.status().name())
                .bind("createdAt", notification.createdAt())
                .bind("updatedAt", notification.updatedAt());

        spec = bindNullable(spec, "subject", notification.subject(), String.class);
        spec = bindNullable(spec, "templateCode", notification.templateCode(), String.class);
        spec = bindNullable(spec, "referenceType", notification.referenceType(), String.class);
        spec = bindNullable(spec, "referenceId", notification.referenceId(), String.class);
        spec = bindNullable(spec, "failureReason", notification.failureReason(), String.class);
        spec = bindNullable(spec, "dispatchedAt", notification.dispatchedAt(), Instant.class);

        return spec.map((row, metadata) -> mapNotification(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("channel", String.class),
                        row.get("recipient", String.class),
                        row.get("subject", String.class),
                        row.get("body", String.class),
                        row.get("template_code", String.class),
                        row.get("reference_type", String.class),
                        row.get("reference_id", String.class),
                        row.get("status", String.class),
                        row.get("failure_reason", String.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Notification> update(Notification notification) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE notification.notifications
                        SET status = :status,
                            failure_reason = :failureReason,
                            dispatched_at = :dispatchedAt,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, channel, recipient, subject, body, template_code, reference_type, reference_id,
                                  status, failure_reason, dispatched_at, created_at, updated_at
                        """)
                .bind("id", notification.id())
                .bind("tenantId", notification.tenantId())
                .bind("status", notification.status().name())
                .bind("updatedAt", notification.updatedAt());

        spec = bindNullable(spec, "failureReason", notification.failureReason(), String.class);
        spec = bindNullable(spec, "dispatchedAt", notification.dispatchedAt(), Instant.class);

        return spec.map((row, metadata) -> mapNotification(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("channel", String.class),
                        row.get("recipient", String.class),
                        row.get("subject", String.class),
                        row.get("body", String.class),
                        row.get("template_code", String.class),
                        row.get("reference_type", String.class),
                        row.get("reference_id", String.class),
                        row.get("status", String.class),
                        row.get("failure_reason", String.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<Notification> findById(UUID notificationId, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, channel, recipient, subject, body, template_code, reference_type, reference_id,
                               status, failure_reason, dispatched_at, created_at, updated_at
                        FROM notification.notifications
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        """)
                .bind("id", notificationId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapNotification(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("channel", String.class),
                        row.get("recipient", String.class),
                        row.get("subject", String.class),
                        row.get("body", String.class),
                        row.get("template_code", String.class),
                        row.get("reference_type", String.class),
                        row.get("reference_id", String.class),
                        row.get("status", String.class),
                        row.get("failure_reason", String.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<Notification> findByStatus(String tenantId, NotificationStatus status, int limit) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, channel, recipient, subject, body, template_code, reference_type, reference_id,
                               status, failure_reason, dispatched_at, created_at, updated_at
                        FROM notification.notifications
                        WHERE tenant_id = :tenantId
                          AND status = :status
                        ORDER BY created_at ASC
                        LIMIT :limit
                        """)
                .bind("tenantId", tenantId)
                .bind("status", status.name())
                .bind("limit", limit)
                .map((row, metadata) -> mapNotification(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("channel", String.class),
                        row.get("recipient", String.class),
                        row.get("subject", String.class),
                        row.get("body", String.class),
                        row.get("template_code", String.class),
                        row.get("reference_type", String.class),
                        row.get("reference_id", String.class),
                        row.get("status", String.class),
                        row.get("failure_reason", String.class),
                        row.get("dispatched_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private Notification mapNotification(
            UUID id,
            String tenantId,
            String channel,
            String recipient,
            String subject,
            String body,
            String templateCode,
            String referenceType,
            String referenceId,
            String status,
            String failureReason,
            Instant dispatchedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Notification(
                id,
                tenantId,
                NotificationChannel.valueOf(channel),
                recipient,
                subject,
                body,
                templateCode,
                referenceType,
                referenceId,
                NotificationStatus.valueOf(status),
                failureReason,
                dispatchedAt,
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
