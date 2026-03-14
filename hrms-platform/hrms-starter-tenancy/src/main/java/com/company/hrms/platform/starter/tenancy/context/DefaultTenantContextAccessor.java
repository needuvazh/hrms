package com.company.hrms.platform.starter.tenancy.context;

import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import reactor.core.publisher.Mono;

public class DefaultTenantContextAccessor implements TenantContextAccessor {

    @Override
    public Mono<String> currentTenantId() {
        return ReactorTenantContext.currentTenantId();
    }
}
