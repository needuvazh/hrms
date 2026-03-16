package com.company.hrms.compliance.repository;

import com.company.hrms.compliance.model.ComplianceModels;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ComplianceR2dbcRepository implements ComplianceRepository {

    private final DatabaseClient databaseClient;

    public ComplianceR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> create(
            String tenantId,
            ComplianceModels.Resource resource,
            ComplianceModels.MasterUpsertRequest request,
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
    public Mono<ComplianceModels.MasterViewDto> update(
            String tenantId,
            ComplianceModels.Resource resource,
            UUID id,
            ComplianceModels.MasterUpsertRequest request,
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
    public Mono<ComplianceModels.MasterViewDto> get(String tenantId, ComplianceModels.Resource resource, UUID id) {
        return databaseClient.sql(baseSelect(resource) + " WHERE t.tenant_id = :tenantId AND t.id = :id")
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<ComplianceModels.MasterViewDto> list(String tenantId, ComplianceModels.Resource resource, ComplianceModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder(baseSelect(resource)).append(" WHERE t.tenant_id = :tenantId");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(t.").append(resource.codeColumn()).append(") LIKE lower(:q) OR lower(t.")
                    .append(resource.nameColumn()).append(") LIKE lower(:q) OR lower(coalesce(t.description,'')) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        sql.append(" ORDER BY t.updated_at DESC LIMIT :limit OFFSET :offset");

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
        return spec.map((row, metadata) -> mapRow(row, resource)).all();
    }

    @Override
    public Mono<ComplianceModels.MasterViewDto> updateStatus(
            String tenantId,
            ComplianceModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    ) {
        String sql = "UPDATE " + resource.table() + " SET active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :updatedBy "
                + "WHERE tenant_id = :tenantId AND id = :id RETURNING *";
        return databaseClient.sql(sql)
                .bind("active", active)
                .bind("updatedBy", actor)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<ComplianceModels.OptionViewDto> options(String tenantId, ComplianceModels.Resource resource, String q, int limit) {
        StringBuilder sql = new StringBuilder("SELECT id, ")
                .append(resource.codeColumn()).append(" AS code, ")
                .append(resource.nameColumn()).append(" AS name FROM ")
                .append(resource.table())
                .append(" WHERE tenant_id = :tenantId AND active = TRUE");
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
        return spec.map((row, metadata) -> new ComplianceModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> codeExists(String tenantId, ComplianceModels.Resource resource, String code, UUID excludeId) {
        String sql = "SELECT count(*) AS cnt FROM " + resource.table() + " WHERE tenant_id = :tenantId AND lower(" + resource.codeColumn()
                + ") = lower(:code)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("code", code);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    private String baseSelect(ComplianceModels.Resource resource) {
        return "SELECT * FROM " + resource.table() + " t";
    }

    private String insertSql(ComplianceModels.Resource resource) {
        return switch (resource) {
            case VISA_TYPES -> """
                    INSERT INTO master_data.visa_types(
                        id, tenant_id, visa_type_code, visa_type_name, visa_category, applies_to, renewable_flag,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :visaCategory, :appliesTo, :renewableFlag,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case RESIDENCE_STATUSES -> """
                    INSERT INTO master_data.residence_statuses(
                        id, tenant_id, residence_status_code, residence_status_name, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case LABOUR_CARD_TYPES -> """
                    INSERT INTO master_data.labour_card_types(
                        id, tenant_id, labour_card_type_code, labour_card_type_name, expiry_tracking_required,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :expiryTrackingRequired,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case CIVIL_ID_TYPES -> """
                    INSERT INTO master_data.civil_id_types(
                        id, tenant_id, civil_id_type_code, civil_id_type_name, applies_to, expiry_tracking_required,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :appliesTo, :expiryTrackingRequired,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case PASSPORT_TYPES -> """
                    INSERT INTO master_data.passport_types(
                        id, tenant_id, passport_type_code, passport_type_name, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SPONSOR_TYPES -> """
                    INSERT INTO master_data.sponsor_types(
                        id, tenant_id, sponsor_type_code, sponsor_type_name, applies_to, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :appliesTo, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case WORK_PERMIT_TYPES -> """
                    INSERT INTO master_data.work_permit_types(
                        id, tenant_id, work_permit_type_code, work_permit_type_name, renewable_flag, description,
                        active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :renewableFlag, :description,
                        :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case NATIONALISATION_CATEGORIES -> """
                    INSERT INTO master_data.nationalisation_categories(
                        id, tenant_id, nationalisation_category_code, nationalisation_category_name, omani_flag,
                        counts_for_omanisation_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :omaniFlag,
                        :countsForOmanisationFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SOCIAL_INSURANCE_TYPES -> """
                    INSERT INTO master_data.social_insurance_eligibility_types(
                        id, tenant_id, social_insurance_type_code, social_insurance_type_name, pension_eligible_flag,
                        occupational_hazard_eligible_flag, govt_contribution_applicable_flag,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :pensionEligibleFlag,
                        :occupationalHazardEligibleFlag, :govtContributionApplicableFlag,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case BENEFICIARY_TYPES -> """
                    INSERT INTO master_data.beneficiary_types(
                        id, tenant_id, beneficiary_type_code, beneficiary_type_name, priority_order,
                        description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :priorityOrder,
                        :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DEPENDENT_TYPES -> """
                    INSERT INTO master_data.dependent_types(
                        id, tenant_id, dependent_type_code, dependent_type_name, insurance_eligible_flag,
                        family_visa_eligible_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :insuranceEligibleFlag,
                        :familyVisaEligibleFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
        };
    }

    private String updateSql(ComplianceModels.Resource resource) {
        return switch (resource) {
            case VISA_TYPES -> """
                    UPDATE master_data.visa_types SET
                        visa_type_code = :code,
                        visa_type_name = :name,
                        visa_category = :visaCategory,
                        applies_to = :appliesTo,
                        renewable_flag = :renewableFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case RESIDENCE_STATUSES -> """
                    UPDATE master_data.residence_statuses SET
                        residence_status_code = :code,
                        residence_status_name = :name,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case LABOUR_CARD_TYPES -> """
                    UPDATE master_data.labour_card_types SET
                        labour_card_type_code = :code,
                        labour_card_type_name = :name,
                        expiry_tracking_required = :expiryTrackingRequired,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case CIVIL_ID_TYPES -> """
                    UPDATE master_data.civil_id_types SET
                        civil_id_type_code = :code,
                        civil_id_type_name = :name,
                        applies_to = :appliesTo,
                        expiry_tracking_required = :expiryTrackingRequired,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case PASSPORT_TYPES -> """
                    UPDATE master_data.passport_types SET
                        passport_type_code = :code,
                        passport_type_name = :name,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case SPONSOR_TYPES -> """
                    UPDATE master_data.sponsor_types SET
                        sponsor_type_code = :code,
                        sponsor_type_name = :name,
                        applies_to = :appliesTo,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case WORK_PERMIT_TYPES -> """
                    UPDATE master_data.work_permit_types SET
                        work_permit_type_code = :code,
                        work_permit_type_name = :name,
                        renewable_flag = :renewableFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case NATIONALISATION_CATEGORIES -> """
                    UPDATE master_data.nationalisation_categories SET
                        nationalisation_category_code = :code,
                        nationalisation_category_name = :name,
                        omani_flag = :omaniFlag,
                        counts_for_omanisation_flag = :countsForOmanisationFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case SOCIAL_INSURANCE_TYPES -> """
                    UPDATE master_data.social_insurance_eligibility_types SET
                        social_insurance_type_code = :code,
                        social_insurance_type_name = :name,
                        pension_eligible_flag = :pensionEligibleFlag,
                        occupational_hazard_eligible_flag = :occupationalHazardEligibleFlag,
                        govt_contribution_applicable_flag = :govtContributionApplicableFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case BENEFICIARY_TYPES -> """
                    UPDATE master_data.beneficiary_types SET
                        beneficiary_type_code = :code,
                        beneficiary_type_name = :name,
                        priority_order = :priorityOrder,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case DEPENDENT_TYPES -> """
                    UPDATE master_data.dependent_types SET
                        dependent_type_code = :code,
                        dependent_type_name = :name,
                        insurance_eligible_flag = :insuranceEligibleFlag,
                        family_visa_eligible_flag = :familyVisaEligibleFlag,
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
            ComplianceModels.Resource resource,
            ComplianceModels.MasterUpsertRequest request
    ) {
        return switch (resource) {
            case VISA_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "visaCategory", valueOrNull(request.visaCategory()), String.class),
                            "appliesTo",
                            valueOrNull(request.appliesTo()),
                            String.class),
                    "renewableFlag",
                    request.renewableFlag(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case RESIDENCE_STATUSES, PASSPORT_TYPES -> spec.bind("description", valueOrNull(request.description()));
            case LABOUR_CARD_TYPES -> bindNullable(spec, "expiryTrackingRequired", request.expiryTrackingRequired(), Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case CIVIL_ID_TYPES -> bindNullable(
                    bindNullable(spec, "appliesTo", valueOrNull(request.appliesTo()), String.class),
                    "expiryTrackingRequired",
                    request.expiryTrackingRequired(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case SPONSOR_TYPES -> bindNullable(spec, "appliesTo", valueOrNull(request.appliesTo()), String.class)
                    .bind("description", valueOrNull(request.description()));
            case WORK_PERMIT_TYPES -> bindNullable(spec, "renewableFlag", request.renewableFlag(), Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case NATIONALISATION_CATEGORIES -> bindNullable(
                    bindNullable(spec, "omaniFlag", request.omaniFlag(), Boolean.class),
                    "countsForOmanisationFlag",
                    request.countsForOmanisationFlag(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case SOCIAL_INSURANCE_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "pensionEligibleFlag", request.pensionEligibleFlag(), Boolean.class),
                            "occupationalHazardEligibleFlag",
                            request.occupationalHazardEligibleFlag(),
                            Boolean.class),
                    "govtContributionApplicableFlag",
                    request.govtContributionApplicableFlag(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
            case BENEFICIARY_TYPES -> bindNullable(spec, "priorityOrder", request.priorityOrder(), Integer.class)
                    .bind("description", valueOrNull(request.description()));
            case DEPENDENT_TYPES -> bindNullable(
                    bindNullable(spec, "insuranceEligibleFlag", request.insuranceEligibleFlag(), Boolean.class),
                    "familyVisaEligibleFlag",
                    request.familyVisaEligibleFlag(),
                    Boolean.class)
                    .bind("description", valueOrNull(request.description()));
        };
    }

    private ComplianceModels.MasterViewDto mapRow(Row row, ComplianceModels.Resource resource) {
        return new ComplianceModels.MasterViewDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                row.get(resource.codeColumn(), String.class),
                row.get(resource.nameColumn(), String.class),
                get(row, "visa_category", String.class),
                get(row, "applies_to", String.class),
                get(row, "renewable_flag", Boolean.class),
                get(row, "expiry_tracking_required", Boolean.class),
                get(row, "omani_flag", Boolean.class),
                get(row, "counts_for_omanisation_flag", Boolean.class),
                get(row, "pension_eligible_flag", Boolean.class),
                get(row, "occupational_hazard_eligible_flag", Boolean.class),
                get(row, "govt_contribution_applicable_flag", Boolean.class),
                get(row, "priority_order", Integer.class),
                get(row, "insurance_eligible_flag", Boolean.class),
                get(row, "family_visa_eligible_flag", Boolean.class),
                get(row, "description", String.class),
                Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class),
                row.get("created_by", String.class),
                row.get("updated_by", String.class));
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
