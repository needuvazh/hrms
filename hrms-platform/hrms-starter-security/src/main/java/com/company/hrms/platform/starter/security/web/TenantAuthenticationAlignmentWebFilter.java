package com.company.hrms.platform.starter.security.web;

import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import org.springframework.http.HttpStatus;
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

        return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(jwt -> jwt.getClaimAsString("tenant"))
                .defaultIfEmpty(requestTenant)
                .flatMap(tokenTenant -> {
                    if (StringUtils.hasText(requestTenant)
                            && StringUtils.hasText(tokenTenant)
                            && !requestTenant.equals(tokenTenant)) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant header/token mismatch"));
                    }
                    return chain.filter(exchange);
                });
    }
}
