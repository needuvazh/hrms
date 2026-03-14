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
        String userId = jwt.getClaimAsString("uid");
        return new CurrentUserContext(
                userId == null ? null : UUID.fromString(userId),
                jwt.getSubject(),
                jwt.getClaimAsString("tenant"),
                roles,
                permissions);
    }

    private Set<String> toStringSet(java.util.List<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().collect(Collectors.toSet());
    }
}
