package com.company.hrms.platform.starter.tenancy.context;

import com.company.hrms.platform.sharedkernel.context.PlatformContextKeys;
import java.util.function.Function;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public final class ReactorTenantContext {

    private ReactorTenantContext() {
    }

    public static Function<Context, Context> withTenantId(String tenantId) {
        return context -> context.put(PlatformContextKeys.TENANT_ID, tenantId);
    }

    public static Mono<String> currentTenantId() {
        return Mono.deferContextual(contextView -> {
            if (!contextView.hasKey(PlatformContextKeys.TENANT_ID)) {
                return Mono.empty();
            }
            return Mono.just(contextView.get(PlatformContextKeys.TENANT_ID));
        });
    }
}
