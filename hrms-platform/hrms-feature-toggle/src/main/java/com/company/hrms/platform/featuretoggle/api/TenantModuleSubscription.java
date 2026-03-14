package com.company.hrms.platform.featuretoggle.api;

public record TenantModuleSubscription(
        String tenantCode,
        String moduleKey,
        boolean enabled
) {
}
