package com.company.hrms.masterdata.infrastructure;

import com.company.hrms.masterdata.domain.LookupValue;
import com.company.hrms.masterdata.domain.MasterDataRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class MasterDataR2dbcRepository implements MasterDataRepository {

    private final DatabaseClient databaseClient;

    public MasterDataR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<LookupValue> findByType(String lookupType, String tenantId) {
        return databaseClient.sql("""
                        SELECT lookup_type, lookup_code, lookup_label, sort_order
                        FROM master_data.lookup_values
                        WHERE lookup_type = :lookupType
                          AND (tenant_id = :tenantId OR tenant_id IS NULL OR tenant_id = 'GLOBAL')
                          AND is_active = TRUE
                        ORDER BY CASE WHEN tenant_id = :tenantId THEN 0 ELSE 1 END,
                                 sort_order ASC
                        """)
                .bind("lookupType", lookupType)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> new LookupValue(
                        row.get("lookup_type", String.class),
                        row.get("lookup_code", String.class),
                        row.get("lookup_label", String.class),
                        row.get("sort_order", Integer.class) == null ? 0 : row.get("sort_order", Integer.class)))
                .all();
    }
}
