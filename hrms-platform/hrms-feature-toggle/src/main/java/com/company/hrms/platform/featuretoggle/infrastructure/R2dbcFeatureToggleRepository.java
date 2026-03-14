package com.company.hrms.platform.featuretoggle.infrastructure;

import com.company.hrms.platform.featuretoggle.api.TenantFeatureFlag;
import com.company.hrms.platform.featuretoggle.api.TenantModuleSubscription;
import com.company.hrms.platform.featuretoggle.service.FeatureToggleRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

public class R2dbcFeatureToggleRepository implements FeatureToggleRepository {

    private final DatabaseClient databaseClient;

    public R2dbcFeatureToggleRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
        return databaseClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM tenant.tenant_module_subscriptions tms
                            JOIN tenant.module_catalog mc ON mc.module_key = tms.module_key
                            WHERE tms.tenant_code = :tenantCode
                              AND tms.module_key = :moduleKey
                              AND tms.is_enabled = TRUE
                              AND mc.is_active = TRUE
                        ) AS enabled
                        """)
                .bind("tenantCode", tenantCode)
                .bind("moduleKey", moduleKey)
                .map((row, metadata) -> Boolean.TRUE.equals(row.get("enabled", Boolean.class)))
                .one()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
        return databaseClient.sql("""
                        SELECT is_enabled
                        FROM tenant.tenant_feature_flags
                        WHERE tenant_code = :tenantCode
                          AND feature_key = :featureKey
                        """)
                .bind("tenantCode", tenantCode)
                .bind("featureKey", featureKey)
                .map((row, metadata) -> Boolean.TRUE.equals(row.get("is_enabled", Boolean.class)))
                .one()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<TenantModuleSubscription> upsertModuleSubscription(String tenantCode, String moduleKey, boolean enabled) {
        return databaseClient.sql("""
                        INSERT INTO tenant.tenant_module_subscriptions(tenant_code, module_key, is_enabled, created_at, updated_at)
                        VALUES (:tenantCode, :moduleKey, :enabled, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        ON CONFLICT (tenant_code, module_key)
                        DO UPDATE SET is_enabled = EXCLUDED.is_enabled, updated_at = CURRENT_TIMESTAMP
                        RETURNING tenant_code, module_key, is_enabled
                        """)
                .bind("tenantCode", tenantCode)
                .bind("moduleKey", moduleKey)
                .bind("enabled", enabled)
                .map((row, metadata) -> new TenantModuleSubscription(
                        row.get("tenant_code", String.class),
                        row.get("module_key", String.class),
                        Boolean.TRUE.equals(row.get("is_enabled", Boolean.class))))
                .one();
    }

    @Override
    public Mono<TenantFeatureFlag> upsertFeatureFlag(String tenantCode, String featureKey, boolean enabled) {
        return databaseClient.sql("""
                        INSERT INTO tenant.tenant_feature_flags(tenant_code, feature_key, is_enabled, created_at, updated_at)
                        VALUES (:tenantCode, :featureKey, :enabled, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        ON CONFLICT (tenant_code, feature_key)
                        DO UPDATE SET is_enabled = EXCLUDED.is_enabled, updated_at = CURRENT_TIMESTAMP
                        RETURNING tenant_code, feature_key, is_enabled
                        """)
                .bind("tenantCode", tenantCode)
                .bind("featureKey", featureKey)
                .bind("enabled", enabled)
                .map((row, metadata) -> new TenantFeatureFlag(
                        row.get("tenant_code", String.class),
                        row.get("feature_key", String.class),
                        Boolean.TRUE.equals(row.get("is_enabled", Boolean.class))))
                .one();
    }
}
