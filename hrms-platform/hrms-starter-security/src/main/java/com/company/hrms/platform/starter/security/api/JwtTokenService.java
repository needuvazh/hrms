package com.company.hrms.platform.starter.security.api;

import reactor.core.publisher.Mono;

public interface JwtTokenService {

    Mono<JwtTokenValue> issueToken(JwtTokenClaims claims);
}
