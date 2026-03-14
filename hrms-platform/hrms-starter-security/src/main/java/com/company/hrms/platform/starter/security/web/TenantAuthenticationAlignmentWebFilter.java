package com.company.hrms.platform.starter.security.web;

import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class TenantAuthenticationAlignmentWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestTenant = exchange.getRequest().getHeaders().getFirst(HrmsHeaders.TENANT_ID);

        Mono<Jwt> jwtMono = ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .cache();

        return jwtMono.hasElement()
                .flatMap(hasJwt -> {
                    if (!hasJwt) {
                        return chain.filter(exchange);
                    }
                    return jwtMono.flatMap(jwt -> {
                    if (Boolean.TRUE.equals(jwt.getClaimAsBoolean("super_admin"))
                            || Boolean.TRUE.equals(jwt.getClaimAsBoolean("can_view_all_tenants"))) {
                        return chain.filter(exchange);
                    }
                    return validateTenantAlignment(jwt, requestTenant, exchange, chain);
                });
                });
    }

    private Mono<Void> validateTenantAlignment(
            Jwt jwt,
            String requestTenant,
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        String tokenTenant = jwt.getClaimAsString("tenant");
        if (StringUtils.hasText(requestTenant)
                && StringUtils.hasText(tokenTenant)
                && !requestTenant.equals(tokenTenant)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant header/token mismatch"));
        }
        return chain.filter(exchange);
    }
}
