package com.company.hrms.masterdata.reference.infrastructure;

import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceMasterRow;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ReferenceMasterR2dbcRepository implements ReferenceMasterRepository {

    private final DatabaseClient databaseClient;

    public ReferenceMasterR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<ReferenceMasterRow> create(ReferenceResource resource, ReferenceMasterUpsertRequest request, String actor) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec spec = databaseClient.sql(insertSql(resource))
                .bind("id", id)
                .bind("code", resolveCode(resource, request))
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<ReferenceMasterRow> update(ReferenceResource resource, UUID id, ReferenceMasterUpsertRequest request, String actor) {
        GenericExecuteSpec spec = databaseClient.sql(updateSql(resource))
                .bind("id", id)
                .bind("code", resolveCode(resource, request))
                .bind("name", request.name().trim())
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<ReferenceMasterRow> findById(ReferenceResource resource, UUID id) {
        return databaseClient.sql(baseSelect(resource) + " WHERE t.id = :id")
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<ReferenceMasterRow> list(ReferenceResource resource, ReferenceSearchQuery query) {
        StringBuilder sql = new StringBuilder(baseSelect(resource)).append(" WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (");
            boolean first = true;
            for (String column : resource.searchColumns()) {
                if (!first) {
                    sql.append(" OR ");
                }
                sql.append("lower(t.").append(column).append(") LIKE lower(:q)");
                first = false;
            }
            sql.append(")");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        if (resource == ReferenceResource.SKILLS && query.skillCategoryId() != null) {
            sql.append(" AND t.skill_category_id = :skillCategoryId");
        }
        sql.append(" ORDER BY ").append(resolveSort(resource, query.sort()));
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
        if (resource == ReferenceResource.SKILLS && query.skillCategoryId() != null) {
            spec = spec.bind("skillCategoryId", query.skillCategoryId());
        }
        return spec.map((row, metadata) -> mapRow(row, resource)).all();
    }

    @Override
    public Mono<Long> count(ReferenceResource resource, ReferenceSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT count(*) AS cnt FROM ").append(resource.tableName()).append(" t WHERE 1=1");
        String likeQuery = "%";
        if (StringUtils.hasText(query.q())) {
            likeQuery = "%" + query.q().trim() + "%";
            sql.append(" AND (");
            boolean first = true;
            for (String column : resource.searchColumns()) {
                if (!first) {
                    sql.append(" OR ");
                }
                sql.append("lower(t.").append(column).append(") LIKE lower(:q)");
                first = false;
            }
            sql.append(")");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        if (resource == ReferenceResource.SKILLS && query.skillCategoryId() != null) {
            sql.append(" AND t.skill_category_id = :skillCategoryId");
        }
        GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", likeQuery);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        if (resource == ReferenceResource.SKILLS && query.skillCategoryId() != null) {
            spec = spec.bind("skillCategoryId", query.skillCategoryId());
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class)).one();
    }

    @Override
    public Flux<ReferenceOptionViewDto> options(ReferenceResource resource, boolean activeOnly) {
        String sql = "SELECT id, " + resource.codeColumn() + " AS code, " + resource.nameColumn() + " AS name FROM " + resource.tableName()
                + (activeOnly ? " WHERE active = TRUE" : "") + " ORDER BY " + resource.nameColumn() + " ASC";
        return databaseClient.sql(sql)
                .map((row, metadata) -> new ReferenceOptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> existsCode(ReferenceResource resource, String code, UUID excludeId) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + resource.tableName() + " WHERE lower(" + resource.codeColumn() + ") = lower(:code)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("code", code);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Boolean> existsName(ReferenceResource resource, String name, UUID excludeId) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + resource.tableName() + " WHERE lower(" + resource.nameColumn() + ") = lower(:name)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("name", name);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Void> updateStatus(ReferenceResource resource, UUID id, boolean active, String actor) {
        return databaseClient.sql("UPDATE " + resource.tableName() + " SET active = :active, updated_at = :updatedAt, updated_by = :updatedBy WHERE id = :id")
                .bind("active", active)
                .bind("updatedAt", Instant.now())
                .bind("updatedBy", actor)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Boolean> existsById(ReferenceResource resource, UUID id) {
        return databaseClient.sql("SELECT count(*) AS cnt FROM " + resource.tableName() + " WHERE id = :id")
                .bind("id", id)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> existsCurrencyCode(String currencyCode) {
        return databaseClient.sql("SELECT count(*) AS cnt FROM master_data.currencies WHERE lower(currency_code) = lower(:code)")
                .bind("code", currencyCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<Boolean> existsCountryCode(String countryCode) {
        return databaseClient.sql("SELECT count(*) AS cnt FROM master_data.countries WHERE lower(country_code) = lower(:code)")
                .bind("code", countryCode)
                .map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0)
                .one();
    }

    @Override
    public Mono<String> resolveCurrencyCode(String currencyToken) {
        return databaseClient.sql("""
                SELECT currency_code
                FROM master_data.currencies
                WHERE active = TRUE
                  AND (
                    lower(currency_code) = lower(:token)
                    OR lower(currency_symbol) = lower(:token)
                    OR lower(currency_name) = lower(:token)
                    OR lower(currency_name) LIKE lower('%' || :token || '%')
                  )
                ORDER BY
                    CASE
                        WHEN lower(currency_code) = lower(:token) THEN 1
                        WHEN lower(currency_symbol) = lower(:token) THEN 2
                        WHEN lower(currency_name) = lower(:token) THEN 3
                        ELSE 4
                    END,
                    currency_code
                LIMIT 1
                """)
                .bind("token", currencyToken)
                .map((row, metadata) -> row.get("currency_code", String.class))
                .one();
    }

    private String baseSelect(ReferenceResource resource) {
        if (resource == ReferenceResource.SKILLS) {
            return """
                    SELECT t.id,
                           t.skill_code AS code,
                           t.skill_name AS name,
                           NULL::VARCHAR AS short_name,
                           NULL::VARCHAR AS iso2_code,
                           NULL::VARCHAR AS iso3_code,
                           NULL::VARCHAR AS phone_code,
                           NULL::VARCHAR AS nationality_name,
                           NULL::VARCHAR AS default_currency_code,
                           NULL::VARCHAR AS default_timezone,
                           NULL::BOOLEAN AS gcc_flag,
                           NULL::VARCHAR AS native_name,
                           NULL::BOOLEAN AS rtl_enabled,
                           NULL::VARCHAR AS country_code,
                           NULL::BOOLEAN AS gcc_national_flag,
                           NULL::BOOLEAN AS omani_flag,
                           NULL::INTEGER AS display_order,
                           NULL::BOOLEAN AS dependent_allowed,
                           NULL::BOOLEAN AS emergency_contact_allowed,
                           NULL::BOOLEAN AS beneficiary_allowed,
                           NULL::VARCHAR AS short_description,
                           NULL::VARCHAR AS document_for,
                           NULL::BOOLEAN AS issue_date_required,
                           NULL::BOOLEAN AS expiry_date_required,
                           NULL::BOOLEAN AS alert_required,
                           NULL::INTEGER AS alert_days_before,
                           NULL::INTEGER AS ranking_order,
                           NULL::BOOLEAN AS expiry_tracking_required,
                           NULL::BOOLEAN AS issuing_body_required,
                           t.description,
                           t.skill_category_id,
                           sc.skill_category_name,
                           t.active,
                           t.created_at,
                           t.updated_at,
                           t.created_by,
                           t.updated_by
                    FROM master_data.skills t
                    JOIN master_data.skill_categories sc ON sc.id = t.skill_category_id
                    """;
        }
        return "SELECT * FROM " + resource.tableName() + " t";
    }

    private String resolveSort(ReferenceResource resource, String sort) {
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

    private GenericExecuteSpec bindResourceFields(GenericExecuteSpec spec, ReferenceResource resource, ReferenceMasterUpsertRequest request) {
        return switch (resource) {
            case COUNTRIES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(
                                                            bindNullable(
                                                                    bindNullable(
                                                                            bindNullable(spec, "shortName", valueOrNull(request.shortName()), String.class),
                                                                            "iso2Code", valueOrNull(request.iso2Code()), String.class),
                                                                    "iso3Code", valueOrNull(request.iso3Code()), String.class),
                                                            "phoneCode", valueOrNull(request.phoneCode()), String.class),
                                                    "nationalityName", valueOrNull(request.nationalityName()), String.class),
                                            "defaultCurrencyCode", valueOrNull(request.defaultCurrencyCode()), String.class),
                                    "defaultTimezone", valueOrNull(request.defaultTimezone()), String.class),
                            "nativeName", valueOrNull(request.nativeName()), String.class),
                    "description", valueOrNull(request.description()), String.class)
                    .bind("gccFlag", request.gccFlag() != null && request.gccFlag())
                    .bind("rtlEnabled", request.rtlEnabled() != null && request.rtlEnabled());
            case CURRENCIES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "currencySymbol", valueOrNull(request.shortName()), String.class),
                            "shortDescription",
                            valueOrNull(request.shortDescription()),
                            String.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class)
                    .bind("decimalPlaces", request.decimalPlaces() == null ? 2 : request.decimalPlaces());
            case LANGUAGES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "nativeName", valueOrNull(request.nativeName()), String.class),
                            "shortDescription",
                            valueOrNull(request.shortDescription()),
                            String.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class)
                    .bind("rtlEnabled", request.rtlEnabled() != null && request.rtlEnabled());
            case NATIONALITIES -> bindNullable(spec, "countryCode", valueOrNull(request.countryCode()), String.class)
                    .bind("gccNationalFlag", request.gccNationalFlag() != null && request.gccNationalFlag())
                    .bind("omaniFlag", request.omaniFlag() != null && request.omaniFlag());
            case RELIGIONS -> spec;
            case GENDERS -> bindNullable(spec, "displayOrder", request.displayOrder(), Integer.class);
            case MARITAL_STATUSES -> spec;
            case RELATIONSHIP_TYPES -> spec
                    .bind("dependentAllowed", request.dependentAllowed() != null && request.dependentAllowed())
                    .bind("emergencyContactAllowed", request.emergencyContactAllowed() != null && request.emergencyContactAllowed())
                    .bind("beneficiaryAllowed", request.beneficiaryAllowed() != null && request.beneficiaryAllowed());
            case DOCUMENT_TYPES -> bindNullable(
                    bindNullable(spec, "shortDescription", valueOrNull(request.shortDescription()), String.class)
                            .bind("documentFor", request.documentFor().name())
                            .bind("issueDateRequired", request.issueDateRequired() != null && request.issueDateRequired())
                            .bind("expiryDateRequired", request.expiryDateRequired() != null && request.expiryDateRequired())
                            .bind("alertRequired", request.alertRequired() != null && request.alertRequired()),
                    "alertDaysBefore",
                    request.alertDaysBefore(),
                    Integer.class);
            case EDUCATION_LEVELS -> bindNullable(spec, "rankingOrder", request.rankingOrder(), Integer.class);
            case CERTIFICATION_TYPES -> spec
                    .bind("expiryTrackingRequired", request.expiryTrackingRequired() != null && request.expiryTrackingRequired())
                    .bind("issuingBodyRequired", request.issuingBodyRequired() != null && request.issuingBodyRequired());
            case SKILL_CATEGORIES -> bindNullable(spec, "description", valueOrNull(request.description()), String.class);
            case SKILLS -> bindNullable(
                    bindNullable(spec, "skillCategoryId", request.skillCategoryId(), UUID.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
        };
    }

    private String insertSql(ReferenceResource resource) {
        return switch (resource) {
            case COUNTRIES -> """
                    INSERT INTO master_data.countries(
                        id, country_code, country_name, short_name, iso2_code, iso3_code, phone_code, nationality_name,
                        default_currency_code, default_timezone, native_name, description, gcc_flag, rtl_enabled, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :shortName, :iso2Code, :iso3Code, :phoneCode, :nationalityName,
                        :defaultCurrencyCode, :defaultTimezone, :nativeName, :description, :gccFlag, :rtlEnabled, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case CURRENCIES -> """
                    INSERT INTO master_data.currencies(
                        id, currency_code, currency_name, currency_symbol, short_description, description, decimal_places, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :currencySymbol, :shortDescription, :description, :decimalPlaces, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case LANGUAGES -> """
                    INSERT INTO master_data.languages(
                        id, language_code, language_name, native_name, short_description, description, rtl_enabled, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :nativeName, :shortDescription, :description, :rtlEnabled, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case NATIONALITIES -> """
                    INSERT INTO master_data.nationalities(
                        id, nationality_code, nationality_name, country_code, gcc_national_flag, omani_flag, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :countryCode, :gccNationalFlag, :omaniFlag, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case RELIGIONS -> """
                    INSERT INTO master_data.religions(id, religion_code, religion_name, active, created_by, updated_by)
                    VALUES (:id, :code, :name, :active, :createdBy, :updatedBy) RETURNING *
                    """;
            case GENDERS -> """
                    INSERT INTO master_data.genders(id, gender_code, gender_name, display_order, active, created_by, updated_by)
                    VALUES (:id, :code, :name, :displayOrder, :active, :createdBy, :updatedBy) RETURNING *
                    """;
            case MARITAL_STATUSES -> """
                    INSERT INTO master_data.marital_statuses(id, marital_status_code, marital_status_name, active, created_by, updated_by)
                    VALUES (:id, :code, :name, :active, :createdBy, :updatedBy) RETURNING *
                    """;
            case RELATIONSHIP_TYPES -> """
                    INSERT INTO master_data.relationship_types(
                        id, relationship_type_code, relationship_type_name, dependent_allowed, emergency_contact_allowed, beneficiary_allowed, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :dependentAllowed, :emergencyContactAllowed, :beneficiaryAllowed, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case DOCUMENT_TYPES -> """
                    INSERT INTO master_data.document_types(
                        id, document_type_code, document_type_name, short_description, document_for, issue_date_required,
                        expiry_date_required, alert_required, alert_days_before, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :shortDescription, :documentFor, :issueDateRequired,
                        :expiryDateRequired, :alertRequired, :alertDaysBefore, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case EDUCATION_LEVELS -> """
                    INSERT INTO master_data.education_levels(
                        id, education_level_code, education_level_name, ranking_order, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :rankingOrder, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case CERTIFICATION_TYPES -> """
                    INSERT INTO master_data.certification_types(
                        id, certification_type_code, certification_type_name, expiry_tracking_required, issuing_body_required, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :expiryTrackingRequired, :issuingBodyRequired, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SKILL_CATEGORIES -> """
                    INSERT INTO master_data.skill_categories(
                        id, skill_category_code, skill_category_name, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SKILLS -> """
                    INSERT INTO master_data.skills(
                        id, skill_code, skill_name, skill_category_id, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :code, :name, :skillCategoryId, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
        };
    }

    private String updateSql(ReferenceResource resource) {
        return switch (resource) {
            case COUNTRIES -> """
                    UPDATE master_data.countries SET
                        country_code = :code,
                        country_name = :name,
                        short_name = :shortName,
                        iso2_code = :iso2Code,
                        iso3_code = :iso3Code,
                        phone_code = :phoneCode,
                        nationality_name = :nationalityName,
                        default_currency_code = :defaultCurrencyCode,
                        default_timezone = :defaultTimezone,
                        native_name = :nativeName,
                        description = :description,
                        gcc_flag = :gccFlag,
                        rtl_enabled = :rtlEnabled,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case CURRENCIES -> """
                    UPDATE master_data.currencies SET
                        currency_code = :code,
                        currency_name = :name,
                        currency_symbol = :currencySymbol,
                        short_description = :shortDescription,
                        description = :description,
                        decimal_places = :decimalPlaces,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case LANGUAGES -> """
                    UPDATE master_data.languages SET
                        language_code = :code,
                        language_name = :name,
                        native_name = :nativeName,
                        short_description = :shortDescription,
                        description = :description,
                        rtl_enabled = :rtlEnabled,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case NATIONALITIES -> """
                    UPDATE master_data.nationalities SET
                        nationality_code = :code,
                        nationality_name = :name,
                        country_code = :countryCode,
                        gcc_national_flag = :gccNationalFlag,
                        omani_flag = :omaniFlag,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case RELIGIONS -> """
                    UPDATE master_data.religions SET
                        religion_code = :code,
                        religion_name = :name,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case GENDERS -> """
                    UPDATE master_data.genders SET
                        gender_code = :code,
                        gender_name = :name,
                        display_order = :displayOrder,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case MARITAL_STATUSES -> """
                    UPDATE master_data.marital_statuses SET
                        marital_status_code = :code,
                        marital_status_name = :name,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case RELATIONSHIP_TYPES -> """
                    UPDATE master_data.relationship_types SET
                        relationship_type_code = :code,
                        relationship_type_name = :name,
                        dependent_allowed = :dependentAllowed,
                        emergency_contact_allowed = :emergencyContactAllowed,
                        beneficiary_allowed = :beneficiaryAllowed,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case DOCUMENT_TYPES -> """
                    UPDATE master_data.document_types SET
                        document_type_code = :code,
                        document_type_name = :name,
                        short_description = :shortDescription,
                        document_for = :documentFor,
                        issue_date_required = :issueDateRequired,
                        expiry_date_required = :expiryDateRequired,
                        alert_required = :alertRequired,
                        alert_days_before = :alertDaysBefore,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case EDUCATION_LEVELS -> """
                    UPDATE master_data.education_levels SET
                        education_level_code = :code,
                        education_level_name = :name,
                        ranking_order = :rankingOrder,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case CERTIFICATION_TYPES -> """
                    UPDATE master_data.certification_types SET
                        certification_type_code = :code,
                        certification_type_name = :name,
                        expiry_tracking_required = :expiryTrackingRequired,
                        issuing_body_required = :issuingBodyRequired,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case SKILL_CATEGORIES -> """
                    UPDATE master_data.skill_categories SET
                        skill_category_code = :code,
                        skill_category_name = :name,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id RETURNING *
                    """;
            case SKILLS -> """
                    UPDATE master_data.skills SET
                        skill_code = :code,
                        skill_name = :name,
                        skill_category_id = :skillCategoryId,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE id = :id
                    RETURNING id,
                              skill_code AS code,
                              skill_name AS name,
                              NULL::VARCHAR AS short_name,
                              NULL::VARCHAR AS iso2_code,
                              NULL::VARCHAR AS iso3_code,
                              NULL::VARCHAR AS phone_code,
                              NULL::VARCHAR AS nationality_name,
                              NULL::VARCHAR AS default_currency_code,
                              NULL::VARCHAR AS default_timezone,
                              NULL::BOOLEAN AS gcc_flag,
                              NULL::VARCHAR AS native_name,
                              NULL::BOOLEAN AS rtl_enabled,
                              NULL::VARCHAR AS country_code,
                              NULL::BOOLEAN AS gcc_national_flag,
                              NULL::BOOLEAN AS omani_flag,
                              NULL::INTEGER AS display_order,
                              NULL::BOOLEAN AS dependent_allowed,
                              NULL::BOOLEAN AS emergency_contact_allowed,
                              NULL::BOOLEAN AS beneficiary_allowed,
                              NULL::VARCHAR AS short_description,
                              NULL::VARCHAR AS document_for,
                              NULL::BOOLEAN AS issue_date_required,
                              NULL::BOOLEAN AS expiry_date_required,
                              NULL::BOOLEAN AS alert_required,
                              NULL::INTEGER AS alert_days_before,
                              NULL::INTEGER AS ranking_order,
                              NULL::BOOLEAN AS expiry_tracking_required,
                              NULL::BOOLEAN AS issuing_body_required,
                              description,
                              skill_category_id,
                              NULL::VARCHAR AS skill_category_name,
                              active,
                              created_at,
                              updated_at,
                              created_by,
                              updated_by
                    """;
        };
    }

    private ReferenceMasterRow mapRow(io.r2dbc.spi.Row row, ReferenceResource resource) {
        String codeCol = resource.codeColumn();
        String nameCol = resource.nameColumn();
        String shortName = get(row, "short_name", String.class);
        if (shortName == null) {
            shortName = get(row, "currency_symbol", String.class);
        }
        String code = get(row, "code", String.class);
        if (code == null) {
            code = get(row, codeCol, String.class);
        }
        String name = get(row, "name", String.class);
        if (name == null) {
            name = get(row, nameCol, String.class);
        }
        return new ReferenceMasterRow(
                row.get("id", UUID.class),
                code,
                name,
                shortName,
                get(row, "iso2_code", String.class),
                get(row, "iso3_code", String.class),
                get(row, "phone_code", String.class),
                get(row, "nationality_name", String.class),
                get(row, "default_currency_code", String.class),
                get(row, "default_timezone", String.class),
                get(row, "gcc_flag", Boolean.class),
                get(row, "decimal_places", Integer.class),
                get(row, "native_name", String.class),
                get(row, "rtl_enabled", Boolean.class),
                get(row, "country_code", String.class),
                get(row, "gcc_national_flag", Boolean.class),
                get(row, "omani_flag", Boolean.class),
                get(row, "display_order", Integer.class),
                get(row, "dependent_allowed", Boolean.class),
                get(row, "emergency_contact_allowed", Boolean.class),
                get(row, "beneficiary_allowed", Boolean.class),
                get(row, "short_description", String.class),
                get(row, "document_for", String.class),
                get(row, "issue_date_required", Boolean.class),
                get(row, "expiry_date_required", Boolean.class),
                get(row, "alert_required", Boolean.class),
                get(row, "alert_days_before", Integer.class),
                get(row, "ranking_order", Integer.class),
                get(row, "expiry_tracking_required", Boolean.class),
                get(row, "issuing_body_required", Boolean.class),
                get(row, "description", String.class),
                get(row, "skill_category_id", UUID.class),
                get(row, "skill_category_name", String.class),
                Boolean.TRUE.equals(get(row, "active", Boolean.class)),
                get(row, "created_at", Instant.class),
                get(row, "updated_at", Instant.class),
                get(row, "created_by", String.class),
                get(row, "updated_by", String.class));
    }

    private <T> T get(io.r2dbc.spi.Row row, String column, Class<T> type) {
        try {
            return row.get(column, type);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private String valueOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveCode(ReferenceResource resource, ReferenceMasterUpsertRequest request) {
        if (resource == ReferenceResource.COUNTRIES && StringUtils.hasText(request.countryCode())) {
            return request.countryCode().trim();
        }
        return request.code().trim();
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
