package com.company.hrms.platform.starter.observability.web;

import com.company.hrms.platform.sharedkernel.web.ExchangeAttributeKeys;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class RequestObservabilityWebFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestObservabilityWebFilter.class);

    private final MeterRegistry meterRegistry;

    public RequestObservabilityWebFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startNanos = System.nanoTime();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long durationNanos = System.nanoTime() - startNanos;
                    int statusCode = exchange.getResponse().getStatusCode() == null
                            ? 200
                            : exchange.getResponse().getStatusCode().value();

                    String method = exchange.getRequest().getMethod() == null
                            ? "UNKNOWN"
                            : exchange.getRequest().getMethod().name();
                    String route = routeTag(exchange);
                    String outcome = statusCode >= 500 ? "SERVER_ERROR"
                            : statusCode >= 400 ? "CLIENT_ERROR"
                            : "SUCCESS";

                    Timer.builder("hrms.http.server.requests")
                            .tag("method", method)
                            .tag("route", route)
                            .tag("status", Integer.toString(statusCode))
                            .tag("outcome", outcome)
                            .tag("tenant_present", tenantId(exchange) == null ? "false" : "true")
                            .register(meterRegistry)
                            .record(durationNanos, TimeUnit.NANOSECONDS);

                    log.info(
                            "http_request method={} route={} status={} duration_ms={} correlation_id={} tenant_id={} trace_id={} span_id={}",
                            method,
                            route,
                            statusCode,
                            TimeUnit.NANOSECONDS.toMillis(durationNanos),
                            correlationId(exchange),
                            tenantId(exchange) == null ? "n/a" : tenantId(exchange),
                            traceId(exchange),
                            spanId(exchange));
                });
    }

    private String routeTag(ServerWebExchange exchange) {
        Object attribute = exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (attribute != null) {
            return attribute.toString();
        }
        return exchange.getRequest().getPath().pathWithinApplication().value();
    }

    private String correlationId(ServerWebExchange exchange) {
        Object correlation = exchange.getAttribute(ExchangeAttributeKeys.CORRELATION_ID);
        return correlation == null ? "n/a" : correlation.toString();
    }

    private String tenantId(ServerWebExchange exchange) {
        Object tenant = exchange.getAttribute(ExchangeAttributeKeys.TENANT_ID);
        return tenant == null ? null : tenant.toString();
    }

    private String traceId(ServerWebExchange exchange) {
        Object trace = exchange.getAttribute(ExchangeAttributeKeys.TRACE_ID);
        return trace == null ? "n/a" : trace.toString();
    }

    private String spanId(ServerWebExchange exchange) {
        Object span = exchange.getAttribute(ExchangeAttributeKeys.SPAN_ID);
        return span == null ? "n/a" : span.toString();
    }
}
