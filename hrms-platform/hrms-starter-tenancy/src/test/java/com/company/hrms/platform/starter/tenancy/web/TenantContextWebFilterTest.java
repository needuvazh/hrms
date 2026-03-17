package com.company.hrms.platform.starter.tenancy.web;

import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TenantContextWebFilterTest {

    private final TenantContextWebFilter tenantContextWebFilter = new TenantContextWebFilter("default");

    @Test
    void propagatesTenantIdFromHeaderToReactorContext() {
        AtomicReference<String> capturedTenant = new AtomicReference<>();

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/employees")
                        .header(HrmsHeaders.TENANT_ID, "tenant-a")
                        .build());

        WebFilterChain chain = ex -> ReactorTenantContext.currentTenantId()
                .doOnNext(capturedTenant::set)
                .then();

        StepVerifier.create(tenantContextWebFilter.filter(exchange, chain)).verifyComplete();

        org.junit.jupiter.api.Assertions.assertEquals("tenant-a", capturedTenant.get());
    }

    @Test
    void usesDefaultTenantWhenHeaderMissing() {
        AtomicReference<String> capturedTenant = new AtomicReference<>();

        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/employees").build());

        WebFilterChain chain = ex -> ReactorTenantContext.currentTenantId()
                .doOnNext(capturedTenant::set)
                .then();

        StepVerifier.create(tenantContextWebFilter.filter(exchange, chain)).verifyComplete();

        org.junit.jupiter.api.Assertions.assertEquals("default", capturedTenant.get());
    }
}
