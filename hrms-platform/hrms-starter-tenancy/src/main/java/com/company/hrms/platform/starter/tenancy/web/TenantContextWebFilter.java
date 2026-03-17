package com.company.hrms.platform.starter.tenancy.web;

import com.company.hrms.platform.sharedkernel.web.ExchangeAttributeKeys;
import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class TenantContextWebFilter implements WebFilter, Ordered {

    private final String defaultTenantId;

    public TenantContextWebFilter() {
        this("default");
    }

    public TenantContextWebFilter(String defaultTenantId) {
        this.defaultTenantId = normalize(defaultTenantId);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String tenantId = normalize(exchange.getRequest().getHeaders().getFirst(HrmsHeaders.TENANT_ID));
        if (!StringUtils.hasText(tenantId)) {
            tenantId = defaultTenantId;
        }
        if (!StringUtils.hasText(tenantId)) {
            return chain.filter(exchange);
        }

        exchange.getAttributes().put(ExchangeAttributeKeys.TENANT_ID, tenantId);
        return chain.filter(exchange).contextWrite(ReactorTenantContext.withTenantId(tenantId));
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
