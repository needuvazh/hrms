package com.company.hrms.masterdata.saas.api;

import java.util.List;

public record TenantSettingsViewDto(
        TenantViewDto tenant,
        TenantSubscriptionViewDto subscription,
        TenantBrandingViewDto branding,
        List<TenantLocalizationViewDto> localizationPreferences,
        List<TenantLanguageViewDto> languages,
        List<TenantCountryViewDto> countries,
        String defaultLanguageCode,
        String homeCountryCode,
        List<ToggleViewDto> enabledModules,
        List<ToggleViewDto> enabledFeatures
) {
}
