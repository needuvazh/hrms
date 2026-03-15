package com.company.hrms.platform.starter.observability.web;

import com.company.hrms.platform.sharedkernel.web.ExchangeAttributeKeys;
import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class TraceResponseWebFilter implements WebFilter, Ordered {

    private final Tracer tracer;

    public TraceResponseWebFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            TraceIds traceIds = resolveTraceIds(exchange);

            exchange.getAttributes().put(ExchangeAttributeKeys.TRACE_ID, traceIds.traceId());
            exchange.getAttributes().put(ExchangeAttributeKeys.SPAN_ID, traceIds.spanId());
            exchange.getResponse().getHeaders().set(HrmsHeaders.TRACE_ID, traceIds.traceId());
            exchange.getResponse().getHeaders().set(HrmsHeaders.SPAN_ID, traceIds.spanId());
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    private TraceIds resolveTraceIds(ServerWebExchange exchange) {
        TraceContext traceContext = currentTraceContext();
        if (traceContext != null && StringUtils.hasText(traceContext.traceId()) && StringUtils.hasText(traceContext.spanId())) {
            return new TraceIds(traceContext.traceId(), traceContext.spanId());
        }

        String traceIdFromHeader = parseTraceParent(exchange, 3, 35);
        String spanIdFromHeader = parseTraceParent(exchange, 36, 52);
        if (StringUtils.hasText(traceIdFromHeader) && StringUtils.hasText(spanIdFromHeader)) {
            return new TraceIds(traceIdFromHeader, spanIdFromHeader);
        }

        String generatedTraceId = randomHex(32);
        String generatedSpanId = randomHex(16);
        return new TraceIds(generatedTraceId, generatedSpanId);
    }

    private TraceContext currentTraceContext() {
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        return span.context();
    }

    private String parseTraceParent(ServerWebExchange exchange, int start, int end) {
        String traceParent = exchange.getRequest().getHeaders().getFirst("traceparent");
        if (!StringUtils.hasText(traceParent) || traceParent.length() < end) {
            return null;
        }
        return traceParent.substring(start, end);
    }

    private String randomHex(int length) {
        String value = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        return value.substring(0, length);
    }

    private record TraceIds(String traceId, String spanId) {
    }
}
