CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification.notification_templates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64),
    template_code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject_template VARCHAR(255),
    body_template TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification.notifications (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    template_code VARCHAR(100),
    reference_type VARCHAR(80),
    reference_id VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    failure_reason TEXT,
    dispatched_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notifications_tenant_status_created
    ON notification.notifications (tenant_id, status, created_at);

CREATE INDEX IF NOT EXISTS idx_notifications_tenant_reference
    ON notification.notifications (tenant_id, reference_type, reference_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_templates_tenant_code_channel
    ON notification.notification_templates (COALESCE(tenant_id, ''), template_code, channel);
