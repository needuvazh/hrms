package com.company.hrms.auth.repository;

import com.company.hrms.auth.model.*;

import com.company.hrms.auth.repository.AuthRepository;
import com.company.hrms.auth.model.PermissionDto;
import com.company.hrms.auth.model.RoleDto;
import com.company.hrms.auth.model.UserDto;
import com.company.hrms.auth.model.UserRoleAssignmentDto;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class AuthR2dbcRepository implements AuthRepository {

    private final DatabaseClient databaseClient;

    public AuthR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<UserDto> findActiveUserByUsername(String username, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, username, email, password_hash, is_active
                        FROM auth.users
                        WHERE username = :username
                          AND tenant_id = :tenantId
                          AND is_active = TRUE
                        """)
                .bind("username", username)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new UserDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("username", String.class),
                        row.get("email", String.class),
                        row.get("password_hash", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .one();
    }

    @Override
    public Flux<RoleDto> rolesForUser(UUID userId, String tenantId) {
        return databaseClient.sql("""
                        SELECT r.id, r.tenant_id, r.role_code, r.role_name
                        FROM auth.user_roles ur
                        JOIN auth.roles r ON r.id = ur.role_id
                        WHERE ur.user_id = :userId
                          AND ur.tenant_id = :tenantId
                        """)
                .bind("userId", userId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new RoleDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("role_code", String.class),
                        row.get("role_name", String.class)))
                .all();
    }

    @Override
    public Flux<PermissionDto> permissionsForUser(UUID userId, String tenantId) {
        return databaseClient.sql("""
                        SELECT p.id, p.tenant_id, p.permission_code, p.permission_name
                        FROM auth.user_roles ur
                        JOIN auth.role_permissions rp ON rp.role_id = ur.role_id AND rp.tenant_id = ur.tenant_id
                        JOIN auth.permissions p ON p.id = rp.permission_id
                        WHERE ur.user_id = :userId
                          AND ur.tenant_id = :tenantId
                        """)
                .bind("userId", userId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new PermissionDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("permission_code", String.class),
                        row.get("permission_name", String.class)))
                .all();
    }

    @Override
    public Flux<UserRoleAssignmentDto> roleAssignmentsForUser(UUID userId, String tenantId) {
        return databaseClient.sql("""
                        SELECT user_id, role_id, tenant_id
                        FROM auth.user_roles
                        WHERE user_id = :userId
                          AND tenant_id = :tenantId
                        """)
                .bind("userId", userId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new UserRoleAssignmentDto(
                        row.get("user_id", UUID.class),
                        row.get("role_id", UUID.class),
                        row.get("tenant_id", String.class)))
                .all();
    }

    @Override
    public Mono<UserDto> saveUser(UserDto user) {
        return databaseClient.sql("""
                        INSERT INTO auth.users(
                            id, tenant_id, username, email, password_hash, is_active, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :username, :email, :passwordHash, :isActive, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                        )
                        RETURNING id, tenant_id, username, email, password_hash, is_active
                        """)
                .bind("id", user.id())
                .bind("tenantId", user.tenantId())
                .bind("username", user.username())
                .bind("email", user.email())
                .bind("passwordHash", user.passwordHash())
                .bind("isActive", user.active())
                .map((row, metadata) -> new UserDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("username", String.class),
                        row.get("email", String.class),
                        row.get("password_hash", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .one();
    }

    @Override
    public Mono<RoleDto> findRoleByCode(String roleCode, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, role_code, role_name
                        FROM auth.roles
                        WHERE role_code = :roleCode
                          AND tenant_id = :tenantId
                        """)
                .bind("roleCode", roleCode)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new RoleDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("role_code", String.class),
                        row.get("role_name", String.class)))
                .one();
    }

    @Override
    public Mono<Void> assignRoleToUser(UUID userId, UUID roleId, String tenantId) {
        return databaseClient.sql("""
                        INSERT INTO auth.user_roles(user_id, role_id, tenant_id, created_at)
                        VALUES (:userId, :roleId, :tenantId, CURRENT_TIMESTAMP)
                        ON CONFLICT (user_id, role_id, tenant_id) DO NOTHING
                        """)
                .bind("userId", userId)
                .bind("roleId", roleId)
                .bind("tenantId", tenantId)
                .then();
    }
}
