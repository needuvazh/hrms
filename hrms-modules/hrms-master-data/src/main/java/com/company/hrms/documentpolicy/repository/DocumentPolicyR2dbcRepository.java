package com.company.hrms.documentpolicy.repository;

import com.company.hrms.documentpolicy.model.DocumentPolicyModels;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class DocumentPolicyR2dbcRepository implements DocumentPolicyRepository {

    private final DatabaseClient databaseClient;

    public DocumentPolicyR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> create(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            DocumentPolicyModels.MasterUpsertRequest request,
            String actor
    ) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec spec = databaseClient.sql(insertSql(resource))
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", request.code().trim())
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> update(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            UUID id,
            DocumentPolicyModels.MasterUpsertRequest request,
            String actor
    ) {
        GenericExecuteSpec spec = databaseClient.sql(updateSql(resource))
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", request.code().trim())
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> get(String tenantId, DocumentPolicyModels.Resource resource, UUID id) {
        return databaseClient.sql(baseSelect(resource) + " WHERE t.tenant_id = :tenantId AND t.id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<DocumentPolicyModels.MasterViewDto> list(String tenantId, DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder(baseSelect(resource)).append(" WHERE t.tenant_id = :tenantId");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(t.").append(resource.codeColumn()).append(") LIKE lower(:q) OR lower(t.")
                    .append(resource.nameColumn()).append(") LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        applyResourceFilters(sql, resource, query);
        sql.append(" ORDER BY ").append(resolveSort(resource, query.sort())).append(" LIMIT :limit OFFSET :offset");

        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("limit", query.limit())
                .bind("offset", query.offset());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        spec = bindResourceFilters(spec, resource, query);
        return spec.map((row, metadata) -> mapRow(row, resource)).all();
    }

    @Override
    public Mono<Long> count(String tenantId, DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT count(*) AS cnt FROM ")
                .append(resource.table())
                .append(" t WHERE t.tenant_id = :tenantId");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(t.").append(resource.codeColumn()).append(") LIKE lower(:q) OR lower(t.")
                    .append(resource.nameColumn()).append(") LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        applyResourceFilters(sql, resource, query);

        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId);
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        spec = bindResourceFilters(spec, resource, query);
        return spec.map((row, metadata) -> row.get("cnt", Long.class) == null ? 0L : row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<DocumentPolicyModels.MasterViewDto> updateStatus(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    ) {
        String sql = "UPDATE " + resource.table() + " SET active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :updatedBy "
                + "WHERE tenant_id = :tenantId AND id = :id RETURNING *";
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_EXPIRY_RULES) {
            sql = "UPDATE " + resource.table() + " SET active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :updatedBy "
                    + "WHERE tenant_id = :tenantId AND id = :id "
                    + "RETURNING *, COALESCE(alert_days_before_json::text, '[]') AS alert_days_before_json_text";
        }
        return databaseClient.sql(sql)
                .bind("active", active)
                .bind("updatedBy", actor)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<DocumentPolicyModels.OptionViewDto> options(
            String tenantId,
            DocumentPolicyModels.Resource resource,
            String q,
            int limit,
            boolean activeOnly
    ) {
        StringBuilder sql = new StringBuilder("SELECT id, ")
                .append(resource.codeColumn()).append(" AS code, ")
                .append(resource.nameColumn()).append(" AS name FROM ")
                .append(resource.table())
                .append(" WHERE tenant_id = :tenantId");
        if (activeOnly) {
            sql.append(" AND active = TRUE");
        }
        if (StringUtils.hasText(q)) {
            sql.append(" AND (lower(").append(resource.codeColumn()).append(") LIKE lower(:q) OR lower(")
                    .append(resource.nameColumn()).append(") LIKE lower(:q))");
        }
        sql.append(" ORDER BY ").append(resource.nameColumn()).append(" ASC LIMIT :limit");

        GenericExecuteSpec spec = databaseClient.sql(sql.toString())
                .bind("tenantId", tenantId)
                .bind("limit", limit);
        if (StringUtils.hasText(q)) {
            spec = spec.bind("q", "%" + q.trim() + "%");
        }

        return spec.map((row, metadata) -> new DocumentPolicyModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> codeExists(String tenantId, DocumentPolicyModels.Resource resource, String code, UUID excludeId) {
        String sql = "SELECT count(*) AS cnt FROM " + resource.table() + " WHERE tenant_id = :tenantId AND lower(" + resource.codeColumn()
                + ") = lower(:code)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("tenantId", tenantId).bind("code", code);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Boolean> existsById(String tenantId, String tableName, UUID id) {
        if (id == null) {
            return Mono.just(false);
        }
        String sql = "SELECT count(*) AS cnt FROM " + tableName + " WHERE tenant_id = :tenantId AND id = :id";
        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    private String baseSelect(DocumentPolicyModels.Resource resource) {
        if (resource == DocumentPolicyModels.Resource.DOCUMENT_EXPIRY_RULES) {
            return "SELECT t.*, COALESCE(t.alert_days_before_json::text, '[]') AS alert_days_before_json_text FROM " + resource.table() + " t";
        }
        return "SELECT t.* FROM " + resource.table() + " t";
    }

    private String resolveSort(DocumentPolicyModels.Resource resource, String sort) {
        String fallback = "t.updated_at DESC";
        if (!StringUtils.hasText(sort)) {
            return fallback;
        }
        String[] parts = sort.split(",");
        String column = parts[0].trim();
        String direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) ? "ASC" : "DESC";
        if (!resource.sortableColumns().contains(column)) {
            return fallback;
        }
        return "t." + column + " " + direction;
    }

    private void applyResourceFilters(StringBuilder sql, DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query) {
        switch (resource) {
            case DOCUMENT_TYPES -> {
                if (query.documentCategoryId() != null) {
                    sql.append(" AND t.document_category_id = :documentCategoryId");
                }
                if (StringUtils.hasText(query.documentFor())) {
                    sql.append(" AND t.document_for = :documentFor");
                }
            }
            case DOCUMENT_APPLICABILITY_RULES -> {
                if (query.documentTypeId() != null) {
                    sql.append(" AND t.document_type_id = :documentTypeId");
                }
                if (query.workerTypeId() != null) {
                    sql.append(" AND t.worker_type_id = :workerTypeId");
                }
                if (query.employeeCategoryId() != null) {
                    sql.append(" AND t.employee_category_id = :employeeCategoryId");
                }
                if (query.nationalisationCategoryId() != null) {
                    sql.append(" AND t.nationalisation_category_id = :nationalisationCategoryId");
                }
                if (query.legalEntityId() != null) {
                    sql.append(" AND t.legal_entity_id = :legalEntityId");
                }
                if (query.jobFamilyId() != null) {
                    sql.append(" AND t.job_family_id = :jobFamilyId");
                }
                if (query.designationId() != null) {
                    sql.append(" AND t.designation_id = :designationId");
                }
                if (query.dependentTypeId() != null) {
                    sql.append(" AND t.dependent_type_id = :dependentTypeId");
                }
                if (query.mandatoryFlag() != null) {
                    sql.append(" AND t.mandatory_flag = :mandatoryFlag");
                }
                if (query.onboardingRequiredFlag() != null) {
                    sql.append(" AND t.onboarding_required_flag = :onboardingRequiredFlag");
                }
            }
            case DOCUMENT_EXPIRY_RULES -> {
                if (query.documentTypeId() != null) {
                    sql.append(" AND t.document_type_id = :documentTypeId");
                }
                if (query.expiryTrackingRequired() != null) {
                    sql.append(" AND t.expiry_tracking_required = :expiryTrackingRequired");
                }
                if (query.renewalRequired() != null) {
                    sql.append(" AND t.renewal_required = :renewalRequired");
                }
                if (query.blockTransactionOnExpiryFlag() != null) {
                    sql.append(" AND t.block_transaction_on_expiry_flag = :blockTransactionOnExpiryFlag");
                }
            }
            case ATTACHMENT_CATEGORIES -> {
                if (StringUtils.hasText(query.mimeGroup())) {
                    sql.append(" AND t.mime_group = :mimeGroup");
                }
            }
            default -> {
            }
        }
    }

    private GenericExecuteSpec bindResourceFilters(GenericExecuteSpec spec, DocumentPolicyModels.Resource resource, DocumentPolicyModels.SearchQuery query) {
        switch (resource) {
            case DOCUMENT_TYPES -> {
                if (query.documentCategoryId() != null) {
                    spec = spec.bind("documentCategoryId", query.documentCategoryId());
                }
                if (StringUtils.hasText(query.documentFor())) {
                    spec = spec.bind("documentFor", query.documentFor().trim().toUpperCase());
                }
            }
            case DOCUMENT_APPLICABILITY_RULES -> {
                if (query.documentTypeId() != null) {
                    spec = spec.bind("documentTypeId", query.documentTypeId());
                }
                if (query.workerTypeId() != null) {
                    spec = spec.bind("workerTypeId", query.workerTypeId());
                }
                if (query.employeeCategoryId() != null) {
                    spec = spec.bind("employeeCategoryId", query.employeeCategoryId());
                }
                if (query.nationalisationCategoryId() != null) {
                    spec = spec.bind("nationalisationCategoryId", query.nationalisationCategoryId());
                }
                if (query.legalEntityId() != null) {
                    spec = spec.bind("legalEntityId", query.legalEntityId());
                }
                if (query.jobFamilyId() != null) {
                    spec = spec.bind("jobFamilyId", query.jobFamilyId());
                }
                if (query.designationId() != null) {
                    spec = spec.bind("designationId", query.designationId());
                }
                if (query.dependentTypeId() != null) {
                    spec = spec.bind("dependentTypeId", query.dependentTypeId());
                }
                if (query.mandatoryFlag() != null) {
                    spec = spec.bind("mandatoryFlag", query.mandatoryFlag());
                }
                if (query.onboardingRequiredFlag() != null) {
                    spec = spec.bind("onboardingRequiredFlag", query.onboardingRequiredFlag());
                }
            }
            case DOCUMENT_EXPIRY_RULES -> {
                if (query.documentTypeId() != null) {
                    spec = spec.bind("documentTypeId", query.documentTypeId());
                }
                if (query.expiryTrackingRequired() != null) {
                    spec = spec.bind("expiryTrackingRequired", query.expiryTrackingRequired());
                }
                if (query.renewalRequired() != null) {
                    spec = spec.bind("renewalRequired", query.renewalRequired());
                }
                if (query.blockTransactionOnExpiryFlag() != null) {
                    spec = spec.bind("blockTransactionOnExpiryFlag", query.blockTransactionOnExpiryFlag());
                }
            }
            case ATTACHMENT_CATEGORIES -> {
                if (StringUtils.hasText(query.mimeGroup())) {
                    spec = spec.bind("mimeGroup", query.mimeGroup().trim().toUpperCase());
                }
            }
            default -> {
            }
        }
        return spec;
    }

    private String insertSql(DocumentPolicyModels.Resource resource) {
        return switch (resource) {
            case DOCUMENT_CATEGORIES -> """
                    INSERT INTO master_data.document_categories(
                        id, tenant_id, document_category_code, document_category_name, description, display_order,
                        active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :description, :displayOrder,
                        :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DOCUMENT_TYPES -> """
                    INSERT INTO master_data.document_types(
                        id, tenant_id, document_type_code, document_type_name, short_description, document_for,
                        document_category_id, attachment_required, issue_date_required, expiry_date_required,
                        reference_no_required, multiple_allowed, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :shortDescription, :documentFor,
                        :documentCategoryId, :attachmentRequired, :issueDateRequired, :expiryDateRequired,
                        :referenceNoRequired, :multipleAllowed, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DOCUMENT_APPLICABILITY_RULES -> """
                    INSERT INTO master_data.document_applicability_rules(
                        id, tenant_id, applicability_rule_code, document_type_id, worker_type_id, employee_category_id,
                        nationalisation_category_id, legal_entity_id, job_family_id, designation_id, dependent_type_id,
                        mandatory_flag, onboarding_required_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :documentTypeId, :workerTypeId, :employeeCategoryId,
                        :nationalisationCategoryId, :legalEntityId, :jobFamilyId, :designationId, :dependentTypeId,
                        :mandatoryFlag, :onboardingRequiredFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DOCUMENT_EXPIRY_RULES -> """
                    INSERT INTO master_data.document_expiry_rules(
                        id, tenant_id, expiry_rule_code, document_type_id, expiry_tracking_required, renewal_required,
                        alert_days_before_json, grace_period_days, block_transaction_on_expiry_flag, description,
                        active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :documentTypeId, :expiryTrackingRequired, :renewalRequired,
                        CAST(:alertDaysBeforeJson AS JSONB), :gracePeriodDays, :blockTransactionOnExpiryFlag, :description,
                        :active, :createdBy, :updatedBy
                    ) RETURNING *, COALESCE(alert_days_before_json::text, '[]') AS alert_days_before_json_text
                    """;
            case POLICY_DOCUMENT_TYPES -> """
                    INSERT INTO master_data.policy_document_types(
                        id, tenant_id, policy_document_type_code, policy_document_type_name, description,
                        version_required_flag, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :description,
                        :versionRequiredFlag, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case POLICY_ACKNOWLEDGEMENT_TYPES -> """
                    INSERT INTO master_data.policy_acknowledgement_types(
                        id, tenant_id, policy_ack_type_code, policy_ack_type_name, e_signature_required_flag,
                        reack_on_version_change_flag, annual_reack_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :eSignatureRequiredFlag,
                        :reackOnVersionChangeFlag, :annualReackFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case ATTACHMENT_CATEGORIES -> """
                    INSERT INTO master_data.attachment_categories(
                        id, tenant_id, attachment_category_code, attachment_category_name, mime_group,
                        max_file_size_mb, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :mimeGroup,
                        :maxFileSizeMb, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
        };
    }

    private String updateSql(DocumentPolicyModels.Resource resource) {
        return switch (resource) {
            case DOCUMENT_CATEGORIES -> """
                    UPDATE master_data.document_categories SET
                        document_category_code = :code,
                        document_category_name = :name,
                        description = :description,
                        display_order = :displayOrder,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case DOCUMENT_TYPES -> """
                    UPDATE master_data.document_types SET
                        document_type_code = :code,
                        document_type_name = :name,
                        short_description = :shortDescription,
                        document_for = :documentFor,
                        document_category_id = :documentCategoryId,
                        attachment_required = :attachmentRequired,
                        issue_date_required = :issueDateRequired,
                        expiry_date_required = :expiryDateRequired,
                        reference_no_required = :referenceNoRequired,
                        multiple_allowed = :multipleAllowed,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case DOCUMENT_APPLICABILITY_RULES -> """
                    UPDATE master_data.document_applicability_rules SET
                        applicability_rule_code = :code,
                        document_type_id = :documentTypeId,
                        worker_type_id = :workerTypeId,
                        employee_category_id = :employeeCategoryId,
                        nationalisation_category_id = :nationalisationCategoryId,
                        legal_entity_id = :legalEntityId,
                        job_family_id = :jobFamilyId,
                        designation_id = :designationId,
                        dependent_type_id = :dependentTypeId,
                        mandatory_flag = :mandatoryFlag,
                        onboarding_required_flag = :onboardingRequiredFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case DOCUMENT_EXPIRY_RULES -> """
                    UPDATE master_data.document_expiry_rules SET
                        expiry_rule_code = :code,
                        document_type_id = :documentTypeId,
                        expiry_tracking_required = :expiryTrackingRequired,
                        renewal_required = :renewalRequired,
                        alert_days_before_json = CAST(:alertDaysBeforeJson AS JSONB),
                        grace_period_days = :gracePeriodDays,
                        block_transaction_on_expiry_flag = :blockTransactionOnExpiryFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id
                    RETURNING *, COALESCE(alert_days_before_json::text, '[]') AS alert_days_before_json_text
                    """;
            case POLICY_DOCUMENT_TYPES -> """
                    UPDATE master_data.policy_document_types SET
                        policy_document_type_code = :code,
                        policy_document_type_name = :name,
                        description = :description,
                        version_required_flag = :versionRequiredFlag,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case POLICY_ACKNOWLEDGEMENT_TYPES -> """
                    UPDATE master_data.policy_acknowledgement_types SET
                        policy_ack_type_code = :code,
                        policy_ack_type_name = :name,
                        e_signature_required_flag = :eSignatureRequiredFlag,
                        reack_on_version_change_flag = :reackOnVersionChangeFlag,
                        annual_reack_flag = :annualReackFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case ATTACHMENT_CATEGORIES -> """
                    UPDATE master_data.attachment_categories SET
                        attachment_category_code = :code,
                        attachment_category_name = :name,
                        mime_group = :mimeGroup,
                        max_file_size_mb = :maxFileSizeMb,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
        };
    }

    private GenericExecuteSpec bindResourceFields(
            GenericExecuteSpec spec,
            DocumentPolicyModels.Resource resource,
            DocumentPolicyModels.MasterUpsertRequest request
    ) {
        return switch (resource) {
            case DOCUMENT_CATEGORIES -> bindNullable(
                    bindNullable(spec, "displayOrder", request.displayOrder(), Integer.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case DOCUMENT_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(
                                                            bindNullable(spec, "shortDescription", valueOrNull(request.shortDescription()), String.class),
                                                            "documentFor",
                                                            valueOrNull(request.documentFor()),
                                                            String.class),
                                                    "documentCategoryId",
                                                    request.documentCategoryId(),
                                                    UUID.class),
                                            "attachmentRequired",
                                            request.attachmentRequired(),
                                            Boolean.class),
                                    "issueDateRequired",
                                    request.issueDateRequired(),
                                    Boolean.class),
                            "expiryDateRequired",
                            request.expiryDateRequired(),
                            Boolean.class),
                    "referenceNoRequired",
                    request.referenceNoRequired(),
                    Boolean.class)
                    .bind("multipleAllowed", request.multipleAllowed() != null && request.multipleAllowed());
            case DOCUMENT_APPLICABILITY_RULES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(
                                                            bindNullable(
                                                                    bindNullable(
                                                                            bindNullable(
                                                                                    bindNullable(spec, "documentTypeId", request.documentTypeId(), UUID.class),
                                                                                    "workerTypeId",
                                                                                    request.workerTypeId(),
                                                                                    UUID.class),
                                                                            "employeeCategoryId",
                                                                            request.employeeCategoryId(),
                                                                            UUID.class),
                                                                    "nationalisationCategoryId",
                                                                    request.nationalisationCategoryId(),
                                                                    UUID.class),
                                                            "legalEntityId",
                                                            request.legalEntityId(),
                                                            UUID.class),
                                                    "jobFamilyId",
                                                    request.jobFamilyId(),
                                                    UUID.class),
                                            "designationId",
                                            request.designationId(),
                                            UUID.class),
                                    "dependentTypeId",
                                    request.dependentTypeId(),
                                    UUID.class),
                            "mandatoryFlag",
                            request.mandatoryFlag(),
                            Boolean.class),
                    "onboardingRequiredFlag",
                    request.onboardingRequiredFlag(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case DOCUMENT_EXPIRY_RULES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(spec, "documentTypeId", request.documentTypeId(), UUID.class),
                                                    "expiryTrackingRequired",
                                                    request.expiryTrackingRequired(),
                                                    Boolean.class),
                                            "renewalRequired",
                                            request.renewalRequired(),
                                            Boolean.class),
                                    "gracePeriodDays",
                                    request.gracePeriodDays(),
                                    Integer.class),
                            "blockTransactionOnExpiryFlag",
                            request.blockTransactionOnExpiryFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class)
                    .bind("alertDaysBeforeJson", toJsonArray(request.alertDaysBefore()));
            case POLICY_DOCUMENT_TYPES -> bindNullable(
                    bindNullable(spec, "description", valueOrNull(request.description()), String.class),
                    "versionRequiredFlag",
                    request.versionRequiredFlag(),
                    Boolean.class);
            case POLICY_ACKNOWLEDGEMENT_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(spec, "eSignatureRequiredFlag", request.eSignatureRequiredFlag(), Boolean.class),
                                    "reackOnVersionChangeFlag",
                                    request.reackOnVersionChangeFlag(),
                                    Boolean.class),
                            "annualReackFlag",
                            request.annualReackFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case ATTACHMENT_CATEGORIES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "mimeGroup", valueOrNull(request.mimeGroup()), String.class),
                            "maxFileSizeMb",
                            request.maxFileSizeMb(),
                            Integer.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
        };
    }

    private DocumentPolicyModels.MasterViewDto mapRow(Row row, DocumentPolicyModels.Resource resource) {
        return new DocumentPolicyModels.MasterViewDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get(resource.codeColumn(), String.class),
                row.get(resource.nameColumn(), String.class),
                get(row, "short_description", String.class),
                get(row, "document_for", String.class),
                get(row, "document_category_id", UUID.class),
                get(row, "attachment_required", Boolean.class),
                get(row, "issue_date_required", Boolean.class),
                get(row, "expiry_date_required", Boolean.class),
                get(row, "reference_no_required", Boolean.class),
                get(row, "multiple_allowed", Boolean.class),
                get(row, "display_order", Integer.class),
                get(row, "document_type_id", UUID.class),
                get(row, "worker_type_id", UUID.class),
                get(row, "employee_category_id", UUID.class),
                get(row, "nationalisation_category_id", UUID.class),
                get(row, "legal_entity_id", UUID.class),
                get(row, "job_family_id", UUID.class),
                get(row, "designation_id", UUID.class),
                get(row, "dependent_type_id", UUID.class),
                get(row, "mandatory_flag", Boolean.class),
                get(row, "onboarding_required_flag", Boolean.class),
                get(row, "expiry_tracking_required", Boolean.class),
                get(row, "renewal_required", Boolean.class),
                parseAlertDays(get(row, "alert_days_before_json_text", String.class)),
                get(row, "grace_period_days", Integer.class),
                get(row, "block_transaction_on_expiry_flag", Boolean.class),
                get(row, "version_required_flag", Boolean.class),
                get(row, "e_signature_required_flag", Boolean.class),
                get(row, "reack_on_version_change_flag", Boolean.class),
                get(row, "annual_reack_flag", Boolean.class),
                get(row, "mime_group", String.class),
                get(row, "max_file_size_mb", Integer.class),
                get(row, "description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
    }

    private List<Integer> parseAlertDays(String jsonText) {
        if (!StringUtils.hasText(jsonText)) {
            return List.of();
        }
        String body = jsonText.trim();
        if (body.length() < 2 || "[]".equals(body)) {
            return List.of();
        }
        String content = body.substring(1, body.length() - 1).trim();
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        String[] parts = content.split(",");
        List<Integer> values = new ArrayList<>();
        for (String part : parts) {
            values.add(Integer.parseInt(part.trim()));
        }
        return values;
    }

    private String toJsonArray(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        builder.append(']');
        return builder.toString();
    }

    private <T> T get(Row row, String column, Class<T> type) {
        try {
            return row.get(column, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String valueOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
