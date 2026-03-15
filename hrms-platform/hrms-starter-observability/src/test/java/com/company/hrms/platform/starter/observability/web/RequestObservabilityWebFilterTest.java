package com.company.hrms.platform.starter.observability.web;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestObservabilityWebFilterTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private final WebTestClient webTestClient = WebTestClient.bindToController(new TestController())
            .webFilter(new CorrelationIdWebFilter())
            .webFilter(new TraceResponseWebFilter(null))
            .webFilter(new RequestObservabilityWebFilter(meterRegistry))
            .build();

    @Test
    void recordsHttpMetricAndPropagatesCorrelationHeader() {
        String correlationId = "corr-123";

        webTestClient.get()
                .uri("/api/v1/ping")
                .header("X-Correlation-Id", correlationId)
                .header("X-Tenant-Id", "default")
                .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-Id", correlationId)
                .expectHeader().valueEquals("X-Trace-Id", "4bf92f3577b34da6a3ce929d0e0e4736")
                .expectHeader().valueEquals("X-Span-Id", "00f067aa0ba902b7")
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");

        assertNotNull(meterRegistry.find("hrms.http.server.requests").timer());
        assertEquals(1L, (long) meterRegistry.find("hrms.http.server.requests").timer().count());
    }

    @RestController
    @RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
    static class TestController {

        @GetMapping("/ping")
        Mono<PingResponse> ping() {
            return Mono.just(new PingResponse("ok"));
        }
    }

    record PingResponse(String status) {
    }
}
