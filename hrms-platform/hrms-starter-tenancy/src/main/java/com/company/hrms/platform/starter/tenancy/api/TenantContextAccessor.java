package com.company.hrms.platform.starter.tenancy.api;

import reactor.core.publisher.Mono;

public interface TenantContextAccessor {

    Mono<String> currentTenantId();
}
