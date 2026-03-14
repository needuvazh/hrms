CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    username VARCHAR(128) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_user_tenant_username UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS auth.roles (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_role_tenant_code UNIQUE (tenant_id, role_code)
);

CREATE TABLE IF NOT EXISTS auth.permissions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_auth_permission_tenant_code UNIQUE (tenant_id, permission_code)
);

CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id, tenant_id),
    CONSTRAINT fk_auth_user_roles_user FOREIGN KEY (user_id) REFERENCES auth.users(id),
    CONSTRAINT fk_auth_user_roles_role FOREIGN KEY (role_id) REFERENCES auth.roles(id)
);

CREATE TABLE IF NOT EXISTS auth.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id, tenant_id),
    CONSTRAINT fk_auth_role_permissions_role FOREIGN KEY (role_id) REFERENCES auth.roles(id),
    CONSTRAINT fk_auth_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES auth.permissions(id)
);
