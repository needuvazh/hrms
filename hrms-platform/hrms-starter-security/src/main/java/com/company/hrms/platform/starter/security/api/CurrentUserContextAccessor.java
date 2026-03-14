package com.company.hrms.platform.starter.security.api;

import reactor.core.publisher.Mono;

public interface CurrentUserContextAccessor {

    Mono<CurrentUserContext> currentUser();
}
