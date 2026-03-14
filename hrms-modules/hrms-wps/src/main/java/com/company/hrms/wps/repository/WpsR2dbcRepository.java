package com.company.hrms.wps.repository;

import com.company.hrms.wps.model.*;

import com.company.hrms.wps.model.WpsBatchDto;
import com.company.hrms.wps.model.WpsEmployeeEntryDto;
import com.company.hrms.wps.model.WpsExportFileDto;
import com.company.hrms.wps.repository.WpsRepository;
import com.company.hrms.wps.model.WpsStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class WpsR2dbcRepository implements WpsRepository {

    private final DatabaseClient databaseClient;

    public WpsR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<WpsBatchDto> saveBatch(WpsBatchDto batch) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO wps.wps_batches(
                            id, tenant_id, payroll_run_id, status, validation_summary,
                            created_by, exported_by, exported_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :payrollRunId, :status, :validationSummary,
                            :createdBy, :exportedBy, :exportedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, payroll_run_id, status, validation_summary,
                                  created_by, exported_by, exported_at, created_at, updated_at
                        """)
                .bind("id", batch.id())
                .bind("tenantId", batch.tenantId())
                .bind("payrollRunId", batch.payrollRunId())
                .bind("status", batch.status().name())
                .bind("createdBy", batch.createdBy())
                .bind("createdAt", batch.createdAt())
                .bind("updatedAt", batch.updatedAt());
        spec = bindNullable(spec, "validationSummary", batch.validationSummary(), String.class);
        spec = bindNullable(spec, "exportedBy", batch.exportedBy(), String.class);
        spec = bindNullable(spec, "exportedAt", batch.exportedAt(), Instant.class);

        return spec.map((row, metadata) -> mapBatch(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("status", String.class),
                        row.get("validation_summary", String.class),
                        row.get("created_by", String.class),
                        row.get("exported_by", String.class),
                        row.get("exported_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WpsBatchDto> updateBatch(WpsBatchDto batch) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE wps.wps_batches
                        SET status = :status,
                            validation_summary = :validationSummary,
                            exported_by = :exportedBy,
                            exported_at = :exportedAt,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, payroll_run_id, status, validation_summary,
                                  created_by, exported_by, exported_at, created_at, updated_at
                        """)
                .bind("id", batch.id())
                .bind("tenantId", batch.tenantId())
                .bind("status", batch.status().name())
                .bind("updatedAt", batch.updatedAt());
        spec = bindNullable(spec, "validationSummary", batch.validationSummary(), String.class);
        spec = bindNullable(spec, "exportedBy", batch.exportedBy(), String.class);
        spec = bindNullable(spec, "exportedAt", batch.exportedAt(), Instant.class);

        return spec.map((row, metadata) -> mapBatch(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("status", String.class),
                        row.get("validation_summary", String.class),
                        row.get("created_by", String.class),
                        row.get("exported_by", String.class),
                        row.get("exported_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WpsBatchDto> findBatchById(String tenantId, UUID batchId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, payroll_run_id, status, validation_summary,
                               created_by, exported_by, exported_at, created_at, updated_at
                        FROM wps.wps_batches
                        WHERE tenant_id = :tenantId
                          AND id = :batchId
                        """)
                .bind("tenantId", tenantId)
                .bind("batchId", batchId)
                .map((row, metadata) -> mapBatch(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("payroll_run_id", UUID.class),
                        row.get("status", String.class),
                        row.get("validation_summary", String.class),
                        row.get("created_by", String.class),
                        row.get("exported_by", String.class),
                        row.get("exported_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<WpsEmployeeEntryDto> saveEntry(WpsEmployeeEntryDto entry) {
        return databaseClient.sql("""
                        INSERT INTO wps.wps_employee_entries(
                            id, tenant_id, wps_batch_id, employee_id, net_amount, payment_reference, created_at
                        ) VALUES (
                            :id, :tenantId, :wpsBatchId, :employeeId, :netAmount, :paymentReference, :createdAt
                        )
                        RETURNING id, tenant_id, wps_batch_id, employee_id, net_amount, payment_reference, created_at
                        """)
                .bind("id", entry.id())
                .bind("tenantId", entry.tenantId())
                .bind("wpsBatchId", entry.wpsBatchId())
                .bind("employeeId", entry.employeeId())
                .bind("netAmount", entry.netAmount())
                .bind("paymentReference", entry.paymentReference())
                .bind("createdAt", entry.createdAt())
                .map((row, metadata) -> new WpsEmployeeEntryDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("wps_batch_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("payment_reference", String.class),
                        row.get("created_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<WpsEmployeeEntryDto> findEntriesByBatchId(String tenantId, UUID batchId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, wps_batch_id, employee_id, net_amount, payment_reference, created_at
                        FROM wps.wps_employee_entries
                        WHERE tenant_id = :tenantId
                          AND wps_batch_id = :batchId
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("batchId", batchId)
                .map((row, metadata) -> new WpsEmployeeEntryDto(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("wps_batch_id", UUID.class),
                        row.get("employee_id", UUID.class),
                        row.get("net_amount", BigDecimal.class),
                        row.get("payment_reference", String.class),
                        row.get("created_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<WpsExportFileDto> saveExportFile(WpsExportFileDto exportFile) {
        return databaseClient.sql("""
                        INSERT INTO wps.wps_export_files(
                            id, tenant_id, wps_batch_id, export_type, file_name, content_type,
                            content_hash, payload, status, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :wpsBatchId, :exportType, :fileName, :contentType,
                            :contentHash, :payload, :status, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, wps_batch_id, export_type, file_name, content_type,
                                  content_hash, payload, status, created_at, updated_at
                        """)
                .bind("id", exportFile.id())
                .bind("tenantId", exportFile.tenantId())
                .bind("wpsBatchId", exportFile.wpsBatchId())
                .bind("exportType", exportFile.exportType())
                .bind("fileName", exportFile.fileName())
                .bind("contentType", exportFile.contentType())
                .bind("contentHash", exportFile.contentHash())
                .bind("payload", exportFile.payload())
                .bind("status", exportFile.status().name())
                .bind("createdAt", exportFile.createdAt())
                .bind("updatedAt", exportFile.updatedAt())
                .map((row, metadata) -> mapExportFile(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("wps_batch_id", UUID.class),
                        row.get("export_type", String.class),
                        row.get("file_name", String.class),
                        row.get("content_type", String.class),
                        row.get("content_hash", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<WpsExportFileDto> findExportFilesByBatchId(String tenantId, UUID batchId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, wps_batch_id, export_type, file_name, content_type,
                               content_hash, payload, status, created_at, updated_at
                        FROM wps.wps_export_files
                        WHERE tenant_id = :tenantId
                          AND wps_batch_id = :batchId
                        ORDER BY created_at DESC
                        """)
                .bind("tenantId", tenantId)
                .bind("batchId", batchId)
                .map((row, metadata) -> mapExportFile(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("wps_batch_id", UUID.class),
                        row.get("export_type", String.class),
                        row.get("file_name", String.class),
                        row.get("content_type", String.class),
                        row.get("content_hash", String.class),
                        row.get("payload", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private WpsBatchDto mapBatch(
            UUID id,
            String tenantId,
            UUID payrollRunId,
            String status,
            String validationSummary,
            String createdBy,
            String exportedBy,
            Instant exportedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new WpsBatchDto(
                id,
                tenantId,
                payrollRunId,
                WpsStatus.valueOf(status),
                validationSummary,
                createdBy,
                exportedBy,
                exportedAt,
                createdAt,
                updatedAt);
    }

    private WpsExportFileDto mapExportFile(
            UUID id,
            String tenantId,
            UUID wpsBatchId,
            String exportType,
            String fileName,
            String contentType,
            String contentHash,
            String payload,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new WpsExportFileDto(
                id,
                tenantId,
                wpsBatchId,
                exportType,
                fileName,
                contentType,
                contentHash,
                payload,
                WpsStatus.valueOf(status),
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
