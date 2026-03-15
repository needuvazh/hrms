package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;

public record TenantBrandingUpsertRequest(
        @NotBlank String brandName,
        String logoUrl,
        String faviconUrl,
        String primaryColor,
        String secondaryColor,
        String loginBannerUrl,
        String emailLogoUrl,
        Boolean active
) {
}
