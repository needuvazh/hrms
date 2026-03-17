package com.company.hrms.hrlifecycle.repository;

import com.company.hrms.hrlifecycle.model.HrLifecycleModels;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class HrLifecycleR2dbcRepository implements HrLifecycleRepository {

    private final DatabaseClient databaseClient;

    public HrLifecycleR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> create(
            String tenantId,
            HrLifecycleModels.Resource resource,
            HrLifecycleModels.MasterUpsertRequest request,
            String actor
    ) {
        UUID id = UUID.randomUUID();
        GenericExecuteSpec spec = databaseClient.sql(insertSql(resource))
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", trimOrEmpty(request.code()))
                .bind("name", trimOrEmpty(request.name()))
                .bind("active", request.active() == null || request.active())
                .bind("createdBy", actor)
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> update(
            String tenantId,
            HrLifecycleModels.Resource resource,
            UUID id,
            HrLifecycleModels.MasterUpsertRequest request,
            String actor
    ) {
        GenericExecuteSpec spec = databaseClient.sql(updateSql(resource))
                .bind("id", id)
                .bind("tenantId", tenantId)
                .bind("code", trimOrEmpty(request.code()))
                .bind("name", trimOrEmpty(request.name()))
                .bind("active", request.active() == null || request.active())
                .bind("updatedBy", actor);
        spec = bindResourceFields(spec, resource, request);
        return spec.map((row, metadata) -> mapRow(row, resource)).one();
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> get(String tenantId, HrLifecycleModels.Resource resource, UUID id) {
        String sql = "SELECT t.* FROM " + resource.table() + " t WHERE t.tenant_id = :tenantId AND t.id = :id";
        return databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<HrLifecycleModels.MasterViewDto> list(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT t.* FROM ").append(resource.table()).append(" t WHERE t.tenant_id = :tenantId");
        String like = "%";
        if (StringUtils.hasText(query.q())) {
            like = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(cast(t.").append(resource.codeColumn()).append(" as text)) LIKE lower(:q) OR lower(cast(t.")
                    .append(resource.nameColumn()).append(" as text)) LIKE lower(:q))");
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
            spec = spec.bind("q", like);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        spec = bindResourceFilters(spec, resource, query);
        return spec.map((row, metadata) -> mapRow(row, resource)).all();
    }

    @Override
    public Mono<Long> count(String tenantId, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT count(*) AS cnt FROM ").append(resource.table()).append(" t WHERE t.tenant_id = :tenantId");
        String like = "%";
        if (StringUtils.hasText(query.q())) {
            like = "%" + query.q().trim() + "%";
            sql.append(" AND (lower(cast(t.").append(resource.codeColumn()).append(" as text)) LIKE lower(:q) OR lower(cast(t.")
                    .append(resource.nameColumn()).append(" as text)) LIKE lower(:q))");
        }
        if (query.active() != null) {
            sql.append(" AND t.active = :active");
        }
        applyResourceFilters(sql, resource, query);

        GenericExecuteSpec spec = databaseClient.sql(sql.toString()).bind("tenantId", tenantId);
        if (StringUtils.hasText(query.q())) {
            spec = spec.bind("q", like);
        }
        if (query.active() != null) {
            spec = spec.bind("active", query.active());
        }
        spec = bindResourceFilters(spec, resource, query);
        return spec.map((row, metadata) -> row.get("cnt", Long.class) == null ? 0L : row.get("cnt", Long.class)).one();
    }

    @Override
    public Mono<HrLifecycleModels.MasterViewDto> updateStatus(
            String tenantId,
            HrLifecycleModels.Resource resource,
            UUID id,
            boolean active,
            String actor
    ) {
        String sql = "UPDATE " + resource.table()
                + " SET active = :active, updated_at = CURRENT_TIMESTAMP, updated_by = :updatedBy"
                + " WHERE tenant_id = :tenantId AND id = :id RETURNING *";
        return databaseClient.sql(sql)
                .bind("active", active)
                .bind("updatedBy", actor)
                .bind("tenantId", tenantId)
                .bind("id", id)
                .map((row, metadata) -> mapRow(row, resource))
                .one();
    }

    @Override
    public Flux<HrLifecycleModels.OptionViewDto> options(
            String tenantId,
            HrLifecycleModels.Resource resource,
            String q,
            int limit,
            boolean activeOnly
    ) {
        String sql = "SELECT id, cast(" + resource.codeColumn() + " as text) AS code, cast(" + resource.nameColumn() + " as text) AS name"
                + " FROM " + resource.table() + " t WHERE t.tenant_id = :tenantId"
                + (activeOnly ? " AND t.active = TRUE" : "")
                + (StringUtils.hasText(q)
                ? " AND (lower(cast(t." + resource.codeColumn() + " as text)) LIKE lower(:q) OR lower(cast(t." + resource.nameColumn() + " as text)) LIKE lower(:q))"
                : "")
                + " ORDER BY t." + resource.nameColumn() + " ASC LIMIT :limit";
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("limit", limit);
        if (StringUtils.hasText(q)) {
            spec = spec.bind("q", "%" + q.trim() + "%");
        }
        return spec.map((row, metadata) -> new HrLifecycleModels.OptionViewDto(
                        row.get("id", UUID.class),
                        row.get("code", String.class),
                        row.get("name", String.class)))
                .all();
    }

    @Override
    public Mono<Boolean> codeExists(String tenantId, HrLifecycleModels.Resource resource, String code, UUID excludeId) {
        String sql = "SELECT count(*) AS cnt FROM " + resource.table() + " WHERE tenant_id = :tenantId"
                + " AND lower(cast(" + resource.codeColumn() + " as text)) = lower(:code)"
                + (excludeId == null ? "" : " AND id <> :excludeId");
        GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tenantId", tenantId)
                .bind("code", code);
        if (excludeId != null) {
            spec = spec.bind("excludeId", excludeId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    @Override
    public Mono<Boolean> existsReferenceCode(String tenantId, String tableName, String codeColumn, String code, boolean tenantOwned) {
        String sql = "SELECT count(*) AS cnt FROM " + tableName + " WHERE lower(cast(" + codeColumn + " as text)) = lower(:code)"
                + (tenantOwned ? " AND tenant_id = :tenantId" : "");
        GenericExecuteSpec spec = databaseClient.sql(sql).bind("code", code);
        if (tenantOwned) {
            spec = spec.bind("tenantId", tenantId);
        }
        return spec.map((row, metadata) -> row.get("cnt", Long.class) != null && row.get("cnt", Long.class) > 0).one();
    }

    private String resolveSort(HrLifecycleModels.Resource resource, String sort) {
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

    private void applyResourceFilters(StringBuilder sql, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query) {
        switch (resource) {
            case HOLIDAY_CALENDARS -> {
                if (StringUtils.hasText(query.countryCode())) sql.append(" AND lower(t.country_code) = lower(:countryCode)");
                if (query.calendarYear() != null) sql.append(" AND t.calendar_year = :calendarYear");
                if (StringUtils.hasText(query.calendarType())) sql.append(" AND t.calendar_type = :calendarType");
                if (query.hijriEnabledFlag() != null) sql.append(" AND t.hijri_enabled_flag = :hijriEnabledFlag");
                if (query.weekendAdjustmentFlag() != null) sql.append(" AND t.weekend_adjustment_flag = :weekendAdjustmentFlag");
            }
            case LEAVE_TYPES -> {
                if (StringUtils.hasText(query.leaveCategory())) sql.append(" AND t.leave_category = :leaveCategory");
                if (query.paidFlag() != null) sql.append(" AND t.paid_flag = :paidFlag");
                if (query.supportingDocumentRequiredFlag() != null) sql.append(" AND t.supporting_document_required_flag = :supportingDocumentRequiredFlag");
                if (StringUtils.hasText(query.genderApplicability())) sql.append(" AND lower(t.gender_applicability) = lower(:genderApplicability)");
                if (StringUtils.hasText(query.religionApplicability())) sql.append(" AND lower(t.religion_applicability) = lower(:religionApplicability)");
                if (StringUtils.hasText(query.nationalisationApplicability())) sql.append(" AND lower(t.nationalisation_applicability) = lower(:nationalisationApplicability)");
            }
            case SHIFTS -> {
                if (StringUtils.hasText(query.shiftType())) sql.append(" AND t.shift_type = :shiftType");
                if (query.overnightFlag() != null) sql.append(" AND t.overnight_flag = :overnightFlag");
            }
            case ATTENDANCE_SOURCES -> {
                if (StringUtils.hasText(query.sourceType())) sql.append(" AND t.source_type = :sourceType");
                if (query.trustedSourceFlag() != null) sql.append(" AND t.trusted_source_flag = :trustedSourceFlag");
                if (query.manualOverrideFlag() != null) sql.append(" AND t.manual_override_flag = :manualOverrideFlag");
            }
            case ONBOARDING_TASK_TYPES, OFFBOARDING_TASK_TYPES -> {
                if (StringUtils.hasText(query.assigneeType())) sql.append(" AND t.assignee_type = :assigneeType");
                if (query.mandatoryFlag() != null) sql.append(" AND t.mandatory_flag = :mandatoryFlag");
                if (StringUtils.hasText(query.taskCategory())) sql.append(" AND lower(t.task_category) = lower(:taskCategory)");
            }
            case EVENT_TYPES -> {
                if (StringUtils.hasText(query.eventGroup())) sql.append(" AND t.event_group = :eventGroup");
            }
            case EMPLOYEE_STATUSES -> {
                if (query.employmentActiveFlag() != null) sql.append(" AND t.employment_active_flag = :employmentActiveFlag");
                if (query.selfServiceAccessFlag() != null) sql.append(" AND t.self_service_access_flag = :selfServiceAccessFlag");
            }
            case EMPLOYMENT_LIFECYCLE_STAGES -> {
                if (query.entryStageFlag() != null) sql.append(" AND t.entry_stage_flag = :entryStageFlag");
                if (query.exitStageFlag() != null) sql.append(" AND t.exit_stage_flag = :exitStageFlag");
            }
        }
    }

    private GenericExecuteSpec bindResourceFilters(GenericExecuteSpec spec, HrLifecycleModels.Resource resource, HrLifecycleModels.SearchQuery query) {
        switch (resource) {
            case HOLIDAY_CALENDARS -> {
                if (StringUtils.hasText(query.countryCode())) spec = spec.bind("countryCode", query.countryCode().trim());
                if (query.calendarYear() != null) spec = spec.bind("calendarYear", query.calendarYear());
                if (StringUtils.hasText(query.calendarType())) spec = spec.bind("calendarType", query.calendarType().trim().toUpperCase());
                if (query.hijriEnabledFlag() != null) spec = spec.bind("hijriEnabledFlag", query.hijriEnabledFlag());
                if (query.weekendAdjustmentFlag() != null) spec = spec.bind("weekendAdjustmentFlag", query.weekendAdjustmentFlag());
            }
            case LEAVE_TYPES -> {
                if (StringUtils.hasText(query.leaveCategory())) spec = spec.bind("leaveCategory", query.leaveCategory().trim().toUpperCase());
                if (query.paidFlag() != null) spec = spec.bind("paidFlag", query.paidFlag());
                if (query.supportingDocumentRequiredFlag() != null) spec = spec.bind("supportingDocumentRequiredFlag", query.supportingDocumentRequiredFlag());
                if (StringUtils.hasText(query.genderApplicability())) spec = spec.bind("genderApplicability", query.genderApplicability().trim());
                if (StringUtils.hasText(query.religionApplicability())) spec = spec.bind("religionApplicability", query.religionApplicability().trim());
                if (StringUtils.hasText(query.nationalisationApplicability())) {
                    spec = spec.bind("nationalisationApplicability", query.nationalisationApplicability().trim());
                }
            }
            case SHIFTS -> {
                if (StringUtils.hasText(query.shiftType())) spec = spec.bind("shiftType", query.shiftType().trim().toUpperCase());
                if (query.overnightFlag() != null) spec = spec.bind("overnightFlag", query.overnightFlag());
            }
            case ATTENDANCE_SOURCES -> {
                if (StringUtils.hasText(query.sourceType())) spec = spec.bind("sourceType", query.sourceType().trim().toUpperCase());
                if (query.trustedSourceFlag() != null) spec = spec.bind("trustedSourceFlag", query.trustedSourceFlag());
                if (query.manualOverrideFlag() != null) spec = spec.bind("manualOverrideFlag", query.manualOverrideFlag());
            }
            case ONBOARDING_TASK_TYPES, OFFBOARDING_TASK_TYPES -> {
                if (StringUtils.hasText(query.assigneeType())) spec = spec.bind("assigneeType", query.assigneeType().trim().toUpperCase());
                if (query.mandatoryFlag() != null) spec = spec.bind("mandatoryFlag", query.mandatoryFlag());
                if (StringUtils.hasText(query.taskCategory())) spec = spec.bind("taskCategory", query.taskCategory().trim());
            }
            case EVENT_TYPES -> {
                if (StringUtils.hasText(query.eventGroup())) spec = spec.bind("eventGroup", query.eventGroup().trim().toUpperCase());
            }
            case EMPLOYEE_STATUSES -> {
                if (query.employmentActiveFlag() != null) spec = spec.bind("employmentActiveFlag", query.employmentActiveFlag());
                if (query.selfServiceAccessFlag() != null) spec = spec.bind("selfServiceAccessFlag", query.selfServiceAccessFlag());
            }
            case EMPLOYMENT_LIFECYCLE_STAGES -> {
                if (query.entryStageFlag() != null) spec = spec.bind("entryStageFlag", query.entryStageFlag());
                if (query.exitStageFlag() != null) spec = spec.bind("exitStageFlag", query.exitStageFlag());
            }
        }
        return spec;
    }

    private String insertSql(HrLifecycleModels.Resource resource) {
        return switch (resource) {
            case HOLIDAY_CALENDARS -> """
                    INSERT INTO master_data.holiday_calendars(
                        id, tenant_id, holiday_calendar_code, holiday_calendar_name, country_code, calendar_year,
                        calendar_type, hijri_enabled_flag, weekend_adjustment_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :countryCode, :calendarYear,
                        :calendarType, :hijriEnabledFlag, :weekendAdjustmentFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case LEAVE_TYPES -> """
                    INSERT INTO master_data.leave_types(
                        id, tenant_id, leave_type_code, leave_type_name, leave_category, paid_flag,
                        supporting_document_required_flag, gender_applicability, religion_applicability,
                        nationalisation_applicability, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :leaveCategory, :paidFlag,
                        :supportingDocumentRequiredFlag, :genderApplicability, :religionApplicability,
                        :nationalisationApplicability, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case SHIFTS -> """
                    INSERT INTO master_data.shifts(
                        id, tenant_id, shift_code, shift_name, shift_type, start_time, end_time, break_duration_minutes,
                        overnight_flag, grace_in_minutes, grace_out_minutes, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :shiftType, :startTime, :endTime, :breakDurationMinutes,
                        :overnightFlag, :graceInMinutes, :graceOutMinutes, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case ATTENDANCE_SOURCES -> """
                    INSERT INTO master_data.attendance_sources(
                        id, tenant_id, attendance_source_code, attendance_source_name, source_type,
                        trusted_source_flag, manual_override_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :sourceType,
                        :trustedSourceFlag, :manualOverrideFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case ONBOARDING_TASK_TYPES -> """
                    INSERT INTO master_data.onboarding_task_types(
                        id, tenant_id, onboarding_task_type_code, onboarding_task_type_name, task_category,
                        mandatory_flag, assignee_type, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :taskCategory,
                        :mandatoryFlag, :assigneeType, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case OFFBOARDING_TASK_TYPES -> """
                    INSERT INTO master_data.offboarding_task_types(
                        id, tenant_id, offboarding_task_type_code, offboarding_task_type_name, task_category,
                        mandatory_flag, assignee_type, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :taskCategory,
                        :mandatoryFlag, :assigneeType, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case EVENT_TYPES -> """
                    INSERT INTO master_data.event_types(
                        id, tenant_id, event_type_code, event_type_name, event_group, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :eventGroup, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case EMPLOYEE_STATUSES -> """
                    INSERT INTO master_data.employee_statuses(
                        id, tenant_id, employee_status_code, employee_status_name, employment_active_flag,
                        self_service_access_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :employmentActiveFlag,
                        :selfServiceAccessFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
            case EMPLOYMENT_LIFECYCLE_STAGES -> """
                    INSERT INTO master_data.employment_lifecycle_stages(
                        id, tenant_id, lifecycle_stage_code, lifecycle_stage_name, stage_order,
                        entry_stage_flag, exit_stage_flag, description, active, created_by, updated_by
                    ) VALUES (
                        :id, :tenantId, :code, :name, :stageOrder,
                        :entryStageFlag, :exitStageFlag, :description, :active, :createdBy, :updatedBy
                    ) RETURNING *
                    """;
        };
    }

    private String updateSql(HrLifecycleModels.Resource resource) {
        return switch (resource) {
            case HOLIDAY_CALENDARS -> """
                    UPDATE master_data.holiday_calendars SET
                        holiday_calendar_code = :code,
                        holiday_calendar_name = :name,
                        country_code = :countryCode,
                        calendar_year = :calendarYear,
                        calendar_type = :calendarType,
                        hijri_enabled_flag = :hijriEnabledFlag,
                        weekend_adjustment_flag = :weekendAdjustmentFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case LEAVE_TYPES -> """
                    UPDATE master_data.leave_types SET
                        leave_type_code = :code,
                        leave_type_name = :name,
                        leave_category = :leaveCategory,
                        paid_flag = :paidFlag,
                        supporting_document_required_flag = :supportingDocumentRequiredFlag,
                        gender_applicability = :genderApplicability,
                        religion_applicability = :religionApplicability,
                        nationalisation_applicability = :nationalisationApplicability,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case SHIFTS -> """
                    UPDATE master_data.shifts SET
                        shift_code = :code,
                        shift_name = :name,
                        shift_type = :shiftType,
                        start_time = :startTime,
                        end_time = :endTime,
                        break_duration_minutes = :breakDurationMinutes,
                        overnight_flag = :overnightFlag,
                        grace_in_minutes = :graceInMinutes,
                        grace_out_minutes = :graceOutMinutes,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case ATTENDANCE_SOURCES -> """
                    UPDATE master_data.attendance_sources SET
                        attendance_source_code = :code,
                        attendance_source_name = :name,
                        source_type = :sourceType,
                        trusted_source_flag = :trustedSourceFlag,
                        manual_override_flag = :manualOverrideFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case ONBOARDING_TASK_TYPES -> """
                    UPDATE master_data.onboarding_task_types SET
                        onboarding_task_type_code = :code,
                        onboarding_task_type_name = :name,
                        task_category = :taskCategory,
                        mandatory_flag = :mandatoryFlag,
                        assignee_type = :assigneeType,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case OFFBOARDING_TASK_TYPES -> """
                    UPDATE master_data.offboarding_task_types SET
                        offboarding_task_type_code = :code,
                        offboarding_task_type_name = :name,
                        task_category = :taskCategory,
                        mandatory_flag = :mandatoryFlag,
                        assignee_type = :assigneeType,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case EVENT_TYPES -> """
                    UPDATE master_data.event_types SET
                        event_type_code = :code,
                        event_type_name = :name,
                        event_group = :eventGroup,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case EMPLOYEE_STATUSES -> """
                    UPDATE master_data.employee_statuses SET
                        employee_status_code = :code,
                        employee_status_name = :name,
                        employment_active_flag = :employmentActiveFlag,
                        self_service_access_flag = :selfServiceAccessFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
            case EMPLOYMENT_LIFECYCLE_STAGES -> """
                    UPDATE master_data.employment_lifecycle_stages SET
                        lifecycle_stage_code = :code,
                        lifecycle_stage_name = :name,
                        stage_order = :stageOrder,
                        entry_stage_flag = :entryStageFlag,
                        exit_stage_flag = :exitStageFlag,
                        description = :description,
                        active = :active,
                        updated_at = CURRENT_TIMESTAMP,
                        updated_by = :updatedBy
                    WHERE tenant_id = :tenantId AND id = :id RETURNING *
                    """;
        };
    }

    private GenericExecuteSpec bindResourceFields(GenericExecuteSpec spec, HrLifecycleModels.Resource resource, HrLifecycleModels.MasterUpsertRequest request) {
        return switch (resource) {
            case HOLIDAY_CALENDARS -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(spec, "countryCode", valueOrNull(request.countryCode()), String.class),
                                            "calendarYear",
                                            request.calendarYear(),
                                            Integer.class),
                                    "calendarType",
                                    valueOrNull(request.calendarType()),
                                    String.class),
                            "hijriEnabledFlag",
                            request.hijriEnabledFlag(),
                            Boolean.class),
                    "weekendAdjustmentFlag",
                    request.weekendAdjustmentFlag(),
                    Boolean.class).bind("description", valueOrNull(request.description()));
            case LEAVE_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(
                                            bindNullable(
                                                    bindNullable(spec, "leaveCategory", valueOrNull(request.leaveCategory()), String.class),
                                                    "paidFlag",
                                                    request.paidFlag(),
                                                    Boolean.class),
                                            "supportingDocumentRequiredFlag",
                                            request.supportingDocumentRequiredFlag(),
                                            Boolean.class),
                                    "genderApplicability",
                                    valueOrNull(request.genderApplicability()),
                                    String.class),
                            "religionApplicability",
                            valueOrNull(request.religionApplicability()),
                            String.class),
                    "nationalisationApplicability",
                    valueOrNull(request.nationalisationApplicability()),
                    String.class).bind("description", valueOrNull(request.description()));
            case SHIFTS -> {
                GenericExecuteSpec shiftSpec = bindNullable(spec, "shiftType", valueOrNull(request.shiftType()), String.class);
                shiftSpec = bindNullable(shiftSpec, "startTime", request.startTime(), LocalTime.class);
                shiftSpec = bindNullable(shiftSpec, "endTime", request.endTime(), LocalTime.class);
                shiftSpec = bindNullable(shiftSpec, "breakDurationMinutes", request.breakDurationMinutes(), Integer.class);
                shiftSpec = bindNullable(shiftSpec, "overnightFlag", request.overnightFlag(), Boolean.class);
                shiftSpec = bindNullable(shiftSpec, "graceInMinutes", request.graceInMinutes(), Integer.class);
                shiftSpec = bindNullable(shiftSpec, "graceOutMinutes", request.graceOutMinutes(), Integer.class);
                yield shiftSpec.bind("description", valueOrNull(request.description()));
            }
            case ATTENDANCE_SOURCES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "sourceType", valueOrNull(request.sourceType()), String.class),
                            "trustedSourceFlag",
                            request.trustedSourceFlag(),
                            Boolean.class),
                    "manualOverrideFlag",
                    request.manualOverrideFlag(),
                    Boolean.class).bind("description", valueOrNull(request.description()));
            case ONBOARDING_TASK_TYPES, OFFBOARDING_TASK_TYPES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "taskCategory", valueOrNull(request.taskCategory()), String.class),
                            "mandatoryFlag",
                            request.mandatoryFlag(),
                            Boolean.class),
                    "assigneeType",
                    valueOrNull(request.assigneeType()),
                    String.class).bind("description", valueOrNull(request.description()));
            case EVENT_TYPES -> bindNullable(
                    bindNullable(spec, "eventGroup", valueOrNull(request.eventGroup()), String.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case EMPLOYEE_STATUSES -> bindNullable(
                    bindNullable(
                            bindNullable(spec, "employmentActiveFlag", request.employmentActiveFlag(), Boolean.class),
                            "selfServiceAccessFlag",
                            request.selfServiceAccessFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
            case EMPLOYMENT_LIFECYCLE_STAGES -> bindNullable(
                    bindNullable(
                            bindNullable(
                                    bindNullable(spec, "stageOrder", request.stageOrder(), Integer.class),
                                    "entryStageFlag",
                                    request.entryStageFlag(),
                                    Boolean.class),
                            "exitStageFlag",
                            request.exitStageFlag(),
                            Boolean.class),
                    "description",
                    valueOrNull(request.description()),
                    String.class);
        };
    }

    private HrLifecycleModels.MasterViewDto mapRow(Row row, HrLifecycleModels.Resource resource) {
        return new HrLifecycleModels.MasterViewDto(
                row.get("id", UUID.class),
                row.get("tenant_id", String.class),
                get(row, resource.codeColumn(), String.class),
                get(row, resource.nameColumn(), String.class),
                get(row, "country_code", String.class),
                get(row, "calendar_year", Integer.class),
                get(row, "calendar_type", String.class),
                get(row, "hijri_enabled_flag", Boolean.class),
                get(row, "weekend_adjustment_flag", Boolean.class),
                get(row, "leave_category", String.class),
                get(row, "paid_flag", Boolean.class),
                get(row, "supporting_document_required_flag", Boolean.class),
                get(row, "gender_applicability", String.class),
                get(row, "religion_applicability", String.class),
                get(row, "nationalisation_applicability", String.class),
                get(row, "shift_type", String.class),
                get(row, "start_time", LocalTime.class),
                get(row, "end_time", LocalTime.class),
                get(row, "break_duration_minutes", Integer.class),
                get(row, "overnight_flag", Boolean.class),
                get(row, "grace_in_minutes", Integer.class),
                get(row, "grace_out_minutes", Integer.class),
                get(row, "source_type", String.class),
                get(row, "trusted_source_flag", Boolean.class),
                get(row, "manual_override_flag", Boolean.class),
                get(row, "task_category", String.class),
                get(row, "mandatory_flag", Boolean.class),
                get(row, "assignee_type", String.class),
                get(row, "event_group", String.class),
                get(row, "employment_active_flag", Boolean.class),
                get(row, "self_service_access_flag", Boolean.class),
                get(row, "stage_order", Integer.class),
                get(row, "entry_stage_flag", Boolean.class),
                get(row, "exit_stage_flag", Boolean.class),
                get(row, "description", String.class),
                Boolean.TRUE.equals(get(row, "active", Boolean.class)),
                get(row, "created_at", Instant.class),
                get(row, "updated_at", Instant.class),
                get(row, "created_by", String.class),
                get(row, "updated_by", String.class));
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

    private String trimOrEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
