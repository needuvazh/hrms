package com.company.hrms.leave.service;

import com.company.hrms.contracts.leave.CreateLeaveApplicationCommandDto;
import com.company.hrms.contracts.leave.ApproveLeaveApplicationCommandDto;
import com.company.hrms.contracts.leave.RejectLeaveApplicationCommandDto;
import com.company.hrms.leave.model.LeaveApplicationViewDto;
import com.company.hrms.leave.model.LeaveBalanceViewDto;
import com.company.hrms.leave.model.LeaveTypeViewDto;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Leave Module API interface.
 * Defines all leave-related operations including:
 * - Leave type management
 * - Leave application workflow
 * - Leave balance tracking
 * - Multi-level approvals
 * - Oman Labour Law compliance
 */
public interface LeaveModuleApi {

    // ============ Leave Type Operations ============
    
    /**
     * Get all active leave types for a tenant.
     */
    Flux<LeaveTypeViewDto> getAllLeaveTypes(String tenantId);
    
    /**
     * Get leave type by ID.
     */
    Mono<LeaveTypeViewDto> getLeaveType(UUID leaveTypeId);

    // ============ Leave Application Operations ============
    
    /**
     * Create new leave application.
     * Validates:
     * - Leave balance availability
     * - Date range validity
     * - Multi-level approval requirement
     * - Oman Labour Law compliance
     */
    Mono<LeaveApplicationViewDto> createLeaveApplication(
            CreateLeaveApplicationCommandDto command);
    
    /**
     * Get leave application by ID.
     */
    Mono<LeaveApplicationViewDto> getLeaveApplication(UUID applicationId);
    
    /**
     * Get all leave applications for an employee.
     */
    Flux<LeaveApplicationViewDto> getEmployeeLeaveApplications(
            UUID employeeId,
            String status);
    
    /**
     * Get pending leave applications for approval.
     * Used in manager portal and HR approval workflows.
     */
    Flux<LeaveApplicationViewDto> getPendingApprovalsForManager(UUID managerId);
    
    /**
     * Approve leave application.
     * Handles multi-level approval workflow.
     */
    Mono<LeaveApplicationViewDto> approveLeaveApplication(
            UUID applicationId,
            ApproveLeaveApplicationCommandDto command);
    
    /**
     * Reject leave application.
     */
    Mono<LeaveApplicationViewDto> rejectLeaveApplication(
            UUID applicationId,
            RejectLeaveApplicationCommandDto command);
    
    /**
     * Cancel leave application.
     * Only allowed in certain statuses.
     */
    Mono<Void> cancelLeaveApplication(UUID applicationId, String reason);

    // ============ Leave Balance Operations ============
    
    /**
     * Get leave balance for an employee.
     */
    Mono<LeaveBalanceViewDto> getLeaveBalance(
            UUID employeeId,
            UUID leaveTypeId,
            Integer financialYear);
    
    /**
     * Get all leave balances for an employee.
     */
    Flux<LeaveBalanceViewDto> getEmployeeLeaveBalances(
            UUID employeeId,
            Integer financialYear);
    
    /**
     * Calculate leave balance after application approval.
     * Used for leave balance impact preview.
     */
    Mono<LeaveBalanceViewDto> calculateLeaveBalanceAfterApplication(
            UUID employeeId,
            UUID leaveTypeId,
            Integer numberOfDays);

    // ============ Leave Calendar & Reporting ============
    
    /**
     * Get team leave calendar.
     * Shows all approved leaves for a department/team.
     */
    Flux<LeaveApplicationViewDto> getTeamLeaveCalendar(
            String tenantId,
            UUID departmentId,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Get workforce availability report.
     * Shows available employees for a date range.
     */
    Mono<WorkforceAvailabilityReportDto> getWorkforceAvailabilityReport(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate);

    // ============ Leave Encashment Operations ============
    
    /**
     * Calculate leave encashment amount.
     * Based on basic salary and leave days.
     */
    Mono<LeaveEncashmentCalculationDto> calculateLeaveEncashment(
            UUID employeeId,
            UUID leaveTypeId,
            Integer numberOfDays);
    
    /**
     * Process leave encashment.
     * Called during resignation/termination.
     */
    Mono<Void> processLeaveEncashment(
            UUID employeeId,
            UUID leaveTypeId,
            Integer numberOfDays);

    // ============ Air Ticket Entitlement (Expatriates) ============
    
    /**
     * Get air ticket entitlement for expatriate employee.
     * Based on employment contract terms.
     */
    Mono<AirTicketEntitlementDto> getAirTicketEntitlement(UUID employeeId);
    
    /**
     * Process air ticket encashment.
     * Called during leave application or resignation.
     */
    Mono<Void> processAirTicketEncashment(
            UUID employeeId,
            java.math.BigDecimal amount);

    // ============ Leave Resumption & Overstay ============
    
    /**
     * Record leave resumption.
     * Called when employee returns from leave.
     */
    Mono<Void> recordLeaveResumption(
            UUID applicationId,
            LocalDate resumptionDate);
    
    /**
     * Check for leave overstay.
     * Alerts if employee has not returned from leave.
     */
    Mono<LeaveOverstayAlertDto> checkLeaveOverstay(UUID employeeId);

    // ============ Hijri Calendar Integration ============
    
    /**
     * Get Islamic holidays for a date range.
     * Integrated with official MOM announcements.
     */
    Flux<IslamicHolidayDto> getIslamicHolidays(
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Reconcile Hijri dates with Gregorian dates.
     */
    Mono<HijriDateConversionDto> convertToHijriDate(LocalDate gregorianDate);
}
