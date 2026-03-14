INSERT INTO tenant.countries (country_code, country_name, currency_code, timezone, locale, is_active)
VALUES
    ('OM', 'Oman', 'OMR', 'Asia/Muscat', 'en-OM', TRUE),
    ('AE', 'United Arab Emirates', 'AED', 'Asia/Dubai', 'en-AE', TRUE),
    ('SA', 'Saudi Arabia', 'SAR', 'Asia/Riyadh', 'en-SA', TRUE)
ON CONFLICT (country_code) DO NOTHING;

INSERT INTO tenant.tenant_country_config (
    tenant_code,
    country_code,
    is_primary,
    compliance_profile,
    effective_from,
    is_active
)
VALUES
    ('default', 'OM', TRUE, 'OMAN_STANDARD', DATE '2025-01-01', TRUE),
    ('default', 'AE', FALSE, 'UAE_STANDARD', DATE '2025-01-01', TRUE),
    ('lite', 'OM', TRUE, 'OMAN_LITE', DATE '2025-01-01', TRUE)
ON CONFLICT (tenant_code, country_code) DO NOTHING;
