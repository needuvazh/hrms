INSERT INTO master_data.tenant_branding (
    tenant_code,
    brand_name,
    logo_url,
    favicon_url,
    primary_color,
    secondary_color,
    login_banner_url,
    email_logo_url,
    active,
    created_by,
    updated_by
)
VALUES
    (
        'default',
        'Default Tenant',
        'https://cdn.hrms.local/branding/default/logo.svg',
        'https://cdn.hrms.local/branding/default/favicon.ico',
        '#0B5FFF',
        '#111827',
        'https://cdn.hrms.local/branding/default/login-banner.jpg',
        'https://cdn.hrms.local/branding/default/email-logo.png',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'lite',
        'Lite Tenant',
        'https://cdn.hrms.local/branding/lite/logo.svg',
        'https://cdn.hrms.local/branding/lite/favicon.ico',
        '#16A34A',
        '#0F172A',
        'https://cdn.hrms.local/branding/lite/login-banner.jpg',
        'https://cdn.hrms.local/branding/lite/email-logo.png',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_code) DO UPDATE
SET brand_name = EXCLUDED.brand_name,
    logo_url = EXCLUDED.logo_url,
    favicon_url = EXCLUDED.favicon_url,
    primary_color = EXCLUDED.primary_color,
    secondary_color = EXCLUDED.secondary_color,
    login_banner_url = EXCLUDED.login_banner_url,
    email_logo_url = EXCLUDED.email_logo_url,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
