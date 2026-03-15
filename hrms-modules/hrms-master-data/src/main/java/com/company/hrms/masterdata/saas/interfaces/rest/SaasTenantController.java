package com.company.hrms.masterdata.saas.interfaces.rest;

import com.company.hrms.masterdata.reference.api.PagedResult;
import com.company.hrms.masterdata.saas.api.AuditLogViewDto;
import com.company.hrms.masterdata.saas.api.TenantBrandingUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantBrandingViewDto;
import com.company.hrms.masterdata.saas.api.TenantCountryHomeUpdateRequest;
import com.company.hrms.masterdata.saas.api.TenantCountryStatusUpdateRequest;
import com.company.hrms.masterdata.saas.api.TenantCountryUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantCountryViewDto;
import com.company.hrms.masterdata.saas.api.TenantLanguageDefaultUpdateRequest;
import com.company.hrms.masterdata.saas.api.TenantLanguageStatusUpdateRequest;
import com.company.hrms.masterdata.saas.api.TenantLanguageUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLanguageViewDto;
import com.company.hrms.masterdata.saas.api.TenantLocalizationUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantLocalizationViewDto;
import com.company.hrms.masterdata.saas.api.TenantSearchQuery;
import com.company.hrms.masterdata.saas.api.TenantSettingsViewDto;
import com.company.hrms.masterdata.saas.api.TenantStatusUpdateRequest;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantSubscriptionViewDto;
import com.company.hrms.masterdata.saas.api.TenantUpsertRequest;
import com.company.hrms.masterdata.saas.api.TenantViewDto;
import com.company.hrms.masterdata.saas.api.ToggleUpdateRequest;
import com.company.hrms.masterdata.saas.api.ToggleViewDto;
import com.company.hrms.masterdata.saas.application.SaasMasterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/saas/tenants")
@Tag(name = "SaaS Tenant Masters", description = "Tenant, settings, branding and enablement APIs")
public class SaasTenantController {

    private final SaasMasterService service;

    public SaasTenantController(SaasMasterService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create tenant")
    public Mono<TenantViewDto> create(@Valid @RequestBody TenantUpsertRequest request) {
        return service.createTenant(request);
    }

    @PutMapping("/{tenantCode}")
    @Operation(summary = "Update tenant")
    public Mono<TenantViewDto> update(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantUpsertRequest request
    ) {
        return service.updateTenant(tenantCode, request);
    }

    @GetMapping("/{tenantCode}")
    @Operation(summary = "Get tenant by code")
    public Mono<TenantViewDto> getByCode(@PathVariable("tenantCode") String tenantCode) {
        return service.getTenant(tenantCode);
    }

    @GetMapping
    @Operation(summary = "List tenants")
    public Mono<PagedResult<TenantViewDto>> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all
    ) {
        return service.listTenants(new TenantSearchQuery(q, active, page, size, sort, all));
    }

    @PatchMapping("/{tenantCode}/status")
    @Operation(summary = "Activate or deactivate tenant")
    public Mono<Void> updateStatus(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantStatusUpdateRequest request
    ) {
        return service.updateTenantStatus(tenantCode, request.active());
    }

    @PostMapping("/{tenantCode}/languages")
    @Operation(summary = "Add tenant language")
    public Mono<TenantLanguageViewDto> createTenantLanguage(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantLanguageUpsertRequest request
    ) {
        return service.upsertTenantLanguage(tenantCode, request.languageCode(), request);
    }

    @PutMapping("/{tenantCode}/languages/{languageCode}")
    @Operation(summary = "Update tenant language")
    public Mono<TenantLanguageViewDto> updateTenantLanguage(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("languageCode") String languageCode,
            @Valid @RequestBody TenantLanguageUpsertRequest request
    ) {
        return service.upsertTenantLanguage(tenantCode, languageCode, request);
    }

    @GetMapping("/{tenantCode}/languages")
    @Operation(summary = "List tenant languages")
    public Flux<TenantLanguageViewDto> listTenantLanguages(@PathVariable("tenantCode") String tenantCode) {
        return service.listTenantLanguages(tenantCode);
    }

    @PatchMapping("/{tenantCode}/languages/{languageCode}/status")
    @Operation(summary = "Activate or deactivate tenant language")
    public Mono<TenantLanguageViewDto> updateTenantLanguageStatus(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("languageCode") String languageCode,
            @Valid @RequestBody TenantLanguageStatusUpdateRequest request
    ) {
        return service.updateTenantLanguageStatus(tenantCode, languageCode, request.active());
    }

    @PatchMapping("/{tenantCode}/languages/{languageCode}/default")
    @Operation(summary = "Set or unset tenant default language")
    public Mono<TenantLanguageViewDto> updateTenantLanguageDefault(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("languageCode") String languageCode,
            @Valid @RequestBody TenantLanguageDefaultUpdateRequest request
    ) {
        return service.updateTenantLanguageDefault(tenantCode, languageCode, request.defaultLanguage());
    }

    @PostMapping("/{tenantCode}/countries")
    @Operation(summary = "Add tenant country")
    public Mono<TenantCountryViewDto> createTenantCountry(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantCountryUpsertRequest request
    ) {
        return service.upsertTenantCountry(tenantCode, request.countryCode(), request);
    }

