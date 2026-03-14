package com.company.hrms.platform.starter.security.context;

import com.company.hrms.platform.starter.security.api.CurrentUserContext;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public class ReactiveCurrentUserContextAccessor implements CurrentUserContextAccessor {

    @Override
    public Mono<CurrentUserContext> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(this::mapCurrentUser);
    }

    private CurrentUserContext mapCurrentUser(Jwt jwt) {
        Set<String> roles = toStringSet(jwt.getClaimAsStringList("roles"));
        Set<String> permissions = toStringSet(jwt.getClaimAsStringList("permissions"));
        Set<String> scopes = toStringSet(jwt.getClaimAsStringList("scopes"));
        String userId = jwt.getClaimAsString("uid");
        return new CurrentUserContext(
                userId == null ? null : UUID.fromString(userId),
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("first_name"),
                jwt.getClaimAsString("last_name"),
                jwt.getClaimAsString("tenant"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("super_admin")),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("can_view_all_tenants")),
                roles,
                permissions,
                scopes);
    }

    private Set<String> toStringSet(java.util.List<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().collect(Collectors.toSet());
    }
}
