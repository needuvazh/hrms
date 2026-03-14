package com.company.hrms.tenant.infrastructure;

import com.company.hrms.tenant.domain.Country;
import com.company.hrms.tenant.domain.Tenant;
import com.company.hrms.tenant.domain.TenantCountryConfig;
import com.company.hrms.tenant.domain.TenantRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class TenantR2dbcRepository implements TenantRepository {

    private final DatabaseClient databaseClient;

    public TenantR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Tenant> findByTenantCode(String tenantCode) {
        return databaseClient.sql("""
                        SELECT id, tenant_code, tenant_name, is_active
                        FROM tenant.tenants
                        WHERE tenant_code = :tenantCode
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new Tenant(
                        row.get("id", UUID.class),
                        row.get("tenant_code", String.class),
                        row.get("tenant_name", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .one();
    }

    @Override
    public Flux<Country> listCountries() {
        return databaseClient.sql("""
                        SELECT country_code, country_name, currency_code, timezone, locale, is_active
                        FROM tenant.countries
                        ORDER BY country_name ASC
                        """)
                .map((row, metadata) -> new Country(
                        row.get("country_code", String.class),
                        row.get("country_name", String.class),
                        row.get("currency_code", String.class),
                        row.get("timezone", String.class),
                        row.get("locale", String.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .all();
    }

    @Override
    public Flux<TenantCountryConfig> listTenantCountries(String tenantCode) {
        return databaseClient.sql("""
                        SELECT tenant_code, country_code, is_primary, compliance_profile, effective_from, effective_to, is_active
                        FROM tenant.tenant_country_config
                        WHERE tenant_code = :tenantCode
                        ORDER BY is_primary DESC, country_code ASC
                        """)
                .bind("tenantCode", tenantCode)
                .map((row, metadata) -> new TenantCountryConfig(
                        row.get("tenant_code", String.class),
                        row.get("country_code", String.class),
                        Boolean.TRUE.equals(row.get("is_primary", Boolean.class)),
                        row.get("compliance_profile", String.class),
                        row.get("effective_from", LocalDate.class),
                        row.get("effective_to", LocalDate.class),
                        Boolean.TRUE.equals(row.get("is_active", Boolean.class))))
                .all();
    }
}
