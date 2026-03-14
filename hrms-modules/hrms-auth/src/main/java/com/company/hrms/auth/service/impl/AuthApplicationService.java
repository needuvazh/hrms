package com.company.hrms.auth.service.impl;

import com.company.hrms.auth.model.AuthTokenCommandDto;
import com.company.hrms.auth.model.AuthTokenViewDto;
import com.company.hrms.auth.model.AuthenticatedUserViewDto;
import com.company.hrms.auth.model.CurrentUserViewDto;
import com.company.hrms.auth.model.PermissionDto;
import com.company.hrms.auth.model.ProvisionedUserAccountViewDto;
import com.company.hrms.auth.model.RoleDto;
import com.company.hrms.auth.model.RoleViewDto;
import com.company.hrms.auth.model.ScopeDto;
import com.company.hrms.auth.model.UserDto;
import com.company.hrms.auth.repository.AuthRepository;
import com.company.hrms.auth.service.AuthModuleApi;
import com.company.hrms.contracts.auth.ProvisionUserAccountCommandDto;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.api.JwtTokenClaims;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    public Mono<AuthTokenViewDto> issueToken(AuthTokenCommandDto command) {
        return authRepository.findActiveUserByUsername(command.username())
                .switchIfEmpty(onFailedAuthentication("platform", command.username(), "USER_NOT_FOUND")
                        .then(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials"))))
                .flatMap(user -> validateCredentials(user, command.password())
                        .then(authRepository.updateLastLoginAt(user.id()))
                        .then(buildTokenView(user))
                        .flatMap(token -> auditEventPublisher.publish(AuditEvent.of(
                                        user.username(),
                                        user.tenantId(),
                                        "AUTH_TOKEN_ISSUED",
                                        "USER",
                                        user.id().toString(),
                                        Map.of("username", user.username())))
                                .thenReturn(token)));
    }

    @Override
    public Mono<CurrentUserViewDto> currentUser() {
        return currentUserContextAccessor.currentUser()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required")))
                .map(user -> new CurrentUserViewDto(
                        user.userId(),
                        user.username(),
                        user.firstName(),
                        user.lastName(),
                        user.email(),
                        user.superAdmin(),
                        user.canViewAllTenants(),
                        user.tenantId(),
                        user.roles(),
                        user.permissions(),
                        user.scopes()));
    }

    @Override
    public Flux<RoleViewDto> getRolesForCurrentUser() {
        return currentUserContextAccessor.currentUser()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required")))
                .flatMapMany(user -> authRepository.rolesForUser(user.userId(), user.tenantId())
                        .map(role -> new RoleViewDto(role.code(), role.name())));
    }

    @Override
    public Mono<ProvisionedUserAccountViewDto> provisionUserAccount(ProvisionUserAccountCommandDto command) {
        if (command == null || !StringUtils.hasText(command.username())
                || !StringUtils.hasText(command.email())
                || !StringUtils.hasText(command.rawPassword())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_USER_PROVISION_REQUEST", "Username, email and password are required"));
        }

        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")))
                .flatMap(tenantId -> {
                    UserDto newUser = new UserDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.username().trim(),
                            command.email().trim(),
                            command.username().trim(),
                            "",
                            "ACTIVE",
                            passwordEncoder.encode(command.rawPassword()),
                            true,
                            false,
                            false);

                    Mono<UserDto> saveUserMono = authRepository.saveUser(newUser);
                    Mono<String> roleMono;
                    if (StringUtils.hasText(command.roleCode())) {
                        roleMono = authRepository.findRoleByCode(command.roleCode().trim(), tenantId)
                                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND", "RoleDto not found")))
                                .flatMap(role -> authRepository.assignRoleToUser(newUser.id(), role.id(), tenantId)
                                        .thenReturn(role.code()));
                    } else {
                        roleMono = Mono.just("");
                    }

                    return saveUserMono.zipWith(roleMono)
                            .flatMap(tuple -> {
                                UserDto user = tuple.getT1();
                                String roleCode = tuple.getT2();
                                return auditEventPublisher.publish(AuditEvent.of(
                                                command.username().trim(),
                                                tenantId,
                                                "AUTH_USER_PROVISIONED",
                                                "USER",
                                                user.id().toString(),
                                                Map.of("username", user.username())))
                                        .thenReturn(new ProvisionedUserAccountViewDto(
                                                user.id(),
                                                user.tenantId(),
                                                user.username(),
                                                user.email(),
                                                roleCode));
                            });
                });
    }

    private Mono<Void> validateCredentials(UserDto user, String rawPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, user.passwordHash());
        if (!matches) {
            return onFailedAuthentication(user.tenantId(), user.username(), "PASSWORD_MISMATCH")
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

    private Mono<AuthTokenViewDto> buildTokenView(UserDto user) {
        Mono<Set<String>> rolesMono = authRepository.rolesForUser(user.id(), user.tenantId())
                .map(RoleDto::code)
                .collect(Collectors.toSet());

        Mono<Set<String>> permissionsMono = authRepository.permissionsForUser(user.id(), user.tenantId())
                .map(PermissionDto::code)
                .collect(Collectors.toSet());

        Mono<Set<String>> scopesMono = authRepository.scopesForUser(user.id())
                .map(this::scopeEntry)
                .collect(Collectors.toSet());

        return Mono.zip(rolesMono, permissionsMono, scopesMono)
                .flatMap(tuple -> jwtTokenService.issueToken(new JwtTokenClaims(
                                user.id(),
                                user.username(),
                                user.email(),
                                user.firstName(),
                                user.lastName(),
                                user.tenantId(),
                                user.superAdmin(),
                                user.canViewAllTenants(),
                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3()))
                        .map(token -> new AuthTokenViewDto(
                                token.tokenValue(),
                                "Bearer",
                                Duration.between(token.issuedAt(), token.expiresAt()).toSeconds(),
                                token.issuedAt(),
                                token.expiresAt(),
                                new AuthenticatedUserViewDto(
                                        user.id(),
                                        user.username(),
                                        user.firstName(),
                                        user.lastName(),
                                        user.email(),
                                        user.superAdmin(),
                                        user.canViewAllTenants(),
                                        tuple.getT1(),
                                        tuple.getT2(),
                                        tuple.getT3()))));
    }

    private String scopeEntry(ScopeDto scopeDto) {
        if (StringUtils.hasText(scopeDto.value())) {
            return scopeDto.code() + ":" + scopeDto.value();
        }
        return scopeDto.code();
    }
}
