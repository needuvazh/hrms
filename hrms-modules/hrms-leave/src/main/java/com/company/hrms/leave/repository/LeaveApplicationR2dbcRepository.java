package com.company.hrms.leave.repository;

import com.company.hrms.leave.model.LeaveApplicationViewDto;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2dbc repository for Leave Application data access.
 * Handles all database operations for leave applications.
 */
@Repository
public class LeaveApplicationR2dbcRepository implements LeaveApplicationRepository {

    private final DatabaseClient databaseClient;

    public LeaveApplicationR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<LeaveApplicationViewDto> findById(UUID id) {
        return databaseClient.sql("""
                SELECT 
                    la.id, la.tenant_id, la.application_number,
                    la.employee_id, e.employee_code, e.first_name, e.last_name,
                    la.department_id, d.department_name,
                    la.reporting_manager_id, m.first_name as manager_first_name, m.last_name as manager_last_name,
                    la.leave_type_id, lt.leave_type_name,
                    la.leave_start_date, la.leave_end_date, la.number_of_days,
                    la.is_half_day, la.half_day_type,
                    la.reason, la.contact_phone_number, la.contact_address, la.alternate_contact_person,
                    la.application_status, la.current_approval_level, la.total_approval_levels,
                    la.last_approved_by, la.last_approved_at, la.rejection_reason,
                    la.balance_before_leave, la.balance_after_leave,
                    la.is_encashment_requested, la.encashment_amount,
                    la.is_air_ticket_encashment_requested, la.air_ticket_encashment_amount,
                    la.attachment_url,
                    la.created_at, la.updated_at, la.submitted_at
                FROM leave_applications la
                JOIN employees e ON la.employee_id = e.id
                LEFT JOIN departments d ON la.department_id = d.id
                LEFT JOIN employees m ON la.reporting_manager_id = m.id
                LEFT JOIN leave_types lt ON la.leave_type_id = lt.id
                WHERE la.id = :id
                """)
                .bind("id", id)
                .map((row, metadata) -> mapLeaveApplicationViewDto(row))
                .one();
    }

    @Override
    public Flux<LeaveApplicationViewDto> findByEmployeeId(UUID employeeId, String status) {
        return databaseClient.sql("""
                SELECT 
                    la.id, la.tenant_id, la.application_number,
                    la.employee_id, e.employee_code, e.first_name, e.last_name,
                    la.department_id, d.department_name,
                    la.reporting_manager_id, m.first_name as manager_first_name, m.last_name as manager_last_name,
                    la.leave_type_id, lt.leave_type_name,
                    la.leave_start_date, la.leave_end_date, la.number_of_days,
                    la.is_half_day, la.half_day_type,
                    la.reason, la.contact_phone_number, la.contact_address, la.alternate_contact_person,
                    la.application_status, la.current_approval_level, la.total_approval_levels,
                    la.last_approved_by, la.last_approved_at, la.rejection_reason,
                    la.balance_before_leave, la.balance_after_leave,
                    la.is_encashment_requested, la.encashment_amount,
                    la.is_air_ticket_encashment_requested, la.air_ticket_encashment_amount,
                    la.attachment_url,
                    la.created_at, la.updated_at, la.submitted_at
                FROM leave_applications la
                JOIN employees e ON la.employee_id = e.id
                LEFT JOIN departments d ON la.department_id = d.id
                LEFT JOIN employees m ON la.reporting_manager_id = m.id
                LEFT JOIN leave_types lt ON la.leave_type_id = lt.id
                WHERE la.employee_id = :employeeId
                AND (:status IS NULL OR la.application_status = :status)
                ORDER BY la.leave_start_date DESC
                """)
                .bind("employeeId", employeeId)
                .bind("status", status)
                .map((row, metadata) -> mapLeaveApplicationViewDto(row))
                .all();
    }

