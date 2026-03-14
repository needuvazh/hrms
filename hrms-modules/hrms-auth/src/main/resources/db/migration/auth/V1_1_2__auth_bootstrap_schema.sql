ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(128),
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(128),
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS is_super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS can_view_all_tenants BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM';

ALTER TABLE auth.roles
    ADD COLUMN IF NOT EXISTS description VARCHAR(512),
    ADD COLUMN IF NOT EXISTS is_system_role BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE auth.permissions
    ADD COLUMN IF NOT EXISTS module_code VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS description VARCHAR(512);

CREATE TABLE IF NOT EXISTS auth.scopes (
    id UUID PRIMARY KEY,
    scope_code VARCHAR(128) NOT NULL,
    scope_name VARCHAR(255) NOT NULL,
    description VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_scope_code UNIQUE (scope_code)
);

CREATE TABLE IF NOT EXISTS auth.user_scopes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    scope_id UUID NOT NULL,
    scope_value VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_user_scopes_user FOREIGN KEY (user_id) REFERENCES auth.users(id),
    CONSTRAINT fk_auth_user_scopes_scope FOREIGN KEY (scope_id) REFERENCES auth.scopes(id),
    CONSTRAINT uq_auth_user_scope UNIQUE (user_id, scope_id, scope_value)
);

CREATE INDEX IF NOT EXISTS idx_auth_users_username ON auth.users (username);
CREATE INDEX IF NOT EXISTS idx_auth_roles_role_code ON auth.roles (role_code);
CREATE INDEX IF NOT EXISTS idx_auth_permissions_permission_code ON auth.permissions (permission_code);
CREATE INDEX IF NOT EXISTS idx_auth_scopes_scope_code ON auth.scopes (scope_code);

UPDATE auth.users
SET first_name = COALESCE(first_name, 'System'),
    last_name = COALESCE(last_name, 'Administrator'),
    status = 'ACTIVE',
    updated_by = COALESCE(updated_by, 'SYSTEM')
WHERE username = 'admin';

UPDATE auth.roles
SET is_system_role = TRUE,
    description = COALESCE(description, 'System role')
WHERE role_code IN ('HR_ADMIN', 'SUPER_ADMIN');

UPDATE auth.permissions
SET module_code = CASE
        WHEN permission_code LIKE '%_%' THEN split_part(permission_code, '_', 1)
        ELSE UPPER(COALESCE(module_code, 'SYSTEM'))
    END,
    description = COALESCE(description, permission_name);
