package com.company.hrms.platform.starter.observability.web;

import com.company.hrms.platform.sharedkernel.context.PlatformContextKeys;
import com.company.hrms.platform.sharedkernel.web.ExchangeAttributeKeys;
import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class CorrelationIdWebFilter implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String headerCorrelationId = exchange.getRequest().getHeaders().getFirst(HrmsHeaders.CORRELATION_ID);
        String correlationId = StringUtils.hasText(headerCorrelationId) ? headerCorrelationId : UUID.randomUUID().toString();

        exchange.getAttributes().put(ExchangeAttributeKeys.CORRELATION_ID, correlationId);
        exchange.getResponse().getHeaders().set(HrmsHeaders.CORRELATION_ID, correlationId);

        return chain.filter(exchange).contextWrite(ctx -> ctx.put(PlatformContextKeys.CORRELATION_ID, correlationId));
    }
}
