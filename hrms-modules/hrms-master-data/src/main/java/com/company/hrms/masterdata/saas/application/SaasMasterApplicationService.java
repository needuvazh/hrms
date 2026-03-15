package com.company.hrms.masterdata.saas.application;

import com.company.hrms.masterdata.reference.api.PagedResult;
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
import com.company.hrms.masterdata.saas.api.TenantSettingsViewDto;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionViewDto;
import com.company.hrms.masterdata.saas.api.TenantUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantViewDto;
import com.company.hrms.masterdata.saas.api.ToggleViewDto;
import com.company.hrms.masterdata.saas.infrastructure.SaasMasterRepository;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SaasMasterApplicationService implements SaasMasterService {

    private static final String ACTOR = "system";

    private final SaasMasterRepository repository;
    private final AuditEventPublisher auditEventPublisher;

    public SaasMasterApplicationService(SaasMasterRepository repository, AuditEventPublisher auditEventPublisher) {
        this.repository = repository;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<TenantViewDto> createTenant(TenantUpsertRequest request) {
        return validateTenantRequest(request)
                .then(repository.tenantCodeExists(request.tenantCode().trim()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "TENANT_CODE_EXISTS", "Tenant code already exists"));
                    }
                    return repository.createTenant(request, ACTOR);
                })
                .flatMap(view -> publishAudit(view.tenantCode(), "TENANT_CREATED", "tenant", view.tenantCode(), Map.of("tenantName", view.tenantName()))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantViewDto> updateTenant(String tenantCode, TenantUpsertRequest request) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateTenantRequest(request))
                .then(repository.updateTenant(tenantCode, request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_UPDATED", "tenant", tenantCode, Map.of("tenantName", view.tenantName()))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantViewDto> getTenant(String tenantCode) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")));
    }

    @Override
    public Mono<PagedResult<TenantViewDto>> listTenants(TenantSearchQuery query) {
        TenantSearchQuery normalized = normalizeQuery(query);
        return repository.listTenants(normalized)
                .collectList()
                .zipWith(repository.countTenants(normalized))
                .map(tuple -> {
                    int responsePage = normalized.all() ? 0 : normalized.page();
                    int responseSize = normalized.all() ? tuple.getT1().size() : normalized.size();
                    int totalPages = normalized.all()
                            ? (tuple.getT2() == 0 ? 0 : 1)
                            : (tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / normalized.size()));
                    return new PagedResult<>(
                            tuple.getT1(),
                            responsePage,
                            responseSize,
                            tuple.getT2(),
                            totalPages);
                });
    }

    @Override
    public Mono<Void> updateTenantStatus(String tenantCode, boolean active) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(repository.updateTenantStatus(tenantCode, active))
                .then(publishAudit(tenantCode, "TENANT_STATUS_CHANGED", "tenant", tenantCode, Map.of("active", active)));
    }

    @Override
    public Mono<TenantLanguageViewDto> upsertTenantLanguage(String tenantCode, String languageCode, TenantLanguageUpsertRequest request) {
        if (!StringUtils.hasText(languageCode)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LANGUAGE_CODE_REQUIRED", "Language code is required"));
        }
        String effectiveLanguageCode = languageCode.trim();
        if (StringUtils.hasText(request.languageCode()) && !effectiveLanguageCode.equalsIgnoreCase(request.languageCode().trim())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LANGUAGE_CODE_MISMATCH", "Path language code must match request language code"));
        }
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateTenantLanguageRequest(request, effectiveLanguageCode))
                .then(repository.upsertTenantLanguage(tenantCode, effectiveLanguageCode, request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_LANGUAGE_UPSERTED", "tenant_language", tenantCode + ":" + effectiveLanguageCode,
                                Map.of("languageCode", effectiveLanguageCode, "defaultLanguage", view.defaultLanguage(), "active", view.active()))
                        .thenReturn(view));
    }

    @Override
    public Flux<TenantLanguageViewDto> listTenantLanguages(String tenantCode) {
        return repository.listTenantLanguages(tenantCode);
    }

    @Override
    public Mono<TenantLanguageViewDto> updateTenantLanguageStatus(String tenantCode, String languageCode, boolean active) {
        return repository.listTenantLanguages(tenantCode)
                .filter(item -> item.languageCode().equalsIgnoreCase(languageCode))
                .next()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_LANGUAGE_NOT_FOUND", "Tenant language not found")))
                .flatMap(existing -> {
                    if (existing.defaultLanguage() && !active) {
                        return repository.countDefaultLanguages(tenantCode, Boolean.TRUE)
                                .flatMap(defaultCount -> {
                                    if (defaultCount <= 1) {
                                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DEFAULT_LANGUAGE_REQUIRED", "Cannot disable the only active default language"));
                                    }
                                    return repository.updateTenantLanguageStatus(tenantCode, existing.languageCode(), false, ACTOR);
                                });
                    }
                    return repository.updateTenantLanguageStatus(tenantCode, existing.languageCode(), active, ACTOR);
                })
                .flatMap(view -> publishAudit(tenantCode, "TENANT_LANGUAGE_STATUS_UPDATED", "tenant_language", tenantCode + ":" + view.languageCode(),
                                Map.of("languageCode", view.languageCode(), "active", active))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantLanguageViewDto> updateTenantLanguageDefault(String tenantCode, String languageCode, boolean defaultLanguage) {
        if (!defaultLanguage) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DEFAULT_LANGUAGE_UNSET_NOT_ALLOWED", "Use another language as default instead of unsetting"));
        }
        return repository.listTenantLanguages(tenantCode)
                .filter(item -> item.languageCode().equalsIgnoreCase(languageCode))
                .next()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_LANGUAGE_NOT_FOUND", "Tenant language not found")))
                .flatMap(existing -> {
                    if (!existing.active()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "DEFAULT_LANGUAGE_MUST_BE_ACTIVE", "Default language must be active"));
                    }
                    return repository.updateTenantLanguageDefault(tenantCode, existing.languageCode(), true, ACTOR);
                })
                .flatMap(view -> publishAudit(tenantCode, "TENANT_LANGUAGE_DEFAULT_UPDATED", "tenant_language", tenantCode + ":" + view.languageCode(),
                                Map.of("languageCode", view.languageCode(), "defaultLanguage", true))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantCountryViewDto> upsertTenantCountry(String tenantCode, String countryCode, TenantCountryUpsertRequest request) {
        if (!StringUtils.hasText(countryCode)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_CODE_REQUIRED", "Country code is required"));
        }
        String effectiveCountryCode = countryCode.trim();
        if (StringUtils.hasText(request.countryCode()) && !effectiveCountryCode.equalsIgnoreCase(request.countryCode().trim())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_CODE_MISMATCH", "Path country code must match request country code"));
        }
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateTenantCountryRequest(request, effectiveCountryCode))
                .then(repository.upsertTenantCountry(tenantCode, effectiveCountryCode, request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_COUNTRY_UPSERTED", "tenant_country", tenantCode + ":" + effectiveCountryCode,
                                Map.of("countryCode", effectiveCountryCode, "homeCountry", view.homeCountry(), "active", view.active()))
                        .thenReturn(view));
    }

    @Override
    public Flux<TenantCountryViewDto> listTenantCountries(String tenantCode) {
        return repository.listTenantCountries(tenantCode);
    }

    @Override
    public Mono<TenantCountryViewDto> updateTenantCountryStatus(String tenantCode, String countryCode, boolean active) {
        return repository.listTenantCountries(tenantCode)
                .filter(item -> item.countryCode().equalsIgnoreCase(countryCode))
                .next()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_COUNTRY_NOT_FOUND", "Tenant country not found")))
                .flatMap(existing -> {
                    if (existing.homeCountry() && !active) {
                        return repository.countHomeCountries(tenantCode, Boolean.TRUE)
                                .flatMap(homeCount -> {
                                    if (homeCount <= 1) {
                                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "HOME_COUNTRY_REQUIRED", "Cannot disable the only active home country"));
                                    }
                                    return repository.updateTenantCountryStatus(tenantCode, existing.countryCode(), false, ACTOR);
                                });
                    }
                    return repository.updateTenantCountryStatus(tenantCode, existing.countryCode(), active, ACTOR);
                })
                .flatMap(view -> publishAudit(tenantCode, "TENANT_COUNTRY_STATUS_UPDATED", "tenant_country", tenantCode + ":" + view.countryCode(),
                                Map.of("countryCode", view.countryCode(), "active", active))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantCountryViewDto> updateTenantCountryHome(String tenantCode, String countryCode, boolean homeCountry) {
        if (!homeCountry) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "HOME_COUNTRY_UNSET_NOT_ALLOWED", "Use another country as home country instead of unsetting"));
        }
        return repository.listTenantCountries(tenantCode)
                .filter(item -> item.countryCode().equalsIgnoreCase(countryCode))
                .next()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_COUNTRY_NOT_FOUND", "Tenant country not found")))
                .flatMap(existing -> {
                    if (!existing.active()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "HOME_COUNTRY_MUST_BE_ACTIVE", "Home country must be active"));
                    }
                    return repository.updateTenantCountryHome(tenantCode, existing.countryCode(), true, ACTOR);
                })
                .flatMap(view -> publishAudit(tenantCode, "TENANT_COUNTRY_HOME_UPDATED", "tenant_country", tenantCode + ":" + view.countryCode(),
                                Map.of("countryCode", view.countryCode(), "homeCountry", true))
                        .thenReturn(view));
    }

    @Override
    public Mono<SubscriptionPlanViewDto> createSubscriptionPlan(SubscriptionPlanUpsertRequest request) {
        return validateSubscriptionPlanRequest(request)
                .then(repository.subscriptionPlanCodeExists(request.planCode().trim(), null))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "SUBSCRIPTION_PLAN_CODE_EXISTS", "Subscription plan code already exists"));
                    }
                    return repository.createSubscriptionPlan(request, ACTOR);
                })
                .flatMap(plan -> publishAudit("platform", "SUBSCRIPTION_PLAN_CREATED", "subscription_plan", String.valueOf(plan.id()), Map.of("planCode", plan.planCode()))
                        .thenReturn(plan));
    }

    @Override
    public Mono<SubscriptionPlanViewDto> updateSubscriptionPlan(UUID id, SubscriptionPlanUpsertRequest request) {
        return repository.findSubscriptionPlanById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "SUBSCRIPTION_PLAN_NOT_FOUND", "Subscription plan not found")))
                .then(validateSubscriptionPlanRequest(request))
                .then(repository.subscriptionPlanCodeExists(request.planCode().trim(), id))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "SUBSCRIPTION_PLAN_CODE_EXISTS", "Subscription plan code already exists"));
                    }
                    return repository.updateSubscriptionPlan(id, request, ACTOR);
                })
                .flatMap(plan -> publishAudit("platform", "SUBSCRIPTION_PLAN_UPDATED", "subscription_plan", String.valueOf(plan.id()), Map.of("planCode", plan.planCode()))
                        .thenReturn(plan));
    }

    @Override
    public Mono<SubscriptionPlanViewDto> getSubscriptionPlan(UUID id) {
        return repository.findSubscriptionPlanById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "SUBSCRIPTION_PLAN_NOT_FOUND", "Subscription plan not found")));
    }

    @Override
    public Mono<PagedResult<SubscriptionPlanViewDto>> listSubscriptionPlans(TenantSearchQuery query) {
        TenantSearchQuery normalized = normalizeQuery(query);
        return repository.listSubscriptionPlans(normalized)
                .collectList()
                .zipWith(repository.countSubscriptionPlans(normalized))
                .map(tuple -> {
                    int responsePage = normalized.all() ? 0 : normalized.page();
                    int responseSize = normalized.all() ? tuple.getT1().size() : normalized.size();
                    int totalPages = normalized.all()
                            ? (tuple.getT2() == 0 ? 0 : 1)
                            : (tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / normalized.size()));
                    return new PagedResult<>(
                            tuple.getT1(),
                            responsePage,
                            responseSize,
                            tuple.getT2(),
                            totalPages);
                });
    }

    @Override
    public Mono<Void> updateSubscriptionPlanStatus(UUID id, boolean active) {
        return repository.findSubscriptionPlanById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "SUBSCRIPTION_PLAN_NOT_FOUND", "Subscription plan not found")))
                .then(repository.updateSubscriptionPlanStatus(id, active, ACTOR))
                .then(publishAudit("platform", "SUBSCRIPTION_PLAN_STATUS_CHANGED", "subscription_plan", String.valueOf(id), Map.of("active", active)));
    }

    @Override
    public Flux<SubscriptionPlanViewDto> subscriptionPlanOptions(boolean activeOnly) {
        return repository.subscriptionPlanOptions(activeOnly);
    }

    @Override
    public Mono<FeatureFlagViewDto> createFeatureFlag(FeatureFlagUpsertRequest request) {
        return validateFeatureFlagRequest(request)
                .then(repository.featureFlagCodeExists(request.featureKey().trim(), null))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "FEATURE_FLAG_KEY_EXISTS", "Feature flag key already exists"));
                    }
                    return repository.createFeatureFlag(request, ACTOR);
                })
                .flatMap(view -> publishAudit("platform", "FEATURE_FLAG_CREATED", "feature_flag", String.valueOf(view.id()),
                                Map.of("featureKey", view.featureKey()))
                        .thenReturn(view));
    }

    @Override
    public Mono<FeatureFlagViewDto> updateFeatureFlag(UUID id, FeatureFlagUpsertRequest request) {
        return repository.findFeatureFlagById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "FEATURE_FLAG_NOT_FOUND", "Feature flag not found")))
                .then(validateFeatureFlagRequest(request))
                .then(repository.featureFlagCodeExists(request.featureKey().trim(), id))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new HrmsException(HttpStatus.CONFLICT, "FEATURE_FLAG_KEY_EXISTS", "Feature flag key already exists"));
                    }
                    return repository.updateFeatureFlag(id, request, ACTOR);
                })
                .flatMap(view -> publishAudit("platform", "FEATURE_FLAG_UPDATED", "feature_flag", String.valueOf(view.id()),
                                Map.of("featureKey", view.featureKey()))
                        .thenReturn(view));
    }

    @Override
    public Mono<FeatureFlagViewDto> getFeatureFlag(UUID id) {
        return repository.findFeatureFlagById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "FEATURE_FLAG_NOT_FOUND", "Feature flag not found")));
    }

    @Override
    public Mono<PagedResult<FeatureFlagViewDto>> listFeatureFlags(TenantSearchQuery query) {
        TenantSearchQuery normalized = normalizeQuery(query);
        return repository.listFeatureFlags(normalized)
                .collectList()
                .zipWith(repository.countFeatureFlags(normalized))
                .map(tuple -> {
                    int responsePage = normalized.all() ? 0 : normalized.page();
                    int responseSize = normalized.all() ? tuple.getT1().size() : normalized.size();
                    int totalPages = normalized.all()
                            ? (tuple.getT2() == 0 ? 0 : 1)
                            : (tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / normalized.size()));
                    return new PagedResult<>(
                            tuple.getT1(),
                            responsePage,
                            responseSize,
                            tuple.getT2(),
                            totalPages);
                });
    }

    @Override
    public Mono<Void> updateFeatureFlagStatus(UUID id, boolean active) {
        return repository.findFeatureFlagById(id)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "FEATURE_FLAG_NOT_FOUND", "Feature flag not found")))
                .then(repository.updateFeatureFlagStatus(id, active, ACTOR))
                .then(publishAudit("platform", "FEATURE_FLAG_STATUS_CHANGED", "feature_flag", String.valueOf(id), Map.of("active", active)));
    }

    @Override
    public Flux<FeatureFlagOptionViewDto> featureFlagOptions(boolean activeOnly) {
        return repository.featureFlagOptions(activeOnly);
    }

    @Override
    public Mono<TenantSubscriptionViewDto> upsertTenantSubscription(String tenantCode, TenantSubscriptionUpsertRequest request) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateTenantSubscription(request))
                .then(repository.subscriptionPlanExists(request.subscriptionPlanId()))
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_PLAN_INVALID", "Subscription plan is invalid")))
                .then(repository.upsertTenantSubscription(tenantCode, request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_SUBSCRIPTION_UPDATED", "tenant_subscription", tenantCode,
                                Map.of("subscriptionPlanId", String.valueOf(view.subscriptionPlanId())))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantSubscriptionViewDto> getTenantSubscription(String tenantCode) {
        return repository.findTenantSubscription(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_SUBSCRIPTION_NOT_FOUND", "Tenant subscription not found")));
    }

    @Override
    public Mono<TenantBrandingViewDto> upsertBranding(String tenantCode, TenantBrandingUpsertRequest request) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateBranding(request))
                .then(repository.upsertBranding(tenantCode, request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_BRANDING_UPDATED", "tenant_branding", tenantCode, Map.of("brandName", view.brandName()))
                        .thenReturn(view));
    }

    @Override
    public Mono<TenantBrandingViewDto> getBranding(String tenantCode) {
        return repository.findBranding(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_BRANDING_NOT_FOUND", "Tenant branding not found")));
    }

    @Override
    public Mono<TenantLocalizationViewDto> upsertLocalization(String tenantCode, String countryCode, TenantLocalizationUpsertRequest request) {
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(validateLocalization(countryCode, request))
                .then(repository.upsertLocalization(tenantCode, countryCode.trim(), request, ACTOR))
                .flatMap(view -> publishAudit(tenantCode, "TENANT_LOCALIZATION_UPDATED", "tenant_localization",
                                tenantCode + ":" + countryCode,
                                Map.of("countryCode", countryCode))
                        .thenReturn(view));
    }

    @Override
    public Flux<TenantLocalizationViewDto> listLocalization(String tenantCode) {
        return repository.listLocalization(tenantCode);
    }

    @Override
    public Mono<Void> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled) {
        if (!StringUtils.hasText(moduleKey)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "MODULE_KEY_REQUIRED", "Module key is required"));
        }
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(repository.setModuleEnabled(tenantCode, moduleKey.trim(), enabled))
                .then(publishAudit(tenantCode, "TENANT_MODULE_UPDATED", "tenant_module", tenantCode + ":" + moduleKey,
                        Map.of("moduleKey", moduleKey, "enabled", enabled)));
    }

    @Override
    public Mono<Void> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled) {
        if (!StringUtils.hasText(featureKey)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FEATURE_KEY_REQUIRED", "Feature key is required"));
        }
        String normalizedFeatureKey = featureKey.trim();
        return repository.findTenantByCode(tenantCode)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", "Tenant not found")))
                .then(repository.featureFlagExists(normalizedFeatureKey)
                        .flatMap(exists -> exists
                                ? Mono.empty()
                                : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FEATURE_FLAG_NOT_FOUND", "Feature flag key is invalid"))))
                .then(repository.setFeatureEnabled(tenantCode, normalizedFeatureKey, enabled))
                .then(publishAudit(tenantCode, "TENANT_FEATURE_UPDATED", "tenant_feature", tenantCode + ":" + normalizedFeatureKey,
                        Map.of("featureKey", normalizedFeatureKey, "enabled", enabled)));
    }

    @Override
    public Flux<ToggleViewDto> listModules(String tenantCode) {
        return repository.listModules(tenantCode);
    }

    @Override
    public Flux<ToggleViewDto> listFeatures(String tenantCode) {
        return repository.listFeatures(tenantCode);
    }

    @Override
    public Mono<TenantSettingsViewDto> getTenantSettings(String tenantCode) {
        Mono<TenantViewDto> tenant = getTenant(tenantCode);
        Mono<Optional<TenantSubscriptionViewDto>> subscription = repository.findTenantSubscription(tenantCode)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
        Mono<Optional<TenantBrandingViewDto>> branding = repository.findBranding(tenantCode)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
        Mono<java.util.List<TenantLocalizationViewDto>> localization = repository.listLocalization(tenantCode).collectList();
        Mono<java.util.List<TenantLanguageViewDto>> languages = repository.listTenantLanguages(tenantCode).collectList();
        Mono<java.util.List<TenantCountryViewDto>> countries = repository.listTenantCountries(tenantCode).collectList();
        Mono<java.util.List<ToggleViewDto>> modules = repository.listModules(tenantCode).collectList();
        Mono<java.util.List<ToggleViewDto>> features = repository.listFeatures(tenantCode).collectList();
        return Mono.zip(tenant, subscription, branding, localization, languages, countries, modules, features)
                .map(tuple -> new TenantSettingsViewDto(
                        tuple.getT1(),
                        tuple.getT2().orElse(null),
                        tuple.getT3().orElse(null),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6(),
                        tuple.getT5().stream().filter(TenantLanguageViewDto::defaultLanguage).map(TenantLanguageViewDto::languageCode).findFirst().orElse(null),
                        tuple.getT6().stream().filter(TenantCountryViewDto::homeCountry).map(TenantCountryViewDto::countryCode).findFirst().orElse(null),
                        tuple.getT7(),
                        tuple.getT8()));
    }

    @Override
    public Mono<PagedResult<AuditLogViewDto>> listAuditLogs(String tenantCode, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 200);
        return repository.listAuditLogs(tenantCode, safePage, safeSize)
                .collectList()
                .zipWith(repository.countAuditLogs(tenantCode))
                .map(tuple -> new PagedResult<>(
                        tuple.getT1(),
                        safePage,
                        safeSize,
                        tuple.getT2(),
                        tuple.getT2() == 0 ? 0 : (int) Math.ceil((double) tuple.getT2() / safeSize)));
    }

    private Mono<Void> validateTenantRequest(TenantUpsertRequest request) {
        if (!StringUtils.hasText(request.tenantCode())
                || !StringUtils.hasText(request.tenantName())
                || !StringUtils.hasText(request.legalName())
                || !StringUtils.hasText(request.defaultTimezone())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED_FIELDS_MISSING", "Tenant code, tenant name, legal name and timezone are required"));
        }
        Mono<Boolean> languageExists = StringUtils.hasText(request.defaultLanguageCode())
                ? repository.languageCodeExists(request.defaultLanguageCode().trim())
                : Mono.just(true);
        Mono<Boolean> countryExists = StringUtils.hasText(request.homeCountryCode())
                ? repository.countryCodeExists(request.homeCountryCode().trim())
                : Mono.just(true);
        return Mono.zip(languageExists, countryExists)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DEFAULT_LANGUAGE", "Default language is invalid"));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_HOME_COUNTRY", "Home country is invalid"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateSubscriptionPlanRequest(SubscriptionPlanUpsertRequest request) {
        if (!StringUtils.hasText(request.planCode()) || !StringUtils.hasText(request.planName())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_PLAN_CODE_NAME_REQUIRED", "Plan code and name are required"));
        }
        if (StringUtils.hasText(request.currencyCode())) {
            return repository.currencyCodeExists(request.currencyCode().trim())
                    .flatMap(exists -> exists
                            ? Mono.empty()
                            : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY_CODE", "Currency code is invalid")));
        }
        return Mono.empty();
    }

    private Mono<Void> validateTenantSubscription(TenantSubscriptionUpsertRequest request) {
        if (request.subscriptionEndDate() != null && request.subscriptionEndDate().isBefore(request.subscriptionStartDate())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_SUBSCRIPTION_WINDOW", "Subscription end date cannot be before start date"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateBranding(TenantBrandingUpsertRequest request) {
        if (!StringUtils.hasText(request.brandName())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "BRAND_NAME_REQUIRED", "Brand name is required"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateLocalization(String countryCode, TenantLocalizationUpsertRequest request) {
        if (!StringUtils.hasText(countryCode)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_CODE_REQUIRED", "Country code is required"));
        }
        Mono<Boolean> countryExists = repository.countryCodeExists(countryCode.trim());
        Mono<Boolean> languageExists = repository.languageCodeExists(request.defaultLanguageCode().trim());
        Mono<Boolean> currencyExists = repository.currencyCodeExists(request.currencyCode().trim());
        return Mono.zip(countryExists, languageExists, currencyExists)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_COUNTRY_CODE", "Country code is invalid"));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_LANGUAGE_CODE", "Default language code is invalid"));
                    }
                    if (!tuple.getT3()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY_CODE", "Currency code is invalid"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateTenantLanguageRequest(TenantLanguageUpsertRequest request, String languageCode) {
        if (!StringUtils.hasText(languageCode)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LANGUAGE_CODE_REQUIRED", "Language code is required"));
        }
        return repository.languageCodeExists(languageCode)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_LANGUAGE_CODE", "Language code is invalid")));
    }

    private Mono<Void> validateTenantCountryRequest(TenantCountryUpsertRequest request, String countryCode) {
        if (!StringUtils.hasText(countryCode)) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_CODE_REQUIRED", "Country code is required"));
        }
        Mono<Boolean> countryExists = repository.countryCodeExists(countryCode);
        Mono<Boolean> currencyExists = StringUtils.hasText(request.defaultCurrencyCode())
                ? repository.currencyCodeExists(request.defaultCurrencyCode().trim())
                : Mono.just(true);
        return Mono.zip(countryExists, currencyExists)
                .flatMap(tuple -> {
                    if (!tuple.getT1()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_COUNTRY_CODE", "Country code is invalid"));
                    }
                    if (!tuple.getT2()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_CURRENCY_CODE", "Currency code is invalid"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateFeatureFlagRequest(FeatureFlagUpsertRequest request) {
        if (!StringUtils.hasText(request.featureKey()) || !StringUtils.hasText(request.featureName())) {
            return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "FEATURE_FLAG_REQUIRED_FIELDS_MISSING", "Feature key and feature name are required"));
        }
        return Mono.empty();
    }

    private TenantSearchQuery normalizeQuery(TenantSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 200);
        return new TenantSearchQuery(query.q(), query.active(), safePage, safeSize, query.sort(), query.all());
    }

    private Mono<Void> publishAudit(String tenantId, String action, String targetType, String targetId, Map<String, Object> metadata) {
        return auditEventPublisher.publish(AuditEvent.of(
                ACTOR,
                tenantId,
                action,
                targetType,
                targetId,
                metadata));
    }
}
