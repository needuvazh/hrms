package com.company.hrms.document.infrastructure;

import com.company.hrms.document.domain.DocumentRecord;
import com.company.hrms.document.domain.DocumentRepository;
import com.company.hrms.document.domain.DocumentType;
import com.company.hrms.document.domain.ExpiryDate;
import com.company.hrms.document.domain.StorageReference;
import com.company.hrms.document.domain.VerificationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcDocumentRepository implements DocumentRepository {

    private final DatabaseClient databaseClient;

    public R2dbcDocumentRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<DocumentRecord> save(DocumentRecord record) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO document.document_records(
                            id,
                            tenant_id,
                            document_type,
                            entity_type,
                            entity_id,
                            file_name,
                            storage_provider,
                            storage_bucket,
                            storage_object_key,
                            storage_checksum,
                            content_type,
                            size_bytes,
                            expiry_date,
                            verification_status,
                            archived,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by
                        ) VALUES (
                            :id,
                            :tenantId,
                            :documentType,
                            :entityType,
                            :entityId,
                            :fileName,
                            :storageProvider,
                            :storageBucket,
                            :storageObjectKey,
                            :storageChecksum,
                            :contentType,
                            :sizeBytes,
                            :expiryDate,
                            :verificationStatus,
                            :archived,
                            :createdAt,
                            :createdBy,
                            :updatedAt,
                            :updatedBy
                        )
                        RETURNING id, tenant_id, document_type, entity_type, entity_id, file_name,
                                  storage_provider, storage_bucket, storage_object_key, storage_checksum,
                                  content_type, size_bytes, expiry_date, verification_status, archived,
                                  created_at, created_by, updated_at, updated_by
                        """)
                .bind("id", record.id())
                .bind("tenantId", record.tenantId())
                .bind("documentType", record.documentType().name())
                .bind("entityType", record.entityType())
                .bind("entityId", record.entityId())
                .bind("fileName", record.fileName())
                .bind("storageProvider", record.storageReference().provider())
                .bind("storageObjectKey", record.storageReference().objectKey())
                .bind("contentType", record.storageReference().contentType())
                .bind("sizeBytes", record.storageReference().sizeBytes())
                .bind("verificationStatus", record.verificationStatus().name())
                .bind("archived", record.archived())
                .bind("createdAt", record.createdAt())
                .bind("createdBy", record.createdBy())
                .bind("updatedAt", record.updatedAt())
                .bind("updatedBy", record.updatedBy());

        spec = bindNullable(spec, "storageBucket", record.storageReference().bucket(), String.class);
        spec = bindNullable(spec, "storageChecksum", record.storageReference().checksum(), String.class);
        spec = bindNullable(spec, "expiryDate", record.expiryDate().value(), LocalDate.class);

        return spec.map((row, metadata) -> mapRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("document_type", String.class),
                        row.get("entity_type", String.class),
                        row.get("entity_id", String.class),
                        row.get("file_name", String.class),
                        row.get("storage_provider", String.class),
                        row.get("storage_bucket", String.class),
                        row.get("storage_object_key", String.class),
                        row.get("storage_checksum", String.class),
                        row.get("content_type", String.class),
                        row.get("size_bytes", Long.class),
                        row.get("expiry_date", LocalDate.class),
                        row.get("verification_status", String.class),
                        row.get("archived", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_at", Instant.class),
                        row.get("updated_by", String.class)))
                .one();
    }

    @Override
    public Mono<DocumentRecord> update(DocumentRecord record) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE document.document_records
                        SET verification_status = :verificationStatus,
                            archived = :archived,
                            updated_at = :updatedAt,
                            updated_by = :updatedBy
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, document_type, entity_type, entity_id, file_name,
                                  storage_provider, storage_bucket, storage_object_key, storage_checksum,
                                  content_type, size_bytes, expiry_date, verification_status, archived,
                                  created_at, created_by, updated_at, updated_by
                        """)
                .bind("id", record.id())
                .bind("tenantId", record.tenantId())
                .bind("verificationStatus", record.verificationStatus().name())
                .bind("archived", record.archived())
                .bind("updatedAt", record.updatedAt())
                .bind("updatedBy", record.updatedBy());

        return spec.map((row, metadata) -> mapRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("document_type", String.class),
                        row.get("entity_type", String.class),
                        row.get("entity_id", String.class),
                        row.get("file_name", String.class),
                        row.get("storage_provider", String.class),
                        row.get("storage_bucket", String.class),
                        row.get("storage_object_key", String.class),
                        row.get("storage_checksum", String.class),
                        row.get("content_type", String.class),
                        row.get("size_bytes", Long.class),
                        row.get("expiry_date", LocalDate.class),
                        row.get("verification_status", String.class),
                        row.get("archived", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_at", Instant.class),
                        row.get("updated_by", String.class)))
                .one();
    }

    @Override
    public Mono<DocumentRecord> findById(UUID documentId, String tenantId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, document_type, entity_type, entity_id, file_name,
                               storage_provider, storage_bucket, storage_object_key, storage_checksum,
                               content_type, size_bytes, expiry_date, verification_status, archived,
                               created_at, created_by, updated_at, updated_by
                        FROM document.document_records
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        """)
                .bind("id", documentId)
                .bind("tenantId", tenantId)
                .map((row, metadata) -> mapRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("document_type", String.class),
                        row.get("entity_type", String.class),
                        row.get("entity_id", String.class),
                        row.get("file_name", String.class),
                        row.get("storage_provider", String.class),
                        row.get("storage_bucket", String.class),
                        row.get("storage_object_key", String.class),
                        row.get("storage_checksum", String.class),
                        row.get("content_type", String.class),
                        row.get("size_bytes", Long.class),
                        row.get("expiry_date", LocalDate.class),
                        row.get("verification_status", String.class),
                        row.get("archived", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_at", Instant.class),
                        row.get("updated_by", String.class)))
                .one();
    }

    @Override
    public Flux<DocumentRecord> findByEntity(String tenantId, String entityType, String entityId, boolean includeArchived) {
        String sql = includeArchived
                ? """
                SELECT id, tenant_id, document_type, entity_type, entity_id, file_name,
                       storage_provider, storage_bucket, storage_object_key, storage_checksum,
                       content_type, size_bytes, expiry_date, verification_status, archived,
                       created_at, created_by, updated_at, updated_by
                FROM document.document_records
                WHERE tenant_id = :tenantId
                  AND entity_type = :entityType
                  AND entity_id = :entityId
                ORDER BY created_at DESC
                """
                : """
                SELECT id, tenant_id, document_type, entity_type, entity_id, file_name,
                       storage_provider, storage_bucket, storage_object_key, storage_checksum,
                       content_type, size_bytes, expiry_date, verification_status, archived,
                       created_at, created_by, updated_at, updated_by
                FROM document.document_records
                WHERE tenant_id = :tenantId
                  AND entity_type = :entityType
                  AND entity_id = :entityId
                  AND archived = false
                ORDER BY created_at DESC
                """;

        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("entityType", entityType)
                .bind("entityId", entityId)
                .map((row, metadata) -> mapRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("document_type", String.class),
                        row.get("entity_type", String.class),
                        row.get("entity_id", String.class),
                        row.get("file_name", String.class),
                        row.get("storage_provider", String.class),
                        row.get("storage_bucket", String.class),
                        row.get("storage_object_key", String.class),
                        row.get("storage_checksum", String.class),
                        row.get("content_type", String.class),
                        row.get("size_bytes", Long.class),
                        row.get("expiry_date", LocalDate.class),
                        row.get("verification_status", String.class),
                        row.get("archived", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_at", Instant.class),
                        row.get("updated_by", String.class)))
                .all();
    }

    @Override
    public Flux<DocumentRecord> findExpiringWithin(String tenantId, LocalDate fromDate, LocalDate toDate, boolean includeArchived) {
        String sql = includeArchived
                ? """
                SELECT id, tenant_id, document_type, entity_type, entity_id, file_name,
                       storage_provider, storage_bucket, storage_object_key, storage_checksum,
                       content_type, size_bytes, expiry_date, verification_status, archived,
                       created_at, created_by, updated_at, updated_by
                FROM document.document_records
                WHERE tenant_id = :tenantId
                  AND expiry_date IS NOT NULL
                  AND expiry_date BETWEEN :fromDate AND :toDate
                ORDER BY expiry_date ASC
                """
                : """
                SELECT id, tenant_id, document_type, entity_type, entity_id, file_name,
                       storage_provider, storage_bucket, storage_object_key, storage_checksum,
                       content_type, size_bytes, expiry_date, verification_status, archived,
                       created_at, created_by, updated_at, updated_by
                FROM document.document_records
                WHERE tenant_id = :tenantId
                  AND archived = false
                  AND expiry_date IS NOT NULL
                  AND expiry_date BETWEEN :fromDate AND :toDate
                ORDER BY expiry_date ASC
                """;

        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> mapRecord(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("document_type", String.class),
                        row.get("entity_type", String.class),
                        row.get("entity_id", String.class),
                        row.get("file_name", String.class),
                        row.get("storage_provider", String.class),
                        row.get("storage_bucket", String.class),
                        row.get("storage_object_key", String.class),
                        row.get("storage_checksum", String.class),
                        row.get("content_type", String.class),
                        row.get("size_bytes", Long.class),
                        row.get("expiry_date", LocalDate.class),
                        row.get("verification_status", String.class),
                        row.get("archived", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("created_by", String.class),
                        row.get("updated_at", Instant.class),
                        row.get("updated_by", String.class)))
                .all();
    }

    private DocumentRecord mapRecord(
            UUID id,
            String tenantId,
            String documentType,
            String entityType,
            String entityId,
            String fileName,
            String storageProvider,
            String storageBucket,
            String storageObjectKey,
            String storageChecksum,
            String contentType,
            Long sizeBytes,
            LocalDate expiryDate,
            String verificationStatus,
            Boolean archived,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy
    ) {
        return new DocumentRecord(
                id,
                tenantId,
                DocumentType.valueOf(documentType),
                entityType,
                entityId,
                fileName,
                new StorageReference(
                        storageProvider,
                        storageBucket,
                        storageObjectKey,
                        storageChecksum,
                        contentType,
                        sizeBytes == null ? 0L : sizeBytes),
                new ExpiryDate(expiryDate),
                VerificationStatus.valueOf(verificationStatus),
                Boolean.TRUE.equals(archived),
                createdAt,
                createdBy,
                updatedAt,
                updatedBy);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
