package com.company.hrms.leave.infrastructure.persistence;

import com.company.hrms.leave.domain.LeaveBalance;
import com.company.hrms.leave.domain.LeaveRepository;
import com.company.hrms.leave.domain.LeaveRequest;
import com.company.hrms.leave.domain.LeaveStatus;
import com.company.hrms.leave.domain.LeaveType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LeaveR2dbcRepository implements LeaveRepository {

    private final DatabaseClient databaseClient;

    public LeaveR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<LeaveType> saveLeaveType(LeaveType leaveType) {
        return databaseClient.sql("""
                        INSERT INTO leave.leave_types(
                            id, tenant_id, leave_code, leave_name, is_paid, annual_limit_days, is_active, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :leaveCode, :leaveName, :isPaid, :annualLimitDays, :isActive, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, leave_code, leave_name, is_paid, annual_limit_days, is_active, created_at, updated_at
                        """)
                .bind("id", leaveType.id())
                .bind("tenantId", leaveType.tenantId())
                .bind("leaveCode", leaveType.leaveCode())
                .bind("leaveName", leaveType.name())
                .bind("isPaid", leaveType.paid())
                .bind("annualLimitDays", leaveType.annualLimitDays())
                .bind("isActive", leaveType.active())
                .bind("createdAt", leaveType.createdAt())
                .bind("updatedAt", leaveType.updatedAt())
                .map((row, metadata) -> mapLeaveType(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("leave_code", String.class),
                        row.get("leave_name", String.class),
                        row.get("is_paid", Boolean.class),
                        row.get("annual_limit_days", Integer.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveType> findLeaveTypeById(String tenantId, UUID leaveTypeId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, leave_code, leave_name, is_paid, annual_limit_days, is_active, created_at, updated_at
                        FROM leave.leave_types
                        WHERE tenant_id = :tenantId
                          AND id = :id
                          AND is_active = true
                        """)
                .bind("tenantId", tenantId)
                .bind("id", leaveTypeId)
                .map((row, metadata) -> mapLeaveType(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("leave_code", String.class),
                        row.get("leave_name", String.class),
                        row.get("is_paid", Boolean.class),
                        row.get("annual_limit_days", Integer.class),
                        row.get("is_active", Boolean.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveBalance> saveLeaveBalance(LeaveBalance leaveBalance) {
        return databaseClient.sql("""
                        INSERT INTO leave.leave_balances(
                            id, tenant_id, employee_id, leave_type_id, leave_year, total_days, used_days, remaining_days, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :employeeId, :leaveTypeId, :leaveYear, :totalDays, :usedDays, :remainingDays, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, employee_id, leave_type_id, leave_year, total_days, used_days, remaining_days, created_at, updated_at
                        """)
                .bind("id", leaveBalance.id())
                .bind("tenantId", leaveBalance.tenantId())
                .bind("employeeId", leaveBalance.employeeId())
                .bind("leaveTypeId", leaveBalance.leaveTypeId())
                .bind("leaveYear", leaveBalance.leaveYear())
                .bind("totalDays", leaveBalance.totalDays())
                .bind("usedDays", leaveBalance.usedDays())
                .bind("remainingDays", leaveBalance.remainingDays())
                .bind("createdAt", leaveBalance.createdAt())
                .bind("updatedAt", leaveBalance.updatedAt())
                .map((row, metadata) -> mapLeaveBalance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("leave_year", Integer.class),
                        row.get("total_days", Integer.class),
                        row.get("used_days", Integer.class),
                        row.get("remaining_days", Integer.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveBalance> updateLeaveBalance(LeaveBalance leaveBalance) {
        return databaseClient.sql("""
                        UPDATE leave.leave_balances
                        SET used_days = :usedDays,
                            remaining_days = :remainingDays,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, employee_id, leave_type_id, leave_year, total_days, used_days, remaining_days, created_at, updated_at
                        """)
                .bind("id", leaveBalance.id())
                .bind("tenantId", leaveBalance.tenantId())
                .bind("usedDays", leaveBalance.usedDays())
                .bind("remainingDays", leaveBalance.remainingDays())
                .bind("updatedAt", leaveBalance.updatedAt())
                .map((row, metadata) -> mapLeaveBalance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("leave_year", Integer.class),
                        row.get("total_days", Integer.class),
                        row.get("used_days", Integer.class),
                        row.get("remaining_days", Integer.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveBalance> findLeaveBalance(String tenantId, UUID employeeId, UUID leaveTypeId, int leaveYear) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, leave_type_id, leave_year, total_days, used_days, remaining_days, created_at, updated_at
                        FROM leave.leave_balances
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND leave_type_id = :leaveTypeId
                          AND leave_year = :leaveYear
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("leaveTypeId", leaveTypeId)
                .bind("leaveYear", leaveYear)
                .map((row, metadata) -> mapLeaveBalance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("leave_year", Integer.class),
                        row.get("total_days", Integer.class),
                        row.get("used_days", Integer.class),
                        row.get("remaining_days", Integer.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<LeaveBalance> findLeaveBalances(String tenantId, UUID employeeId, int leaveYear) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, leave_type_id, leave_year, total_days, used_days, remaining_days, created_at, updated_at
                        FROM leave.leave_balances
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND leave_year = :leaveYear
                        ORDER BY created_at ASC
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("leaveYear", leaveYear)
                .map((row, metadata) -> mapLeaveBalance(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("leave_year", Integer.class),
                        row.get("total_days", Integer.class),
                        row.get("used_days", Integer.class),
                        row.get("remaining_days", Integer.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    @Override
    public Mono<LeaveRequest> saveLeaveRequest(LeaveRequest leaveRequest) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO leave.leave_requests(
                            id, tenant_id, employee_id, leave_type_id, from_date, to_date, requested_days,
                            reason, leave_status, workflow_instance_id, requested_by, reviewed_by, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :employeeId, :leaveTypeId, :fromDate, :toDate, :requestedDays,
                            :reason, :leaveStatus, :workflowInstanceId, :requestedBy, :reviewedBy, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, employee_id, leave_type_id, from_date, to_date, requested_days,
                                  reason, leave_status, workflow_instance_id, requested_by, reviewed_by, created_at, updated_at
                        """)
                .bind("id", leaveRequest.id())
                .bind("tenantId", leaveRequest.tenantId())
                .bind("employeeId", leaveRequest.employeeId())
                .bind("leaveTypeId", leaveRequest.leaveTypeId())
                .bind("fromDate", leaveRequest.fromDate())
                .bind("toDate", leaveRequest.toDate())
                .bind("requestedDays", leaveRequest.requestedDays())
                .bind("reason", leaveRequest.reason())
                .bind("leaveStatus", leaveRequest.status().name())
                .bind("requestedBy", leaveRequest.requestedBy())
                .bind("createdAt", leaveRequest.createdAt())
                .bind("updatedAt", leaveRequest.updatedAt());

        spec = bindNullable(spec, "workflowInstanceId", leaveRequest.workflowInstanceId(), UUID.class);
        spec = bindNullable(spec, "reviewedBy", leaveRequest.reviewedBy(), String.class);

        return spec
                .map((row, metadata) -> mapLeaveRequest(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("from_date", LocalDate.class),
                        row.get("to_date", LocalDate.class),
                        row.get("requested_days", Integer.class),
                        row.get("reason", String.class),
                        row.get("leave_status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("requested_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveRequest> updateLeaveRequest(LeaveRequest leaveRequest) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE leave.leave_requests
                        SET leave_status = :leaveStatus,
                            workflow_instance_id = :workflowInstanceId,
                            reviewed_by = :reviewedBy,
                            updated_at = :updatedAt
                        WHERE id = :id
                          AND tenant_id = :tenantId
                        RETURNING id, tenant_id, employee_id, leave_type_id, from_date, to_date, requested_days,
                                  reason, leave_status, workflow_instance_id, requested_by, reviewed_by, created_at, updated_at
                        """)
                .bind("id", leaveRequest.id())
                .bind("tenantId", leaveRequest.tenantId())
                .bind("leaveStatus", leaveRequest.status().name())
                .bind("updatedAt", leaveRequest.updatedAt());

        spec = bindNullable(spec, "workflowInstanceId", leaveRequest.workflowInstanceId(), UUID.class);
        spec = bindNullable(spec, "reviewedBy", leaveRequest.reviewedBy(), String.class);

        return spec
                .map((row, metadata) -> mapLeaveRequest(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("from_date", LocalDate.class),
                        row.get("to_date", LocalDate.class),
                        row.get("requested_days", Integer.class),
                        row.get("reason", String.class),
                        row.get("leave_status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("requested_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<LeaveRequest> findLeaveRequestById(String tenantId, UUID leaveRequestId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, leave_type_id, from_date, to_date, requested_days,
                               reason, leave_status, workflow_instance_id, requested_by, reviewed_by, created_at, updated_at
                        FROM leave.leave_requests
                        WHERE tenant_id = :tenantId
                          AND id = :id
                        """)
                .bind("tenantId", tenantId)
                .bind("id", leaveRequestId)
                .map((row, metadata) -> mapLeaveRequest(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("from_date", LocalDate.class),
                        row.get("to_date", LocalDate.class),
                        row.get("requested_days", Integer.class),
                        row.get("reason", String.class),
                        row.get("leave_status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("requested_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<LeaveRequest> findLeaveRequests(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, employee_id, leave_type_id, from_date, to_date, requested_days,
                               reason, leave_status, workflow_instance_id, requested_by, reviewed_by, created_at, updated_at
                        FROM leave.leave_requests
                        WHERE tenant_id = :tenantId
                          AND employee_id = :employeeId
                          AND from_date >= :fromDate
                          AND to_date <= :toDate
                        ORDER BY created_at DESC
                        """)
                .bind("tenantId", tenantId)
                .bind("employeeId", employeeId)
                .bind("fromDate", fromDate)
                .bind("toDate", toDate)
                .map((row, metadata) -> mapLeaveRequest(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("employee_id", UUID.class),
                        row.get("leave_type_id", UUID.class),
                        row.get("from_date", LocalDate.class),
                        row.get("to_date", LocalDate.class),
                        row.get("requested_days", Integer.class),
                        row.get("reason", String.class),
                        row.get("leave_status", String.class),
                        row.get("workflow_instance_id", UUID.class),
                        row.get("requested_by", String.class),
                        row.get("reviewed_by", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private LeaveType mapLeaveType(
            UUID id,
            String tenantId,
            String leaveCode,
            String leaveName,
            Boolean paid,
            Integer annualLimitDays,
            Boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new LeaveType(id, tenantId, leaveCode, leaveName, Boolean.TRUE.equals(paid), annualLimitDays, Boolean.TRUE.equals(active), createdAt, updatedAt);
    }

    private LeaveBalance mapLeaveBalance(
            UUID id,
            String tenantId,
            UUID employeeId,
            UUID leaveTypeId,
            Integer leaveYear,
            Integer totalDays,
            Integer usedDays,
            Integer remainingDays,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new LeaveBalance(id, tenantId, employeeId, leaveTypeId, leaveYear, totalDays, usedDays, remainingDays, createdAt, updatedAt);
    }

    private LeaveRequest mapLeaveRequest(
            UUID id,
            String tenantId,
            UUID employeeId,
            UUID leaveTypeId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer requestedDays,
            String reason,
            String leaveStatus,
            UUID workflowInstanceId,
            String requestedBy,
            String reviewedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new LeaveRequest(
                id,
                tenantId,
                employeeId,
                leaveTypeId,
                fromDate,
                toDate,
                requestedDays,
                reason,
                LeaveStatus.valueOf(leaveStatus),
                workflowInstanceId,
                requestedBy,
                reviewedBy,
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
