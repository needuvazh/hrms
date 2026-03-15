package com.company.hrms.masterdata.saas.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.saas.api.AuditLogViewDto;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanUpsertRequest;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanViewDto;
import com.company.hrms.masterdata.saas.api.FeatureFlagOptionViewDto;
import com.company.hrms.masterdata.saas.api.FeatureFlagUpsertRequest;
import com.company.hrms.masterdata.saas.api.FeatureFlagViewDto;
import com.company.hrms.masterdata.saas.api.TenantBrandingUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantBrandingViewDto;
import com.company.hrms.masterdata.saas.api.TenantCountryUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantCountryViewDto;
import com.company.hrms.masterdata.saas.api.TenantLanguageUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLanguageViewDto;
import com.company.hrms.masterdata.saas.api.TenantLocalizationUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLocalizationViewDto;
import com.company.hrms.masterdata.saas.api.TenantSearchQuery;
import com.company.hrms.masterdata.saas.api.TenantSettingsViewDto;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionViewDto;
import com.company.hrms.masterdata.saas.api.TenantUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantViewDto;
import com.company.hrms.masterdata.saas.api.ToggleViewDto;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SaasMasterService {

    Mono<TenantViewDto> createTenant(TenantUpsertRequest request);

    Mono<TenantViewDto> updateTenant(String tenantCode, TenantUpsertRequest request);

    Mono<TenantViewDto> getTenant(String tenantCode);

    Mono<PagedResult<TenantViewDto>> listTenants(TenantSearchQuery query);

    Mono<Void> updateTenantStatus(String tenantCode, boolean active);

    Mono<TenantLanguageViewDto> upsertTenantLanguage(String tenantCode, String languageCode, TenantLanguageUpsertRequest request);

    Flux<TenantLanguageViewDto> listTenantLanguages(String tenantCode);

    Mono<TenantLanguageViewDto> updateTenantLanguageStatus(String tenantCode, String languageCode, boolean active);

    Mono<TenantLanguageViewDto> updateTenantLanguageDefault(String tenantCode, String languageCode, boolean defaultLanguage);

    Mono<TenantCountryViewDto> upsertTenantCountry(String tenantCode, String countryCode, TenantCountryUpsertRequest request);

    Flux<TenantCountryViewDto> listTenantCountries(String tenantCode);

    Mono<TenantCountryViewDto> updateTenantCountryStatus(String tenantCode, String countryCode, boolean active);

    Mono<TenantCountryViewDto> updateTenantCountryHome(String tenantCode, String countryCode, boolean homeCountry);

    Mono<SubscriptionPlanViewDto> createSubscriptionPlan(SubscriptionPlanUpsertRequest request);

    Mono<SubscriptionPlanViewDto> updateSubscriptionPlan(UUID id, SubscriptionPlanUpsertRequest request);

    Mono<SubscriptionPlanViewDto> getSubscriptionPlan(UUID id);

    Mono<PagedResult<SubscriptionPlanViewDto>> listSubscriptionPlans(TenantSearchQuery query);

    Mono<Void> updateSubscriptionPlanStatus(UUID id, boolean active);

    Flux<SubscriptionPlanViewDto> subscriptionPlanOptions(boolean activeOnly);

    Mono<FeatureFlagViewDto> createFeatureFlag(FeatureFlagUpsertRequest request);

    Mono<FeatureFlagViewDto> updateFeatureFlag(UUID id, FeatureFlagUpsertRequest request);

    Mono<FeatureFlagViewDto> getFeatureFlag(UUID id);

    Mono<PagedResult<FeatureFlagViewDto>> listFeatureFlags(TenantSearchQuery query);

    Mono<Void> updateFeatureFlagStatus(UUID id, boolean active);

    Flux<FeatureFlagOptionViewDto> featureFlagOptions(boolean activeOnly);

    Mono<TenantSubscriptionViewDto> upsertTenantSubscription(String tenantCode, TenantSubscriptionUpsertRequest request);

    Mono<TenantSubscriptionViewDto> getTenantSubscription(String tenantCode);

    Mono<TenantBrandingViewDto> upsertBranding(String tenantCode, TenantBrandingUpsertRequest request);

    Mono<TenantBrandingViewDto> getBranding(String tenantCode);

    Mono<TenantLocalizationViewDto> upsertLocalization(String tenantCode, String countryCode, TenantLocalizationUpsertRequest request);

    Flux<TenantLocalizationViewDto> listLocalization(String tenantCode);

    Mono<Void> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled);

    Mono<Void> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled);

    Flux<ToggleViewDto> listModules(String tenantCode);

    Flux<ToggleViewDto> listFeatures(String tenantCode);

    Mono<TenantSettingsViewDto> getTenantSettings(String tenantCode);

    Mono<PagedResult<AuditLogViewDto>> listAuditLogs(String tenantCode, int page, int size);
}
