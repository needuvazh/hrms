package com.company.hrms.auth.service.impl;

import com.company.hrms.auth.model.*;
import com.company.hrms.auth.repository.*;
import com.company.hrms.auth.service.*;

import com.company.hrms.auth.service.AuthModuleApi;
import com.company.hrms.auth.model.AuthTokenCommandDto;
import com.company.hrms.auth.model.AuthTokenViewDto;
import com.company.hrms.auth.model.CurrentUserViewDto;
import com.company.hrms.contracts.auth.ProvisionUserAccountCommandDto;
import com.company.hrms.auth.model.ProvisionedUserAccountViewDto;
import com.company.hrms.auth.model.RoleViewDto;
import com.company.hrms.auth.repository.AuthRepository;
import com.company.hrms.auth.model.PermissionDto;
import com.company.hrms.auth.model.RoleDto;
import com.company.hrms.auth.model.UserDto;
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
    public Mono<AuthTokenViewDto> issueToken(AuthTokenCommandDto command) {
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
    public Mono<CurrentUserViewDto> currentUser() {
        return currentUserContextAccessor.currentUser()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentication is required")))
                .map(user -> new CurrentUserViewDto(user.userId(), user.username(), user.tenantId(), user.roles(), user.permissions()));
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
        if (command == null || !org.springframework.util.StringUtils.hasText(command.username())
                || !org.springframework.util.StringUtils.hasText(command.email())
                || !org.springframework.util.StringUtils.hasText(command.rawPassword())) {
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
                            passwordEncoder.encode(command.rawPassword()),
                            true);

                    Mono<UserDto> saveUserMono = authRepository.saveUser(newUser);
                    Mono<String> roleMono;
                    if (org.springframework.util.StringUtils.hasText(command.roleCode())) {
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

    private Mono<Void> validateCredentials(UserDto user, String rawPassword, String tenantId) {
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

    private Mono<AuthTokenViewDto> buildTokenView(UserDto user) {
        Mono<Set<String>> rolesMono = authRepository.rolesForUser(user.id(), user.tenantId())
                .map(RoleDto::code)
                .collect(Collectors.toSet());

        Mono<Set<String>> permissionsMono = authRepository.permissionsForUser(user.id(), user.tenantId())
                .map(PermissionDto::code)
                .collect(Collectors.toSet());

        return Mono.zip(rolesMono, permissionsMono)
                .flatMap(tuple -> jwtTokenService.issueToken(new JwtTokenClaims(
                                user.id(),
                                user.username(),
                                user.tenantId(),
                                tuple.getT1(),
                                tuple.getT2()))
                        .map(token -> new AuthTokenViewDto(
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
