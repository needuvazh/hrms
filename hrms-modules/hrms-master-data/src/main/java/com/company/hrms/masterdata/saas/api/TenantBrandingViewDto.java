package com.company.hrms.masterdata.saas.api;

import java.time.Instant;

public record TenantBrandingViewDto(
        String tenantCode,
        String brandName,
        String logoUrl,
        String faviconUrl,
        String primaryColor,
        String secondaryColor,
        String loginBannerUrl,
        String emailLogoUrl,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