    @Override
    public Flux<LeaveApplicationViewDto> findPendingApprovals(UUID approverId) {
        return databaseClient.sql("""
                SELECT 
                    la.id, la.tenant_id, la.application_number,
                    la.employee_id, e.employee_code, e.first_name, e.last_name,
                    la.department_id, d.department_name,
                    la.reporting_manager_id, m.first_name as manager_first_name, m.last_name as manager_last_name,
                    la.leave_type_id, lt.leave_type_name,
                    la.leave_start_date, la.leave_end_date, la.number_of_days,
                    la.is_half_day, la.half_day_type,
                    la.reason, la.contact_phone_number, la.contact_address, la.alternate_contact_person,
                    la.application_status, la.current_approval_level, la.total_approval_levels,
                    la.last_approved_by, la.last_approved_at, la.rejection_reason,
                    la.balance_before_leave, la.balance_after_leave,
                    la.is_encashment_requested, la.encashment_amount,
                    la.is_air_ticket_encashment_requested, la.air_ticket_encashment_amount,
                    la.attachment_url,
                    la.created_at, la.updated_at, la.submitted_at
                FROM leave_applications la
                JOIN employees e ON la.employee_id = e.id
                LEFT JOIN departments d ON la.department_id = d.id
                LEFT JOIN employees m ON la.reporting_manager_id = m.id
                LEFT JOIN leave_types lt ON la.leave_type_id = lt.id
                JOIN approval_workflows aw ON la.id = aw.leave_application_id
                WHERE aw.current_approver_id = :approverId
                AND la.application_status = 'SUBMITTED'
                ORDER BY la.created_at ASC
                """)
                .bind("approverId", approverId)
                .map((row, metadata) -> mapLeaveApplicationViewDto(row))
                .all();
    }

    @Override
    public Flux<LeaveApplicationViewDto> findByDateRange(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate) {
        return databaseClient.sql("""
                SELECT 
                    la.id, la.tenant_id, la.application_number,
                    la.employee_id, e.employee_code, e.first_name, e.last_name,
                    la.department_id, d.department_name,
                    la.reporting_manager_id, m.first_name as manager_first_name, m.last_name as manager_last_name,
                    la.leave_type_id, lt.leave_type_name,
                    la.leave_start_date, la.leave_end_date, la.number_of_days,
                    la.is_half_day, la.half_day_type,
                    la.reason, la.contact_phone_number, la.contact_address, la.alternate_contact_person,
                    la.application_status, la.current_approval_level, la.total_approval_levels,
                    la.last_approved_by, la.last_approved_at, la.rejection_reason,
                    la.balance_before_leave, la.balance_after_leave,
                    la.is_encashment_requested, la.encashment_amount,
                    la.is_air_ticket_encashment_requested, la.air_ticket_encashment_amount,
                    la.attachment_url,
                    la.created_at, la.updated_at, la.submitted_at
                FROM leave_applications la
                JOIN employees e ON la.employee_id = e.id
                LEFT JOIN departments d ON la.department_id = d.id
                LEFT JOIN employees m ON la.reporting_manager_id = m.id
                LEFT JOIN leave_types lt ON la.leave_type_id = lt.id
                WHERE la.tenant_id = :tenantId
                AND la.application_status = 'APPROVED'
                AND la.leave_start_date <= :endDate
                AND la.leave_end_date >= :startDate
                ORDER BY la.leave_start_date ASC
                """)
                .bind("tenantId", tenantId)
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map((row, metadata) -> mapLeaveApplicationViewDto(row))
                .all();
    }

