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
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SaasMasterR2dbcRepository implements SaasMasterRepository {

    private static final Set<String> TENANT_SORT_COLUMNS = Set.of("tenant_code", "tenant_name", "updated_at", "created_at");
    private static final Set<String> PLAN_SORT_COLUMNS = Set.of("plan_code", "plan_name", "updated_at", "created_at");
    private static final Set<String> FEATURE_SORT_COLUMNS = Set.of("feature_key", "feature_name", "updated_at", "created_at");

    private final DatabaseClient databaseClient;

    public SaasMasterR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> tenantCodeExists(String tenantCode) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM tenant.tenants WHERE lower(tenant_code) = lower(:tenantCode)")
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<TenantViewDto> createTenant(TenantUpsertRequest request, String actor) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec insertSettingsSpec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_settings(
                            tenant_code,
                            legal_name,
                            contact_email,
                            contact_phone,
                            default_timezone,
                            go_live_date,
                            default_language_code,
                            home_country_code,
                            created_by,
                            updated_by
                        ) VALUES (
                            :tenantCode,
                            :legalName,
                            :contactEmail,
                            :contactPhone,
                            :defaultTimezone,
                            :goLiveDate,
                            :defaultLanguageCode,
                            :homeCountryCode,
                            :createdBy,
                            :updatedBy
                        )
                        """)
                .bind("tenantCode", request.tenantCode().trim())
                .bind("legalName", request.legalName().trim())
                .bind("defaultTimezone", request.defaultTimezone().trim())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        insertSettingsSpec = bindNullable(insertSettingsSpec, "contactEmail", trimOrNull(request.contactEmail()), String.class);
        insertSettingsSpec = bindNullable(insertSettingsSpec, "contactPhone", trimOrNull(request.contactPhone()), String.class);
        insertSettingsSpec = bindNullable(insertSettingsSpec, "goLiveDate", request.goLiveDate(), java.time.LocalDate.class);
        insertSettingsSpec = bindNullable(insertSettingsSpec, "defaultLanguageCode", trimOrNull(request.defaultLanguageCode()), String.class);
        insertSettingsSpec = bindNullable(insertSettingsSpec, "homeCountryCode", trimOrNull(request.homeCountryCode()), String.class);

        return databaseClient.sql("""
                        INSERT INTO tenant.tenants(id, tenant_code, tenant_name, is_active, created_at, updated_at)
                        VALUES (:id, :tenantCode, :tenantName, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """)
                .bind("id", id)
                .bind("tenantCode", request.tenantCode().trim())
                .bind("tenantName", request.tenantName().trim())
                .fetch()
                .rowsUpdated()
                .then(insertSettingsSpec.fetch().rowsUpdated())
                .then(findTenantByCode(request.tenantCode().trim()));
    }

    @Override
    public Mono<TenantViewDto> updateTenant(String tenantCode, TenantUpsertRequest request, String actor) {
        GenericExecuteSpec updateSettingsSpec = databaseClient.sql("""
                        UPDATE master_data.tenant_settings
                        SET legal_name = :legalName,
                            contact_email = :contactEmail,
                            contact_phone = :contactPhone,
                            default_timezone = :defaultTimezone,
                            go_live_date = :goLiveDate,
                            default_language_code = :defaultLanguageCode,
                            home_country_code = :homeCountryCode,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE tenant_code = :tenantCode
                        """)
                .bind("tenantCode", tenantCode)
                .bind("legalName", request.legalName().trim())
                .bind("defaultTimezone", request.defaultTimezone().trim())
                .bind("updatedBy", actor);
        updateSettingsSpec = bindNullable(updateSettingsSpec, "contactEmail", trimOrNull(request.contactEmail()), String.class);
        updateSettingsSpec = bindNullable(updateSettingsSpec, "contactPhone", trimOrNull(request.contactPhone()), String.class);
        updateSettingsSpec = bindNullable(updateSettingsSpec, "goLiveDate", request.goLiveDate(), java.time.LocalDate.class);
        updateSettingsSpec = bindNullable(updateSettingsSpec, "defaultLanguageCode", trimOrNull(request.defaultLanguageCode()), String.class);
        updateSettingsSpec = bindNullable(updateSettingsSpec, "homeCountryCode", trimOrNull(request.homeCountryCode()), String.class);

        return databaseClient.sql("""
                        UPDATE tenant.tenants
                        SET tenant_name = :tenantName,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE tenant_code = :tenantCode
                        """)
                .bind("tenantCode", tenantCode)
                .bind("tenantName", request.tenantName().trim())
                .fetch()
                .rowsUpdated()
                .then(updateSettingsSpec.fetch().rowsUpdated())
                .then(findTenantByCode(tenantCode));
    }

    @Override
    public Mono<TenantViewDto> findTenantByCode(String tenantCode) {
        return databaseClient.sql(tenantBaseSelect() + " WHERE t.tenant_code = :tenantCode")
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> mapTenant(row))
                .one();
    }

    @Override
    public Flux<TenantViewDto> listTenants(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder(tenantBaseSelect()).append(" WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(t.tenant_code) LIKE lower(:q) OR lower(t.tenant_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.is_active = :active");
        }
        sql.append(" ORDER BY ").append(resolveTenantSort(query.sort()));
        if (!query.all()) {
            sql.append(" LIMIT :limit OFFSET :offset");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (!query.all()) {
            spec = spec
                    .bind("limit", query.size())
                    .bind("offset", query.page() * query.size());
        }
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        return spec.map((row, metadata) -> mapTenant(row)).all();
    }

    @Override
    public Mono<Long> countTenants(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM tenant.tenants t WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(t.tenant_code) LIKE lower(:q) OR lower(t.tenant_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.is_active = :active");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<Void> updateTenantStatus(String tenantCode, boolean active) {
        return databaseClient.sql("UPDATE tenant.tenants SET is_active = :active, updated_at = CURRENT_TIMESTAMP WHERE tenant_code = :tenantCode")
                .bind("tenantCode", tenantCode)
                .bind("active", active)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<TenantLanguageViewDto> upsertTenantLanguage(String tenantCode, String languageCode, TenantLanguageUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_languages(
                            id,
                            tenant_code,
                            language_code,
                            is_default,
                            display_order,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :id,
                            :tenantCode,
                            :languageCode,
                            :isDefault,
                            :displayOrder,
                            :active,
                            :createdBy,
                            :updatedBy
                        )
                        ON CONFLICT (tenant_code, language_code)
                        DO UPDATE SET
                            is_default = EXCLUDED.is_default,
                            display_order = EXCLUDED.display_order,
                            active = EXCLUDED.active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = EXCLUDED.updated_by
                        """)
                .bind("id", UUID.randomUUID())
                .bind("tenantCode", tenantCode)
                .bind("languageCode", languageCode)
                .bind("isDefault", request.defaultLanguage() != null && request.defaultLanguage())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "displayOrder", request.displayOrder(), Integer.class);

        Mono<Void> syncDefault = request.defaultLanguage() != null && request.defaultLanguage()
                ? databaseClient.sql("""
                                UPDATE master_data.tenant_languages
                                SET is_default = FALSE,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                  AND lower(language_code) <> lower(:languageCode)
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("languageCode", languageCode)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated()
                        .then()
                : Mono.empty();

        return syncDefault
                .then(spec.fetch().rowsUpdated())
                .then(findTenantLanguage(tenantCode, languageCode));
    }

    @Override
    public Flux<TenantLanguageViewDto> listTenantLanguages(String tenantCode) {
        return databaseClient.sql("""
                        SELECT tl.id,
                               tl.tenant_code,
                               tl.language_code,
                               l.language_name,
                               tl.is_default,
                               tl.active,
                               tl.display_order,
                               tl.created_at,
                               tl.updated_at,
                               tl.created_by,
                               tl.updated_by
                        FROM master_data.tenant_languages tl
                        LEFT JOIN master_data.languages l ON lower(l.language_code) = lower(tl.language_code)
                        WHERE tl.tenant_code = :tenantCode
                        ORDER BY COALESCE(tl.display_order, 99999), tl.language_code ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> mapTenantLanguage(row))
                .all();
    }

    @Override
    public Mono<TenantLanguageViewDto> updateTenantLanguageStatus(String tenantCode, String languageCode, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE master_data.tenant_languages
                        SET active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE tenant_code = :tenantCode
                          AND lower(language_code) = lower(:languageCode)
                        """)
                .bind("tenantCode", tenantCode)
                .bind("languageCode", languageCode)
                .bind("active", active)
                .bind("updatedBy", actor)
                .fetch()
                .rowsUpdated()
                .then(findTenantLanguage(tenantCode, languageCode));
    }

    @Override
    public Mono<TenantLanguageViewDto> updateTenantLanguageDefault(String tenantCode, String languageCode, boolean defaultLanguage, String actor) {
        Mono<Void> unsetOthers = defaultLanguage
                ? databaseClient.sql("""
                                UPDATE master_data.tenant_languages
                                SET is_default = FALSE,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated()
                        .then()
                : Mono.empty();

        return unsetOthers
                .then(databaseClient.sql("""
                                UPDATE master_data.tenant_languages
                                SET is_default = :defaultLanguage,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                  AND lower(language_code) = lower(:languageCode)
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("languageCode", languageCode)
                        .bind("defaultLanguage", defaultLanguage)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated())
                .then(findTenantLanguage(tenantCode, languageCode));
    }

    @Override
    public Mono<Long> countDefaultLanguages(String tenantCode, Boolean activeOnly) {
        String sql = "SELECT COUNT(*) AS cnt FROM master_data.tenant_languages WHERE tenant_code = :tenantCode AND is_default = TRUE"
                + (activeOnly == null ? "" : " AND active = :active");
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantCode", tenantCode);
        if (activeOnly != null) {
            spec = spec.bind("active", activeOnly);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<TenantCountryViewDto> upsertTenantCountry(String tenantCode, String countryCode, TenantCountryUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_countries(
                            id,
                            tenant_code,
                            country_code,
                            default_currency_code,
                            default_timezone,
                            is_home_country,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :id,
                            :tenantCode,
                            :countryCode,
                            :defaultCurrencyCode,
                            :defaultTimezone,
                            :isHomeCountry,
                            :active,
                            :createdBy,
                            :updatedBy
                        )
                        ON CONFLICT (tenant_code, country_code)
                        DO UPDATE SET
                            default_currency_code = EXCLUDED.default_currency_code,
                            default_timezone = EXCLUDED.default_timezone,
                            is_home_country = EXCLUDED.is_home_country,
                            active = EXCLUDED.active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = EXCLUDED.updated_by
                        """)
                .bind("id", UUID.randomUUID())
                .bind("tenantCode", tenantCode)
                .bind("countryCode", countryCode)
                .bind("isHomeCountry", request.homeCountry() != null && request.homeCountry())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "defaultCurrencyCode", trimOrNull(request.defaultCurrencyCode()), String.class);
        spec = bindNullable(spec, "defaultTimezone", trimOrNull(request.defaultTimezone()), String.class);

        Mono<Void> syncHome = request.homeCountry() != null && request.homeCountry()
                ? databaseClient.sql("""
                                UPDATE master_data.tenant_countries
                                SET is_home_country = FALSE,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                  AND lower(country_code) <> lower(:countryCode)
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("countryCode", countryCode)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated()
                        .then()
                : Mono.empty();

        return syncHome
                .then(spec.fetch().rowsUpdated())
                .then(findTenantCountry(tenantCode, countryCode));
    }

    @Override
    public Flux<TenantCountryViewDto> listTenantCountries(String tenantCode) {
        return databaseClient.sql("""
                        SELECT tc.id,
                               tc.tenant_code,
                               tc.country_code,
                               c.country_name,
                               tc.default_currency_code,
                               tc.default_timezone,
                               tc.is_home_country,
                               tc.active,
                               tc.created_at,
                               tc.updated_at,
                               tc.created_by,
                               tc.updated_by
                        FROM master_data.tenant_countries tc
                        LEFT JOIN master_data.countries c ON lower(c.country_code) = lower(tc.country_code)
                        WHERE tc.tenant_code = :tenantCode
                        ORDER BY tc.country_code ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> mapTenantCountry(row))
                .all();
    }

    @Override
    public Mono<TenantCountryViewDto> updateTenantCountryStatus(String tenantCode, String countryCode, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE master_data.tenant_countries
                        SET active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE tenant_code = :tenantCode
                          AND lower(country_code) = lower(:countryCode)
                        """)
                .bind("tenantCode", tenantCode)
                .bind("countryCode", countryCode)
                .bind("active", active)
                .bind("updatedBy", actor)
                .fetch()
                .rowsUpdated()
                .then(findTenantCountry(tenantCode, countryCode));
    }

    @Override
    public Mono<TenantCountryViewDto> updateTenantCountryHome(String tenantCode, String countryCode, boolean homeCountry, String actor) {
        Mono<Void> unsetOthers = homeCountry
                ? databaseClient.sql("""
                                UPDATE master_data.tenant_countries
                                SET is_home_country = FALSE,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated()
                        .then()
                : Mono.empty();

        return unsetOthers
                .then(databaseClient.sql("""
                                UPDATE master_data.tenant_countries
                                SET is_home_country = :homeCountry,
                                    updated_at = CURRENT_TIMESTAMP,
                                    updated_by = :updatedBy
                                WHERE tenant_code = :tenantCode
                                  AND lower(country_code) = lower(:countryCode)
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("countryCode", countryCode)
                        .bind("homeCountry", homeCountry)
                        .bind("updatedBy", actor)
                        .fetch()
                        .rowsUpdated())
                .then(findTenantCountry(tenantCode, countryCode));
    }

    @Override
    public Mono<Long> countHomeCountries(String tenantCode, Boolean activeOnly) {
        String sql = "SELECT COUNT(*) AS cnt FROM master_data.tenant_countries WHERE tenant_code = :tenantCode AND is_home_country = TRUE"
                + (activeOnly == null ? "" : " AND active = :active");
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantCode", tenantCode);
        if (activeOnly != null) {
            spec = spec.bind("active", activeOnly);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<Boolean> subscriptionPlanCodeExists(String planCode, UUID excludeId) {
        String sql = "SELECT COUNT(*) AS cnt FROM master_data.subscription_plans WHERE lower(plan_code) = lower(:planCode)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("planCode", planCode);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<SubscriptionPlanViewDto> createSubscriptionPlan(SubscriptionPlanUpsertRequest request, String actor) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.subscription_plans(
                            id,
                            plan_code,
                            plan_name,
                            description,
                            max_users,
                            max_storage_gb,
                            monthly_price,
                            annual_price,
                            currency_code,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :id,
                            :planCode,
                            :planName,
                            :description,
                            :maxUsers,
                            :maxStorageGb,
                            :monthlyPrice,
                            :annualPrice,
                            :currencyCode,
                            :active,
                            :createdBy,
                            :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", id)
                .bind("planCode", request.planCode().trim())
                .bind("planName", request.planName().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "description", trimOrNull(request.description()), String.class);
        spec = bindNullable(spec, "maxUsers", request.maxUsers(), Integer.class);
        spec = bindNullable(spec, "maxStorageGb", request.maxStorageGb(), Integer.class);
        spec = bindNullable(spec, "monthlyPrice", request.monthlyPrice(), java.math.BigDecimal.class);
        spec = bindNullable(spec, "annualPrice", request.annualPrice(), java.math.BigDecimal.class);
        spec = bindNullable(spec, "currencyCode", trimOrNull(request.currencyCode()), String.class);
        return spec.map((row, metadata) -> mapPlan(row)).one();
    }

    @Override
    public Mono<SubscriptionPlanViewDto> updateSubscriptionPlan(UUID id, SubscriptionPlanUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE master_data.subscription_plans
                        SET plan_code = :planCode,
                            plan_name = :planName,
                            description = :description,
                            max_users = :maxUsers,
                            max_storage_gb = :maxStorageGb,
                            monthly_price = :monthlyPrice,
                            annual_price = :annualPrice,
                            currency_code = :currencyCode,
                            active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind("id", id)
                .bind("planCode", request.planCode().trim())
                .bind("planName", request.planName().trim())
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "description", trimOrNull(request.description()), String.class);
        spec = bindNullable(spec, "maxUsers", request.maxUsers(), Integer.class);
        spec = bindNullable(spec, "maxStorageGb", request.maxStorageGb(), Integer.class);
        spec = bindNullable(spec, "monthlyPrice", request.monthlyPrice(), java.math.BigDecimal.class);
        spec = bindNullable(spec, "annualPrice", request.annualPrice(), java.math.BigDecimal.class);
        spec = bindNullable(spec, "currencyCode", trimOrNull(request.currencyCode()), String.class);
        return spec.map((row, metadata) -> mapPlan(row)).one();
    }

    @Override
    public Mono<SubscriptionPlanViewDto> findSubscriptionPlanById(UUID id) {
        return databaseClient.sql("SELECT * FROM master_data.subscription_plans WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> mapPlan(row))
                .one();
    }

    @Override
    public Flux<SubscriptionPlanViewDto> listSubscriptionPlans(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT * FROM master_data.subscription_plans WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(plan_code) LIKE lower(:q) OR lower(plan_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND active = :active");
        }
        sql.append(" ORDER BY ").append(resolveSort(query.sort(), PLAN_SORT_COLUMNS, "updated_at DESC"));
        if (!query.all()) {
            sql.append(" LIMIT :limit OFFSET :offset");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (!query.all()) {
            spec = spec
                    .bind("limit", query.size())
                    .bind("offset", query.page() * query.size());
        }
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        return spec.map((row, metadata) -> mapPlan(row)).all();
    }

    @Override
    public Mono<Long> countSubscriptionPlans(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM master_data.subscription_plans WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(plan_code) LIKE lower(:q) OR lower(plan_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND active = :active");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<Void> updateSubscriptionPlanStatus(UUID id, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE master_data.subscription_plans
                        SET active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE id = :id
                        """)
                .bind("id", id)
                .bind("active", active)
                .bind("updatedBy", actor)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Flux<SubscriptionPlanViewDto> subscriptionPlanOptions(boolean activeOnly) {
        String sql = "SELECT * FROM master_data.subscription_plans"
                + (activeOnly ? " WHERE active = TRUE" : "")
                + " ORDER BY plan_name ASC";
        return databaseClient.sql(sql)
                .map((row, metadata) -> mapPlan(row))
                .all();
    }

    @Override
    public Mono<Boolean> featureFlagCodeExists(String featureKey, UUID excludeId) {
        String sql = "SELECT COUNT(*) AS cnt FROM master_data.feature_flags WHERE lower(feature_key) = lower(:featureKey)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("featureKey", featureKey);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<FeatureFlagViewDto> createFeatureFlag(FeatureFlagUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.feature_flags(
                            id,
                            feature_key,
                            feature_name,
                            description,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :id,
                            :featureKey,
                            :featureName,
                            :description,
                            :active,
                            :createdBy,
                            :updatedBy
                        ) RETURNING *
                        """)
                .bind("id", UUID.randomUUID())
                .bind("featureKey", request.featureKey().trim())
                .bind("featureName", request.featureName().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "description", trimOrNull(request.description()), String.class);
        return spec.map((row, metadata) -> mapFeatureFlag(row)).one();
    }

    @Override
    public Mono<FeatureFlagViewDto> updateFeatureFlag(UUID id, FeatureFlagUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE master_data.feature_flags
                        SET feature_key = :featureKey,
                            feature_name = :featureName,
                            description = :description,
                            active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind("id", id)
                .bind("featureKey", request.featureKey().trim())
                .bind("featureName", request.featureName().trim())
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "description", trimOrNull(request.description()), String.class);
        return spec.map((row, metadata) -> mapFeatureFlag(row)).one();
    }

    @Override
    public Mono<FeatureFlagViewDto> findFeatureFlagById(UUID id) {
        return databaseClient.sql("SELECT * FROM master_data.feature_flags WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> mapFeatureFlag(row))
                .one();
    }

    @Override
    public Flux<FeatureFlagViewDto> listFeatureFlags(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT * FROM master_data.feature_flags WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(feature_key) LIKE lower(:q) OR lower(feature_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND active = :active");
        }
        sql.append(" ORDER BY ").append(resolveSort(query.sort(), FEATURE_SORT_COLUMNS, "updated_at DESC"));
        if (!query.all()) {
            sql.append(" LIMIT :limit OFFSET :offset");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        if (!query.all()) {
            spec = spec.bind("limit", query.size())
                    .bind("offset", query.page() * query.size());
        }
        return spec.map((row, metadata) -> mapFeatureFlag(row)).all();
    }

    @Override
    public Mono<Long> countFeatureFlags(TenantSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM master_data.feature_flags WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(feature_key) LIKE lower(:q) OR lower(feature_name) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND active = :active");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<Void> updateFeatureFlagStatus(UUID id, boolean active, String actor) {
        return databaseClient.sql("""
                        UPDATE master_data.feature_flags
                        SET active = :active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = :updatedBy
                        WHERE id = :id
                        """)
                .bind("id", id)
                .bind("active", active)
                .bind("updatedBy", actor)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Flux<FeatureFlagOptionViewDto> featureFlagOptions(boolean activeOnly) {
        String sql = "SELECT id, feature_key, feature_name FROM master_data.feature_flags"
                + (activeOnly ? " WHERE active = TRUE" : "")
                + " ORDER BY feature_name ASC";
        return databaseClient.sql(sql)
                .map((row, metadata) -> new FeatureFlagOptionViewDto(
                        row.get("id", UUID.class),
                        row.get("feature_key", String.class),
                        row.get("feature_name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> featureFlagExists(String featureKey) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM master_data.feature_flags WHERE lower(feature_key) = lower(:featureKey) AND active = TRUE")
                .bind("featureKey", featureKey)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> subscriptionPlanExists(UUID id) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM master_data.subscription_plans WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<TenantSubscriptionViewDto> upsertTenantSubscription(String tenantCode, TenantSubscriptionUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_subscriptions(
                            tenant_code,
                            subscription_plan_id,
                            subscription_start_date,
                            subscription_end_date,
                            auto_renew,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :tenantCode,
                            :subscriptionPlanId,
                            :subscriptionStartDate,
                            :subscriptionEndDate,
                            :autoRenew,
                            :active,
                            :createdBy,
                            :updatedBy
                        )
                        ON CONFLICT (tenant_code)
                        DO UPDATE SET
                            subscription_plan_id = EXCLUDED.subscription_plan_id,
                            subscription_start_date = EXCLUDED.subscription_start_date,
                            subscription_end_date = EXCLUDED.subscription_end_date,
                            auto_renew = EXCLUDED.auto_renew,
                            active = EXCLUDED.active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = EXCLUDED.updated_by
                        """)
                .bind("tenantCode", tenantCode)
                .bind("subscriptionPlanId", request.subscriptionPlanId())
                .bind("subscriptionStartDate", request.subscriptionStartDate())
                .bind("autoRenew", request.autoRenew() != null && request.autoRenew())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "subscriptionEndDate", request.subscriptionEndDate(), java.time.LocalDate.class);
        return spec.fetch().rowsUpdated().then(findTenantSubscription(tenantCode));
    }

    @Override
    public Mono<TenantSubscriptionViewDto> findTenantSubscription(String tenantCode) {
        return databaseClient.sql("""
                        SELECT ts.tenant_code,
                               ts.subscription_plan_id,
                               sp.plan_code,
                               sp.plan_name,
                               ts.subscription_start_date,
                               ts.subscription_end_date,
                               ts.auto_renew,
                               ts.active,
                               ts.created_at,
                               ts.updated_at,
                               ts.created_by,
                               ts.updated_by
                        FROM master_data.tenant_subscriptions ts
                        JOIN master_data.subscription_plans sp ON sp.id = ts.subscription_plan_id
                        WHERE ts.tenant_code = :tenantCode
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new TenantSubscriptionViewDto(
                        row.get("tenant_code", String.class),
                        row.get("subscription_plan_id", UUID.class),
                        row.get("plan_code", String.class),
                        row.get("plan_name", String.class),
                        row.get("subscription_start_date", java.time.LocalDate.class),
                        row.get("subscription_end_date", java.time.LocalDate.class),
                        Boolean.TRUE.equals(row.get("auto_renew", Boolean.class)),
                        Boolean.TRUE.equals(row.get("active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_by", String.class)))
                .one();
    }

    @Override
    public Mono<TenantBrandingViewDto> upsertBranding(String tenantCode, TenantBrandingUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_branding(
                            tenant_code,
                            brand_name,
                            logo_url,
                            favicon_url,
                            primary_color,
                            secondary_color,
                            login_banner_url,
                            email_logo_url,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :tenantCode,
                            :brandName,
                            :logoUrl,
                            :faviconUrl,
                            :primaryColor,
                            :secondaryColor,
                            :loginBannerUrl,
                            :emailLogoUrl,
                            :active,
                            :createdBy,
                            :updatedBy
                        )
                        ON CONFLICT (tenant_code)
                        DO UPDATE SET
                            brand_name = EXCLUDED.brand_name,
                            logo_url = EXCLUDED.logo_url,
                            favicon_url = EXCLUDED.favicon_url,
                            primary_color = EXCLUDED.primary_color,
                            secondary_color = EXCLUDED.secondary_color,
                            login_banner_url = EXCLUDED.login_banner_url,
                            email_logo_url = EXCLUDED.email_logo_url,
                            active = EXCLUDED.active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = EXCLUDED.updated_by
                        """)
                .bind("tenantCode", tenantCode)
                .bind("brandName", request.brandName().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "logoUrl", trimOrNull(request.logoUrl()), String.class);
        spec = bindNullable(spec, "faviconUrl", trimOrNull(request.faviconUrl()), String.class);
        spec = bindNullable(spec, "primaryColor", trimOrNull(request.primaryColor()), String.class);
        spec = bindNullable(spec, "secondaryColor", trimOrNull(request.secondaryColor()), String.class);
        spec = bindNullable(spec, "loginBannerUrl", trimOrNull(request.loginBannerUrl()), String.class);
        spec = bindNullable(spec, "emailLogoUrl", trimOrNull(request.emailLogoUrl()), String.class);
        return spec.fetch().rowsUpdated().then(findBranding(tenantCode));
    }

    @Override
    public Mono<TenantBrandingViewDto> findBranding(String tenantCode) {
        return databaseClient.sql("SELECT * FROM master_data.tenant_branding WHERE tenant_code = :tenantCode")
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new TenantBrandingViewDto(
                        row.get("tenant_code", String.class),
                        row.get("brand_name", String.class),
                        row.get("logo_url", String.class),
                        row.get("favicon_url", String.class),
                        row.get("primary_color", String.class),
                        row.get("secondary_color", String.class),
                        row.get("login_banner_url", String.class),
                        row.get("email_logo_url", String.class),
                        Boolean.TRUE.equals(row.get("active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_by", String.class)))
                .one();
    }

    @Override
    public Mono<TenantLocalizationViewDto> upsertLocalization(String tenantCode, String countryCode, TenantLocalizationUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO master_data.tenant_localization_preferences(
                            id,
                            tenant_code,
                            country_code,
                            default_language_code,
                            date_format,
                            time_format,
                            week_start_day,
                            currency_code,
                            number_format,
                            rtl_enabled,
                            public_holiday_calendar_code,
                            calendar_type,
                            active,
                            created_by,
                            updated_by
                        ) VALUES (
                            :id,
                            :tenantCode,
                            :countryCode,
                            :defaultLanguageCode,
                            :dateFormat,
                            :timeFormat,
                            :weekStartDay,
                            :currencyCode,
                            :numberFormat,
                            :rtlEnabled,
                            :publicHolidayCalendarCode,
                            :calendarType,
                            :active,
                            :createdBy,
                            :updatedBy
                        )
                        ON CONFLICT (tenant_code, country_code)
                        DO UPDATE SET
                            default_language_code = EXCLUDED.default_language_code,
                            date_format = EXCLUDED.date_format,
                            time_format = EXCLUDED.time_format,
                            week_start_day = EXCLUDED.week_start_day,
                            currency_code = EXCLUDED.currency_code,
                            number_format = EXCLUDED.number_format,
                            rtl_enabled = EXCLUDED.rtl_enabled,
                            public_holiday_calendar_code = EXCLUDED.public_holiday_calendar_code,
                            calendar_type = EXCLUDED.calendar_type,
                            active = EXCLUDED.active,
                            updated_at = CURRENT_TIMESTAMP,
                            updated_by = EXCLUDED.updated_by
                        """)
                .bind("id", UUID.randomUUID())
                .bind("tenantCode", tenantCode)
                .bind("countryCode", countryCode)
                .bind("defaultLanguageCode", request.defaultLanguageCode().trim())
                .bind("dateFormat", request.dateFormat().trim())
                .bind("timeFormat", request.timeFormat().trim())
                .bind("weekStartDay", request.weekStartDay().trim())
                .bind("currencyCode", request.currencyCode().trim())
                .bind("numberFormat", request.numberFormat().trim())
                .bind("rtlEnabled", request.rtlEnabled() != null && request.rtlEnabled())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindNullable(spec, "publicHolidayCalendarCode", trimOrNull(request.publicHolidayCalendarCode()), String.class);
        spec = bindNullable(spec, "calendarType", trimOrNull(request.calendarType()), String.class);
        return spec.fetch().rowsUpdated().then(listLocalization(tenantCode)
                .filter(item -> countryCode.equalsIgnoreCase(item.countryCode()))
                .next());
    }

    @Override
    public Flux<TenantLocalizationViewDto> listLocalization(String tenantCode) {
        return databaseClient.sql("""
                        SELECT *
                        FROM master_data.tenant_localization_preferences
                        WHERE tenant_code = :tenantCode
                        ORDER BY country_code ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new TenantLocalizationViewDto(
                        String.valueOf(row.get("id", UUID.class)),
                        row.get("tenant_code", String.class),
                        row.get("country_code", String.class),
                        row.get("default_language_code", String.class),
                        row.get("date_format", String.class),
                        row.get("time_format", String.class),
                        row.get("week_start_day", String.class),
                        row.get("currency_code", String.class),
                        row.get("number_format", String.class),
                        Boolean.TRUE.equals(row.get("rtl_enabled", Boolean.class)),
                        row.get("public_holiday_calendar_code", String.class),
                        row.get("calendar_type", String.class),
                        Boolean.TRUE.equals(row.get("active", Boolean.class)),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_by", String.class)))
                .all();
    }

    @Override
    public Mono<Void> setModuleEnabled(String tenantCode, String moduleKey, boolean enabled) {
        return databaseClient.sql("""
                        INSERT INTO tenant.module_catalog(module_key, module_name, is_active)
                        VALUES (:moduleKey, :moduleName, TRUE)
                        ON CONFLICT (module_key) DO NOTHING
                        """)
                .bind("moduleKey", moduleKey)
                .bind("moduleName", moduleKey)
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("""
                                INSERT INTO tenant.tenant_module_subscriptions(tenant_code, module_key, is_enabled)
                                VALUES (:tenantCode, :moduleKey, :enabled)
                                ON CONFLICT (tenant_code, module_key)
                                DO UPDATE SET is_enabled = EXCLUDED.is_enabled
                                """)
                        .bind("tenantCode", tenantCode)
                        .bind("moduleKey", moduleKey)
                        .bind("enabled", enabled)
                        .fetch()
                        .rowsUpdated()
                        .then());
    }

    @Override
    public Mono<Void> setFeatureEnabled(String tenantCode, String featureKey, boolean enabled) {
        return databaseClient.sql("""
                        INSERT INTO tenant.tenant_feature_flags(tenant_code, feature_key, is_enabled)
                        VALUES (:tenantCode, :featureKey, :enabled)
                        ON CONFLICT (tenant_code, feature_key)
                        DO UPDATE SET is_enabled = EXCLUDED.is_enabled
                        """)
                .bind("tenantCode", tenantCode)
                .bind("featureKey", featureKey)
                .bind("enabled", enabled)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Flux<ToggleViewDto> listModules(String tenantCode) {
        return databaseClient.sql("""
                        SELECT module_key, is_enabled
                        FROM tenant.tenant_module_subscriptions
                        WHERE tenant_code = :tenantCode
                        ORDER BY module_key ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new ToggleViewDto(
                        row.get("module_key", String.class),
                        Boolean.TRUE.equals(row.get("is_enabled", Boolean.class))))
                .all();
    }

    @Override
    public Flux<ToggleViewDto> listFeatures(String tenantCode) {
        return databaseClient.sql("""
                        SELECT feature_key, is_enabled
                        FROM tenant.tenant_feature_flags
                        WHERE tenant_code = :tenantCode
                        ORDER BY feature_key ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new ToggleViewDto(
                        row.get("feature_key", String.class),
                        Boolean.TRUE.equals(row.get("is_enabled", Boolean.class))))
                .all();
    }

    @Override
    public Flux<AuditLogViewDto> listAuditLogs(String tenantCode, int page, int size) {
        return databaseClient.sql("""
                        SELECT id, actor, tenant_id, action, target_type, target_id, event_timestamp, metadata::text AS metadata
                        FROM audit.audit_events
                        WHERE tenant_id = :tenantCode
                        ORDER BY event_timestamp DESC
                        LIMIT :limit OFFSET :offset
                        """)
                .bind("tenantCode", tenantCode)
                .bind("limit", size)
                .bind("offset", page * size)
                .map((row, metadata) -> new AuditLogViewDto(
                        row.get("id", Long.class) == null ? 0L : row.get("id", Long.class),
                        row.get("actor", String.class),
                        row.get("tenant_id", String.class),
                        row.get("action", String.class),
                        row.get("target_type", String.class),
                        row.get("target_id", String.class),
                        row.get("event_timestamp", Instant.class),
                        row.get("metadata", String.class)))
                .all();
    }

    @Override
    public Mono<Long> countAuditLogs(String tenantCode) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM audit.audit_events WHERE tenant_id = :tenantCode")
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> row.get("cnt", Long.class))
                .one();
    }

    @Override
    public Mono<Boolean> languageCodeExists(String languageCode) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM master_data.languages WHERE lower(language_code) = lower(:code)")
                .bind("code", languageCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> countryCodeExists(String countryCode) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM master_data.countries WHERE lower(country_code) = lower(:code)")
                .bind("code", countryCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> currencyCodeExists(String currencyCode) {
        return databaseClient.sql("SELECT COUNT(*) AS cnt FROM master_data.currencies WHERE lower(currency_code) = lower(:code)")
                .bind("code", currencyCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    private Mono<TenantLanguageViewDto> findTenantLanguage(String tenantCode, String languageCode) {
        return databaseClient.sql("""
                        SELECT tl.id,
                               tl.tenant_code,
                               tl.language_code,
                               l.language_name,
                               tl.is_default,
                               tl.active,
                               tl.display_order,
                               tl.created_at,
                               tl.updated_at,
                               tl.created_by,
                               tl.updated_by
                        FROM master_data.tenant_languages tl
                        LEFT JOIN master_data.languages l ON lower(l.language_code) = lower(tl.language_code)
                        WHERE tl.tenant_code = :tenantCode
                          AND lower(tl.language_code) = lower(:languageCode)
                        """)
                .bind("tenantCode", tenantCode)
                .bind("languageCode", languageCode)
                .map((row, metadata) -> mapTenantLanguage(row))
                .one();
    }

    private Mono<TenantCountryViewDto> findTenantCountry(String tenantCode, String countryCode) {
        return databaseClient.sql("""
                        SELECT tc.id,
                               tc.tenant_code,
                               tc.country_code,
                               c.country_name,
                               tc.default_currency_code,
                               tc.default_timezone,
                               tc.is_home_country,
                               tc.active,
                               tc.created_at,
                               tc.updated_at,
                               tc.created_by,
                               tc.updated_by
                        FROM master_data.tenant_countries tc
                        LEFT JOIN master_data.countries c ON lower(c.country_code) = lower(tc.country_code)
                        WHERE tc.tenant_code = :tenantCode
                          AND lower(tc.country_code) = lower(:countryCode)
                        """)
                .bind("tenantCode", tenantCode)
                .bind("countryCode", countryCode)
                .map((row, metadata) -> mapTenantCountry(row))
                .one();
    }

    private TenantLanguageViewDto mapTenantLanguage(Row row) {
        return new TenantLanguageViewDto(
                row.get("id", UUID.class),
                row.get("tenant_code", String.class),
                row.get("language_code", String.class),
                row.get("language_name", String.class),
                Boolean.TRUE.equals(row.get("is_default", Boolean.class)),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("display_order", Integer.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private TenantCountryViewDto mapTenantCountry(Row row) {
        return new TenantCountryViewDto(
                row.get("id", UUID.class),
                row.get("tenant_code", String.class),
                row.get("country_code", String.class),
                row.get("country_name", String.class),
                row.get("default_currency_code", String.class),
                row.get("default_timezone", String.class),
                Boolean.TRUE.equals(row.get("is_home_country", Boolean.class)),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private FeatureFlagViewDto mapFeatureFlag(Row row) {
        return new FeatureFlagViewDto(
                row.get("id", UUID.class),
                row.get("feature_key", String.class),
                row.get("feature_name", String.class),
                row.get("description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private TenantViewDto mapTenant(Row row) {
        return new TenantViewDto(
                row.get("tenant_code", String.class),
                row.get("tenant_name", String.class),
                row.get("legal_name", String.class),
                row.get("contact_email", String.class),
                row.get("contact_phone", String.class),
                row.get("default_timezone", String.class),
                row.get("go_live_date", java.time.LocalDate.class),
                row.get("default_language_code", String.class),
                row.get("home_country_code", String.class),
                Boolean.TRUE.equals(row.get("is_active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private SubscriptionPlanViewDto mapPlan(Row row) {
        return new SubscriptionPlanViewDto(
                row.get("id", UUID.class),
                row.get("plan_code", String.class),
                row.get("plan_name", String.class),
                row.get("description", String.class),
                row.get("max_users", Integer.class),
                row.get("max_storage_gb", Integer.class),
                row.get("monthly_price", java.math.BigDecimal.class),
                row.get("annual_price", java.math.BigDecimal.class),
                row.get("currency_code", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private String tenantBaseSelect() {
        return """
                SELECT t.tenant_code,
                       t.tenant_name,
                       s.legal_name,
                       s.contact_email,
                       s.contact_phone,
                       s.default_timezone,
                       s.go_live_date,
                       s.default_language_code,
                       s.home_country_code,
                       t.is_active,
                       COALESCE(s.created_at, t.created_at) AS created_at,
                       COALESCE(s.updated_at, t.updated_at) AS updated_at,
                       s.created_by,
                       s.updated_by
                FROM tenant.tenants t
                LEFT JOIN master_data.tenant_settings s ON s.tenant_code = t.tenant_code
                """;
    }

    private String resolveSort(String sort, Set<String> allowList, String fallback) {
        if (!StringUtils.hasText(sort)) {
            return fallback;
        }
        String[] parts = sort.split(",");
        String column = parts[0].trim();
        if (!allowList.contains(column)) {
            return fallback;
        }
        String direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) ? "ASC" : "DESC";
        return column + " " + direction;
    }

    private String resolveTenantSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return "t.updated_at DESC";
        }
        String[] parts = sort.split(",");
        String column = parts[0].trim();
        if (!TENANT_SORT_COLUMNS.contains(column)) {
            return "t.updated_at DESC";
        }
        String direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) ? "ASC" : "DESC";
        return "t." + column + " " + direction;
    }

    private String trimOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
