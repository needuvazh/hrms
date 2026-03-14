package com.company.hrms.auth.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthRepository {

    Mono<User> findActiveUserByUsername(String username, String tenantId);

    Flux<Role> rolesForUser(UUID userId, String tenantId);

    Flux<Permission> permissionsForUser(UUID userId, String tenantId);

    Flux<UserRoleAssignment> roleAssignmentsForUser(UUID userId, String tenantId);

    Mono<User> saveUser(User user);

    Mono<Role> findRoleByCode(String roleCode, String tenantId);

    Mono<Void> assignRoleToUser(UUID userId, UUID roleId, String tenantId);
}
