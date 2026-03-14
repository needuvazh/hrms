package com.company.hrms.platform.featuretoggle.api;

public record TenantFeatureFlag(
        String tenantCode,
        String featureKey,
        boolean enabled
) {
}