    @PutMapping("/{tenantCode}/countries/{countryCode}")
    @Operation(summary = "Update tenant country")
    public Mono<TenantCountryViewDto> updateTenantCountry(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("countryCode") String countryCode,
            @Valid @RequestBody TenantCountryUpsertRequest request
    ) {
        return service.upsertTenantCountry(tenantCode, countryCode, request);
    }

    @GetMapping("/{tenantCode}/countries")
    @Operation(summary = "List tenant countries")
    public Flux<TenantCountryViewDto> listTenantCountries(@PathVariable("tenantCode") String tenantCode) {
        return service.listTenantCountries(tenantCode);
    }

    @PatchMapping("/{tenantCode}/countries/{countryCode}/status")
    @Operation(summary = "Activate or deactivate tenant country")
    public Mono<TenantCountryViewDto> updateTenantCountryStatus(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("countryCode") String countryCode,
            @Valid @RequestBody TenantCountryStatusUpdateRequest request
    ) {
        return service.updateTenantCountryStatus(tenantCode, countryCode, request.active());
    }

    @PatchMapping("/{tenantCode}/countries/{countryCode}/home")
    @Operation(summary = "Set or unset tenant home country")
    public Mono<TenantCountryViewDto> updateTenantCountryHome(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("countryCode") String countryCode,
            @Valid @RequestBody TenantCountryHomeUpdateRequest request
    ) {
        return service.updateTenantCountryHome(tenantCode, countryCode, request.homeCountry());
    }

    @PutMapping("/{tenantCode}/subscription")
    @Operation(summary = "Assign or update tenant subscription")
    public Mono<TenantSubscriptionViewDto> upsertSubscription(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantSubscriptionUpsertRequest request
    ) {
        return service.upsertTenantSubscription(tenantCode, request);
    }

    @GetMapping("/{tenantCode}/subscription")
    @Operation(summary = "Get tenant subscription")
    public Mono<TenantSubscriptionViewDto> getSubscription(@PathVariable("tenantCode") String tenantCode) {
        return service.getTenantSubscription(tenantCode);
    }

    @PutMapping("/{tenantCode}/branding")
    @Operation(summary = "Upsert tenant branding")
    public Mono<TenantBrandingViewDto> upsertBranding(
            @PathVariable("tenantCode") String tenantCode,
            @Valid @RequestBody TenantBrandingUpsertRequest request
    ) {
        return service.upsertBranding(tenantCode, request);
    }

    @GetMapping("/{tenantCode}/branding")
    @Operation(summary = "Get tenant branding")
    public Mono<TenantBrandingViewDto> getBranding(@PathVariable("tenantCode") String tenantCode) {
        return service.getBranding(tenantCode);
    }

    @PutMapping("/{tenantCode}/localization/{countryCode}")
    @Operation(summary = "Upsert tenant localization by country")
    public Mono<TenantLocalizationViewDto> upsertLocalization(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("countryCode") String countryCode,
            @Valid @RequestBody TenantLocalizationUpsertRequest request
    ) {
        return service.upsertLocalization(tenantCode, countryCode, request);
    }

    @GetMapping("/{tenantCode}/localization")
    @Operation(summary = "List tenant localization preferences")
    public Flux<TenantLocalizationViewDto> listLocalization(@PathVariable("tenantCode") String tenantCode) {
        return service.listLocalization(tenantCode);
    }

    @PutMapping("/{tenantCode}/modules/{moduleKey}")
    @Operation(summary = "Enable or disable module for tenant")
    public Mono<Void> setModuleEnabled(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("moduleKey") String moduleKey,
            @Valid @RequestBody ToggleUpdateRequest request
    ) {
        return service.setModuleEnabled(tenantCode, moduleKey, request.enabled());
    }

    @GetMapping("/{tenantCode}/modules")
    @Operation(summary = "List tenant module enablement")
    public Flux<ToggleViewDto> listModules(@PathVariable("tenantCode") String tenantCode) {
        return service.listModules(tenantCode);
    }

    @PutMapping("/{tenantCode}/features/{featureKey}")
    @Operation(summary = "Enable or disable feature flag for tenant")
    public Mono<Void> setFeatureEnabled(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("featureKey") String featureKey,
            @Valid @RequestBody ToggleUpdateRequest request
    ) {
        return service.setFeatureEnabled(tenantCode, featureKey, request.enabled());
    }

    @GetMapping("/{tenantCode}/features")
    @Operation(summary = "List tenant feature flags")
    public Flux<ToggleViewDto> listFeatures(@PathVariable("tenantCode") String tenantCode) {
        return service.listFeatures(tenantCode);
    }

    @GetMapping("/{tenantCode}/settings")
    @Operation(summary = "Get consolidated tenant settings")
    public Mono<TenantSettingsViewDto> getSettings(@PathVariable("tenantCode") String tenantCode) {
        return service.getTenantSettings(tenantCode);
    }

    @GetMapping("/{tenantCode}/audit-logs")
    @Operation(summary = "List tenant audit logs")
    public Mono<PagedResult<AuditLogViewDto>> listAuditLogs(
            @PathVariable("tenantCode") String tenantCode,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        return service.listAuditLogs(tenantCode, page, size);
    }
}
