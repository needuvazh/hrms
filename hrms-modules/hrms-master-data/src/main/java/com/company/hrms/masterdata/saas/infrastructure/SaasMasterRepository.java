package com.company.hrms.masterdata.saas.infrastructure;

import com.company.hrms.masterdata.saas.api.AuditLogViewDto;
import com.company.hrms.masterdata.saas.api.FeatureFlagOptionViewDto;
import com.company.hrms.masterdata.saas.api.FeatureFlagUpsertRequest;
import com.company.hrms.masterdata.saas.api.FeatureFlagViewDto;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanUpsertRequest;
import com.company.hrms.masterdata.saas.api.SubscriptionPlanViewDto;
import com.company.hrms.masterdata.saas.api.TenantBrandingUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantBrandingViewDto;
import com.company.hrms.masterdata.saas.api.TenantCountryUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantCountryViewDto;
import com.company.hrms.masterdata.saas.api.TenantLanguageUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLanguageViewDto;
import com.company.hrms.masterdata.saas.api.TenantLocalizationUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLocalizationViewDto;
import com.company.hrms.masterdata.saas.api.TenantSearchQuery;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionViewDto;
import com.company.hrms.masterdata.saas.api.TenantUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantViewDto;
import com.company.hrms.masterdata.saas.api.ToggleViewDto;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SaasMasterRepository {

    Mono<Boolean> tenantCodeExists(String tenantCode);

    Mono<TenantViewDto> createTenant(TenantUpsertRequest request, String actor);

    Mono<TenantViewDto> updateTenant(String tenantCode, TenantUpsertRequest request, String actor);

    Mono<TenantViewDto> findTenantByCode(String tenantCode);

    Flux<TenantViewDto> listTenants(TenantSearchQuery query);

    Mono<Long> countTenants(TenantSearchQuery query);

    Mono<Void> updateTenantStatus(String tenantCode, boolean active);

    Mono<TenantLanguageViewDto> upsertTenantLanguage(String tenantCode, String languageCode, TenantLanguageUpsertRequest request, String actor);

    Flux<TenantLanguageViewDto> listTenantLanguages(String tenantCode);

    Mono<TenantLanguageViewDto> updateTenantLanguageStatus(String tenantCode, String languageCode, boolean active, String actor);

    Mono<TenantLanguageViewDto> updateTenantLanguageDefault(String tenantCode, String languageCode, boolean defaultLanguage, String actor);

    Mono<Long> countDefaultLanguages(String tenantCode, Boolean activeOnly);

    Mono<TenantCountryViewDto> upsertTenantCountry(String tenantCode, String countryCode, TenantCountryUpsertRequest request, String actor);

    Flux<TenantCountryViewDto> listTenantCountries(String tenantCode);

    Mono<TenantCountryViewDto> updateTenantCountryStatus(String tenantCode, String countryCode, boolean active, String actor);

    Mono<TenantCountryViewDto> updateTenantCountryHome(String tenantCode, String countryCode, boolean homeCountry, String actor);

    Mono<Long> countHomeCountries(String tenantCode, Boolean activeOnly);

    Mono<Boolean> subscriptionPlanCodeExists(String planCode, UUID excludeId);

    Mono<SubscriptionPlanViewDto> createSubscriptionPlan(SubscriptionPlanUpsertRequest request, String actor);

    Mono<SubscriptionPlanViewDto> updateSubscriptionPlan(UUID id, SubscriptionPlanUpsertRequest request, String actor);

    Mono<SubscriptionPlanViewDto> findSubscriptionPlanById(UUID id);

    Flux<SubscriptionPlanViewDto> listSubscriptionPlans(TenantSearchQuery query);

    Mono<Long> countSubscriptionPlans(TenantSearchQuery query);

    Mono<Void> updateSubscriptionPlanStatus(UUID id, boolean active, String actor);

    Flux<SubscriptionPlanViewDto> subscriptionPlanOptions(boolean activeOnly);

    Mono<Boolean> featureFlagCodeExists(String featureKey, UUID excludeId);

    Mono<FeatureFlagViewDto> createFeatureFlag(FeatureFlagUpsertRequest request, String actor);

    Mono<FeatureFlagViewDto> updateFeatureFlag(UUID id, FeatureFlagUpsertRequest request, String actor);

    Mono<FeatureFlagViewDto> findFeatureFlagById(UUID id);

    Flux<FeatureFlagViewDto> listFeatureFlags(TenantSearchQuery query);

    Mono<Long> countFeatureFlags(TenantSearchQuery query);

    Mono<Void> updateFeatureFlagStatus(UUID id, boolean active, String actor);

    Flux<FeatureFlagOptionViewDto> featureFlagOptions(boolean activeOnly);

    Mono<Boolean> featureFlagExists(String featureKey);

    Mono<Boolean> subscriptionPlanExists(UUID id);

    Mono<TenantSubscriptionViewDto> upsertTenantSubscription(String tenantCode, TenantSubscriptionUpsertRequest request, String actor);

    Mono<TenantSubscriptionViewDto> findTenantSubscription(String tenantCode);

    Mono<TenantBrandingViewDto> upsertBranding(String tenantCode, TenantBrandingUpsertRequest request, String actor);

    Mono<TenantBrandingViewDto> findBranding(String tenantCode);

    Mono<TenantLocalizationViewDto> upsertLocalization(String tenantCode, String countryCode, TenantLocalizationUpsertRequest request, String actor);

    Flux<TenantLocalizationViewDto> listLocalization(String tenantCode);

    Mono<Void> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled);

    Mono<Void> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled);

    Flux<ToggleViewDto> listModules(String tenantCode);

    Flux<ToggleViewDto> listFeatures(String tenantCode);

    Flux<AuditLogViewDto> listAuditLogs(String tenantCode, int page, int size);

    Mono<Long> countAuditLogs(String tenantCode);

    Mono<Boolean> languageCodeExists(String languageCode);

    Mono<Boolean> countryCodeExists(String countryCode);

    Mono<Boolean> currencyCodeExists(String currencyCode);
}
