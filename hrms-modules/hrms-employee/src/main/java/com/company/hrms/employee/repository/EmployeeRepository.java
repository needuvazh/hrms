package com.company.hrms.employee.repository;

import com.company.hrms.employee.model.EmployeeViewDto;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository interface for Employee data access.
 */
public interface EmployeeRepository {
    
    /**
     * Find employee by ID.
     */
    Mono<EmployeeViewDto> findById(UUID id);
    
    /**
     * Find employee by code.
     */
    Mono<EmployeeViewDto> findByEmployeeCode(String employeeCode);
    
    /**
     * Find all employees for a tenant with pagination.
     */
    Flux<EmployeeViewDto> findAllByTenantId(String tenantId, Pageable pageable);
    
    /**
     * Find employees by department.
     */
    Flux<EmployeeViewDto> findByDepartmentId(UUID departmentId);
    
    /**
     * Find employees by reporting manager.
     */
    Flux<EmployeeViewDto> findByReportingManagerId(UUID managerId);
    
    /**
     * Find employees with expiring documents (passport, visa, labour card).
     */
    Flux<EmployeeViewDto> findEmployeesWithExpiringDocuments(
            String tenantId,
            LocalDate beforeDate);
    
    /**
     * Find employees by employment status.
     */
    Flux<EmployeeViewDto> findByEmploymentStatus(
            String tenantId,
            String status);
    
    /**
     * Search employees by name or code.
     */
    Flux<EmployeeViewDto> searchEmployees(
            String tenantId,
            String searchTerm,
            Pageable pageable);
    
    /**
     * Find employees by employment type.
     */
    Flux<EmployeeViewDto> findByEmploymentType(
            String tenantId,
            String employmentType);
    
    /**
     * Find Omani employees for Omanisation tracking.
     */
    Flux<EmployeeViewDto> findOmaniEmployees(String tenantId);
    
    /**
     * Find expatriate employees.
     */
    Flux<EmployeeViewDto> findExpatriateEmployees(String tenantId);
    
    /**
     * Save employee.
     */
    Mono<UUID> save(EmployeeViewDto employee);
    
    /**
     * Delete employee (soft delete).
     */
    Mono<Void> delete(UUID id);
    
    /**
     * Count employees by status.
     */
    Mono<Long> countByEmploymentStatus(String tenantId, String status);
}
