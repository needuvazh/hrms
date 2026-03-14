package com.company.hrms.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.company.hrms.auth.model.AuthTokenCommandDto;
import com.company.hrms.auth.model.PermissionDto;
import com.company.hrms.auth.model.RoleDto;
import com.company.hrms.auth.model.ScopeDto;
import com.company.hrms.auth.model.UserDto;
import com.company.hrms.auth.model.UserRoleAssignmentDto;
import com.company.hrms.auth.repository.AuthRepository;
import com.company.hrms.contracts.auth.ProvisionUserAccountCommandDto;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.security.api.CurrentUserContext;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.platform.starter.security.api.JwtTokenValue;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthApplicationServiceTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final InMemoryAuthRepository authRepository = new InMemoryAuthRepository(passwordEncoder);
    private final CurrentUserContextAccessor currentUserContextAccessor =
            () -> Mono.just(new CurrentUserContext(
                    UUID.fromString("21111111-1111-1111-1111-111111111111"),
                    "admin",
                    "admin@local",
                    "System",
                    "Administrator",
                    "platform",
                    true,
                    true,
                    Set.of("SUPER_ADMIN"),
                    Set.of("EMPLOYEE_READ", "EMPLOYEE_WRITE"),
                    Set.of("TENANT_ALL", "SYSTEM_ALL")));

    private final JwtTokenService jwtTokenService = claims -> Mono.just(new JwtTokenValue(
            "jwt-token-for-" + claims.username(),
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T01:00:00Z")));
    private final RecordingAuditEventPublisher auditEventPublisher = new RecordingAuditEventPublisher();

    private final AuthApplicationService authApplicationService = new AuthApplicationService(
            authRepository,
            new DefaultTenantContextAccessor(),
            passwordEncoder,
            jwtTokenService,
            currentUserContextAccessor,
            auditEventPublisher);

    @Test
    void issuesTokenWhenCredentialsValid() {
        StepVerifier.create(authApplicationService.issueToken(new AuthTokenCommandDto("admin", "admin")))
                .assertNext(token -> {
                    assertEquals("Bearer", token.tokenType());
                    assertEquals(3600L, token.expiresIn());
                    assertEquals("admin", token.user().username());
                    assertEquals(Set.of("SUPER_ADMIN"), token.user().roles());
                    assertEquals(Set.of("EMPLOYEE_READ", "EMPLOYEE_WRITE"), token.user().permissions());
                    assertEquals(Set.of("TENANT_ALL", "SYSTEM_ALL"), token.user().scopes());
                })
                .verifyComplete();

        assertTrue(auditEventPublisher.actions.contains("AUTH_TOKEN_ISSUED"));
    }

    @Test
    void rejectsInvalidCredentials() {
        StepVerifier.create(authApplicationService.issueToken(new AuthTokenCommandDto("admin", "wrong")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException exception = (HrmsException) error;
                    assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
                })
                .verify();

        assertTrue(auditEventPublisher.actions.contains("AUTHENTICATION_FAILED"));
    }

    @Test
    void returnsCurrentAuthenticatedUser() {
        StepVerifier.create(authApplicationService.currentUser())
                .assertNext(user -> {
                    assertEquals("admin", user.username());
                    assertEquals("platform", user.tenantId());
                    assertTrue(user.superAdmin());
                    assertEquals(Set.of("TENANT_ALL", "SYSTEM_ALL"), user.scopes());
                })
                .verifyComplete();
    }

    @Test
    void provisionsUserAccount() {
        StepVerifier.create(authApplicationService.provisionUserAccount(new ProvisionUserAccountCommandDto(
                                "john",
                                "john@default.hrms",
                                "changeMe123",
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> assertEquals("john", view.username()))
                .verifyComplete();
    }

    static class RecordingAuditEventPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }

    static class InMemoryAuthRepository implements AuthRepository {

        private final UserDto user;
        private final List<RoleDto> roles;
        private final List<PermissionDto> permissions;
        private final List<ScopeDto> scopes;

        InMemoryAuthRepository(PasswordEncoder passwordEncoder) {
            UUID userId = UUID.fromString("21111111-1111-1111-1111-111111111111");
            this.user = new UserDto(
                    userId,
                    "platform",
                    "admin",
                    "admin@local",
                    "System",
                    "Administrator",
                    "ACTIVE",
                    passwordEncoder.encode("admin"),
                    true,
                    true,
                    true);
            this.roles = List.of(new RoleDto(UUID.randomUUID(), "platform", "SUPER_ADMIN", "Super Admin"));
            this.permissions = List.of(
                    new PermissionDto(UUID.randomUUID(), "platform", "EMPLOYEE_READ", "Read Employee"),
                    new PermissionDto(UUID.randomUUID(), "platform", "EMPLOYEE_WRITE", "Write Employee"));
            this.scopes = List.of(
                    new ScopeDto("TENANT_ALL", null),
                    new ScopeDto("SYSTEM_ALL", null));
        }

        @Override
        public Mono<UserDto> findActiveUserByUsername(String username) {
            if ("admin".equals(username)) {
                return Mono.just(user);
            }
            return Mono.empty();
        }

        @Override
        public Flux<RoleDto> rolesForUser(UUID userId, String tenantId) {
            return Flux.fromIterable(roles);
        }

        @Override
        public Flux<PermissionDto> permissionsForUser(UUID userId, String tenantId) {
            return Flux.fromIterable(permissions);
        }

        @Override
        public Flux<ScopeDto> scopesForUser(UUID userId) {
            return Flux.fromIterable(scopes);
        }

        @Override
        public Flux<UserRoleAssignmentDto> roleAssignmentsForUser(UUID userId, String tenantId) {
            return Flux.empty();
        }

        @Override
        public Mono<UserDto> saveUser(UserDto user) {
            return Mono.just(user);
        }

        @Override
        public Mono<RoleDto> findRoleByCode(String roleCode, String tenantId) {
            return Flux.fromIterable(roles)
                    .filter(role -> roleCode.equals(role.code()))
                    .next();
        }

        @Override
        public Mono<Void> assignRoleToUser(UUID userId, UUID roleId, String tenantId) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> updateLastLoginAt(UUID userId) {
            return Mono.empty();
        }
    }
}
