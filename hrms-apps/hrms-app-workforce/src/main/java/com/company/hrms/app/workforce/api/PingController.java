package com.company.hrms.app.workforce.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class PingController {

    @GetMapping("/ping")
    public Mono<PingResponse> ping() {
        return Mono.just(new PingResponse("ok", "workforce"));
    }

    public record PingResponse(String status, String app) {
    }
}
