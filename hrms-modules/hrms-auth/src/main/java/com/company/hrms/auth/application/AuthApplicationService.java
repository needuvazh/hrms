package com.company.hrms.auth.application;

import com.company.hrms.auth.api.AuthModuleApi;
import com.company.hrms.auth.api.AuthTokenCommand;
import com.company.hrms.auth.api.AuthTokenView;
import com.company.hrms.auth.api.CurrentUserView;
import com.company.hrms.auth.api.ProvisionUserAccountCommand;
import com.company.hrms.auth.api.ProvisionedUserAccountView;
import com.company.hrms.auth.api.RoleView;
import com.company.hrms.auth.domain.AuthRepository;
import com.company.hrms.auth.domain.Permission;
import com.company.hrms.auth.domain.Role;
import com.company.hrms.auth.domain.User;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.api.JwtTokenClaims;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AuthApplicationService implements AuthModuleApi {

    private final AuthRepository authRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUserContextAccessor currentUserContextAccessor;
    private final AuditEventPublisher auditEventPublisher;

    public AuthApplicationService(
            AuthRepository authRepository,
            TenantContextAccessor tenantContextAccessor,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            CurrentUserContextAccessor currentUserContextAccessor,
            AuditEventPublisher auditEventPublisher
    ) {
        this.authRepository = authRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.currentUserContextAccessor = currentUserContextAccessor;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<AuthTokenView> issueToken(AuthTokenCommand command) {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                .flatMap(tenantId -> authRepository.findActiveUserByUsername(command.username(), tenantId)
                        .switchIfEmpty(onFailedAuthentication(tenantId, command.username(), "USER_NOT_FOUND")
                                .then(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials"))))
                        .flatMap(user -> validateCredentials(user, command.password(), tenantId)
                                .then(buildTokenView(user))
                                .flatMap(token -> auditEventPublisher.publish(AuditEvent.of(
                                                user.username(),
                                                tenantId,
                                                "AUTH_TOKEN_ISSUED",
                                                "USER",
                                                user.id().toString(),
                                                Map.of("username", user.username())))
                                        .thenReturn(token))));
    }

    @Override
    public Mono<CurrentUserView> currentUser() {
        return currentUserContextAccessor.currentUser()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required")))
                .map(user -> new CurrentUserView(user.userId(), user.username(), user.tenantId(), user.roles(), user.permissions()));
    }

    @Override
    public Flux<RoleView> getRolesForCurrentUser() {
        return currentUserContextAccessor.currentUser()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required")))
                .flatMapMany(user -> authRepository.rolesForUser(user.userId(), user.tenantId())
                        .map(role -> new RoleView(role.code(), role.name())));
    }

    @Override
    public Mono<ProvisionedUserAccountView> provisionUserAccount(ProvisionUserAccountCommand command) {
        if (command == null || !org.springframework.util.StringUtils.hasText(command.username())
                || !org.springframework.util.StringUtils.hasText(command.email())
                || !org.springframework.util.StringUtils.hasText(command.rawPassword())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_USER_PROVISION_REQUEST", "Username, email and password are required"));
        }

        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                .flatMap(tenantId -> {
                    User newUser = new User(
                            UUID.randomUUID(),
                            tenantId,
                            command.username().trim(),
                            command.email().trim(),
                            passwordEncoder.encode(command.rawPassword()),
                            true);

                    Mono<User> saveUserMono = authRepository.saveUser(newUser);
                    Mono<String> roleMono;
                    if (org.springframework.util.StringUtils.hasText(command.roleCode())) {
                        roleMono = authRepository.findRoleByCode(command.roleCode().trim(), tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND", "Role not found")))
                                .flatMap(role -> authRepository.assignRoleToUser(newUser.id(), role.id(), tenantId)
                                        .thenReturn(role.code()));
                    } else {
                        roleMono = Mono.just("");
                    }

                    return saveUserMono.zipWith(roleMono)
                            .flatMap(tuple -> {
                                User user = tuple.getT1();
                                String roleCode = tuple.getT2();
                                return auditEventPublisher.publish(AuditEvent.of(
                                                command.username().trim(),
                                                tenantId,
                                                "AUTH_USER_PROVISIONED",
                                                "USER",
                                                user.id().toString(),
                                                Map.of("username", user.username())))
                                        .thenReturn(new ProvisionedUserAccountView(
                                                user.id(),
                                                user.tenantId(),
                                                user.username(),
                                                user.email(),
                                                roleCode));
                            });
                });
    }

    private Mono<Void> validateCredentials(User user, String rawPassword, String tenantId) {
        boolean matches = passwordEncoder.matches(rawPassword, user.passwordHash());
        if (!matches) {
            return onFailedAuthentication(tenantId, user.username(), "PASSWORD_MISMATCH")
                    .then(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials")));
        }
        return Mono.empty();
    }

    private Mono<Void> onFailedAuthentication(String tenantId, String username, String reason) {
        return auditEventPublisher.publish(AuditEvent.of(
                username,
                tenantId,
                "AUTHENTICATION_FAILED",
                "USER",
                username,
                Map.of("reason", reason)));
    }

    private Mono<AuthTokenView> buildTokenView(User user) {
        Mono<Set<String>> rolesMono = authRepository.rolesForUser(user.id(), user.tenantId())
                .map(Role::code)
                .collect(Collectors.toSet());

        Mono<Set<String>> permissionsMono = authRepository.permissionsForUser(user.id(), user.tenantId())
                .map(Permission::code)
                .collect(Collectors.toSet());

        return Mono.zip(rolesMono, permissionsMono)
                .flatMap(tuple -> jwtTokenService.issueToken(new JwtTokenClaims(
                                user.id(),
                                user.username(),
                                user.tenantId(),
                                tuple.getT1(),
                                tuple.getT2()))
                        .map(token -> new AuthTokenView(
                                token.tokenValue(),
                                "Bearer",
                                token.issuedAt(),
                                token.expiresAt(),
                                user.id(),
                                user.username(),
                                user.tenantId(),
                                tuple.getT1(),
                                tuple.getT2())));
    }
}
