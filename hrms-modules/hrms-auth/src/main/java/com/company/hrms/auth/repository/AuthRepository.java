package com.company.hrms.auth.repository;

import com.company.hrms.auth.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthRepository {

    Mono<UserDto> findActiveUserByUsername(String username);

    Flux<RoleDto> rolesForUser(UUID userId, String tenantId);

    Flux<PermissionDto> permissionsForUser(UUID userId, String tenantId);

    Flux<ScopeDto> scopesForUser(UUID userId);

    Flux<UserRoleAssignmentDto> roleAssignmentsForUser(UUID userId, String tenantId);

    Mono<UserDto> saveUser(UserDto user);

    Mono<RoleDto> findRoleByCode(String roleCode, String tenantId);

    Mono<Void> assignRoleToUser(UUID userId, UUID roleId, String tenantId);

    Mono<Void> updateLastLoginAt(UUID userId);
}
