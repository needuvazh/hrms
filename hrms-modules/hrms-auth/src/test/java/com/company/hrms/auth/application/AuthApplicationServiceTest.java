package com.company.hrms.auth.application;

import com.company.hrms.auth.api.AuthTokenCommand;
import com.company.hrms.auth.api.ProvisionUserAccountCommand;
import com.company.hrms.auth.domain.AuthRepository;
import com.company.hrms.auth.domain.Permission;
import com.company.hrms.auth.domain.Role;
import com.company.hrms.auth.domain.User;
import com.company.hrms.auth.domain.UserRoleAssignment;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.security.api.CurrentUserContext;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.api.JwtTokenClaims;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthApplicationServiceTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final InMemoryAuthRepository authRepository = new InMemoryAuthRepository(passwordEncoder);
    private final CurrentUserContextAccessor currentUserContextAccessor =
            () -> Mono.just(new CurrentUserContext(
                    UUID.fromString("21111111-1111-1111-1111-111111111111"),
                    "admin",
                    "default",
                    Set.of("HR_ADMIN"),
                    Set.of("EMPLOYEE_READ", "EMPLOYEE_WRITE")));

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
        StepVerifier.create(authApplicationService.issueToken(new AuthTokenCommand("admin", "admin123"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(token -> {
                    assertEquals("Bearer", token.tokenType());
                    assertEquals("default", token.tenantId());
                    assertEquals("admin", token.username());
                    assertEquals(Set.of("HR_ADMIN"), token.roles());
                    assertEquals(Set.of("EMPLOYEE_READ", "EMPLOYEE_WRITE"), token.permissions());
                })
                .verifyComplete();

        assertTrue(auditEventPublisher.actions.contains("AUTH_TOKEN_ISSUED"));
    }

    @Test
    void rejectsInvalidCredentials() {
        StepVerifier.create(authApplicationService.issueToken(new AuthTokenCommand("admin", "wrong"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException exception = (HrmsException) error;
                    assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
                })
                .verify();

        assertTrue(auditEventPublisher.actions.contains("AUTHENTICATION_FAILED"));
    }

    static class RecordingAuditEventPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }

    @Test
    void returnsCurrentAuthenticatedUser() {
        StepVerifier.create(authApplicationService.currentUser())
                .assertNext(user -> {
                    assertEquals("admin", user.username());
                    assertEquals("default", user.tenantId());
                })
                .verifyComplete();
    }

    @Test
    void provisionsUserAccount() {
        StepVerifier.create(authApplicationService.provisionUserAccount(new ProvisionUserAccountCommand(
                                "john",
                                "john@default.hrms",
                                "changeMe123",
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> assertEquals("john", view.username()))
                .verifyComplete();
    }

    static class InMemoryAuthRepository implements AuthRepository {

        private final User user;
        private final List<Role> roles;
        private final List<Permission> permissions;

        InMemoryAuthRepository(PasswordEncoder passwordEncoder) {
            UUID userId = UUID.fromString("21111111-1111-1111-1111-111111111111");
            this.user = new User(
                    userId,
                    "default",
                    "admin",
                    "admin@default.hrms",
                    passwordEncoder.encode("admin123"),
                    true);
            this.roles = List.of(new Role(UUID.randomUUID(), "default", "HR_ADMIN", "HR Admin"));
            this.permissions = List.of(
                    new Permission(UUID.randomUUID(), "default", "EMPLOYEE_READ", "Read Employee"),
                    new Permission(UUID.randomUUID(), "default", "EMPLOYEE_WRITE", "Write Employee"));
        }

        @Override
        public Mono<User> findActiveUserByUsername(String username, String tenantId) {
            if ("admin".equals(username) && "default".equals(tenantId)) {
                return Mono.just(user);
            }
            return Mono.empty();
        }

        @Override
        public Flux<Role> rolesForUser(UUID userId, String tenantId) {
            return Flux.fromIterable(roles);
        }

        @Override
        public Flux<Permission> permissionsForUser(UUID userId, String tenantId) {
            return Flux.fromIterable(permissions);
        }

        @Override
        public Flux<UserRoleAssignment> roleAssignmentsForUser(UUID userId, String tenantId) {
            return Flux.empty();
        }

        @Override
        public Mono<User> saveUser(User user) {
            return Mono.just(user);
        }

        @Override
        public Mono<Role> findRoleByCode(String roleCode, String tenantId) {
            return Flux.fromIterable(roles)
                    .filter(role -> roleCode.equals(role.code()))
                    .next();
        }

        @Override
        public Mono<Void> assignRoleToUser(UUID userId, UUID roleId, String tenantId) {
            return Mono.empty();
        }
    }
}