    @Override
    public Mono<UUID> save(LeaveApplicationViewDto application) {
        UUID id = application.id() != null ? application.id() : java.util.UUID.randomUUID();
        java.time.Instant now = java.time.Instant.now();

        return databaseClient.sql("""
                INSERT INTO leave_applications(
                    id, tenant_id, application_number, employee_id, department_id,
                    reporting_manager_id, leave_type_id, leave_start_date, leave_end_date,
                    number_of_days, is_half_day, half_day_type, reason,
                    contact_phone_number, contact_address, alternate_contact_person,
                    application_status, current_approval_level, total_approval_levels,
                    balance_before_leave, balance_after_leave,
                    is_encashment_requested, encashment_amount,
                    is_air_ticket_encashment_requested, air_ticket_encashment_amount,
                    attachment_url, created_at, updated_at
                )
                VALUES(
                    :id, :tenantId, :applicationNumber, :employeeId, :departmentId,
                    :reportingManagerId, :leaveTypeId, :leaveStartDate, :leaveEndDate,
                    :numberOfDays, :isHalfDay, :halfDayType, :reason,
                    :contactPhoneNumber, :contactAddress, :alternateContactPerson,
                    :applicationStatus, :currentApprovalLevel, :totalApprovalLevels,
                    :balanceBeforeLeave, :balanceAfterLeave,
                    :isEncashmentRequested, :encashmentAmount,
                    :isAirTicketEncashmentRequested, :airTicketEncashmentAmount,
                    :attachmentUrl, :createdAt, :updatedAt
                )
                ON DUPLICATE KEY UPDATE
                    application_status = :applicationStatus,
                    current_approval_level = :currentApprovalLevel,
                    last_approved_by = :lastApprovedBy,
                    last_approved_at = :lastApprovedAt,
                    rejection_reason = :rejectionReason,
                    updated_at = :updatedAt
                """)
                .bind("id", id)
                .bind("tenantId", application.tenantId())
                .bind("applicationNumber", application.applicationNumber())
                .bind("employeeId", application.employeeId())
                .bind("departmentId", application.departmentId())
                .bind("reportingManagerId", application.reportingManagerId())
                .bind("leaveTypeId", application.leaveTypeId())
                .bind("leaveStartDate", application.leaveStartDate())
                .bind("leaveEndDate", application.leaveEndDate())
                .bind("numberOfDays", application.numberOfDays())
                .bind("isHalfDay", application.isHalfDay())
                .bind("halfDayType", application.halfDayType())
                .bind("reason", application.reason())
                .bind("contactPhoneNumber", application.contactPhoneNumber())
                .bind("contactAddress", application.contactAddress())
                .bind("alternateContactPerson", application.alternateContactPerson())
                .bind("applicationStatus", application.applicationStatus())
                .bind("currentApprovalLevel", application.currentApprovalLevel())
                .bind("totalApprovalLevels", application.totalApprovalLevels())
                .bind("lastApprovedBy", application.lastApprovedBy())
                .bind("lastApprovedAt", application.lastApprovedAt())
                .bind("rejectionReason", application.rejectionReason())
                .bind("balanceBeforeLeave", application.balanceBeforeLeave())
                .bind("balanceAfterLeave", application.balanceAfterLeave())
                .bind("isEncashmentRequested", application.isEncashmentRequested())
                .bind("encashmentAmount", application.encashmentAmount())
                .bind("isAirTicketEncashmentRequested", application.isAirTicketEncashmentRequested())
                .bind("airTicketEncashmentAmount", application.airTicketEncashmentAmount())
                .bind("attachmentUrl", application.attachmentUrl())
                .bind("createdAt", application.createdAt() != null ? application.createdAt() : now)
                .bind("updatedAt", now)
                .fetch()
                .rowsUpdated()
                .map(rows -> id);
    }

    private LeaveApplicationViewDto mapLeaveApplicationViewDto(io.r2dbc.spi.Row row) {
        return new LeaveApplicationViewDto(
                row.get("id", java.util.UUID.class),
                row.get("tenant_id", String.class),
                row.get("application_number", String.class),
                row.get("employee_id", java.util.UUID.class),
                row.get("employee_code", String.class),
                row.get("first_name", String.class) + " " + row.get("last_name", String.class),
                row.get("department_id", java.util.UUID.class),
                row.get("department_name", String.class),
                row.get("reporting_manager_id", java.util.UUID.class),
                row.get("manager_first_name", String.class) + " " + row.get("manager_last_name", String.class),
                row.get("leave_type_id", java.util.UUID.class),
                row.get("leave_type_name", String.class),
                row.get("leave_start_date", java.time.LocalDate.class),
                row.get("leave_end_date", java.time.LocalDate.class),
                row.get("number_of_days", Integer.class),
                row.get("is_half_day", Boolean.class),
                row.get("half_day_type", String.class),
                row.get("reason", String.class),
                row.get("contact_phone_number", String.class),
                row.get("contact_address", String.class),
                row.get("alternate_contact_person", String.class),
                row.get("application_status", String.class),
                row.get("current_approval_level", Integer.class),
                row.get("total_approval_levels", Integer.class),
                row.get("last_approved_by", String.class),
                row.get("last_approved_at", java.time.Instant.class),
                row.get("rejection_reason", String.class),
                row.get("balance_before_leave", java.math.BigDecimal.class),
                row.get("balance_after_leave", java.math.BigDecimal.class),
                row.get("is_encashment_requested", Boolean.class),
                row.get("encashment_amount", java.math.BigDecimal.class),
                row.get("is_air_ticket_encashment_requested", Boolean.class),
                row.get("air_ticket_encashment_amount", java.math.BigDecimal.class),
                row.get("attachment_url", String.class),
                row.get("created_at", java.time.Instant.class),
                row.get("updated_at", java.time.Instant.class),
                row.get("submitted_at", java.time.Instant.class)
        );
    }
}
