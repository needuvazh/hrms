package com.company.hrms.leave.repository;

import com.company.hrms.leave.model.LeaveApplicationViewDto;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for Leave Application data access.
 */
public interface LeaveApplicationRepository {
    
    /**
     * Find leave application by ID.
     */
    Mono<LeaveApplicationViewDto> findById(UUID id);
    
    /**
     * Find all leave applications for an employee.
     */
    Flux<LeaveApplicationViewDto> findByEmployeeId(UUID employeeId, String status);
    
    /**
     * Find pending leave applications for approval.
     */
    Flux<LeaveApplicationViewDto> findPendingApprovals(UUID approverId);
    
    /**
     * Find leave applications within a date range.
     * Used for leave calendar and availability reporting.
     */
    Flux<LeaveApplicationViewDto> findByDateRange(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate);
    
    /**
     * Save leave application.
     */
    Mono<UUID> save(LeaveApplicationViewDto application);
}
