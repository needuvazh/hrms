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

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String tenantId = exchange.getRequest().getHeaders().getFirst(HrmsHeaders.TENANT_ID);
        if (!StringUtils.hasText(tenantId)) {
            return chain.filter(exchange);
        }

        exchange.getAttributes().put(ExchangeAttributeKeys.TENANT_ID, tenantId);
        return chain.filter(exchange).contextWrite(ReactorTenantContext.withTenantId(tenantId));
    }
}
