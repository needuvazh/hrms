package com.company.hrms.employee.service;

import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.contracts.employee.UpdateEmployeeCommandDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Employee Module API interface.
 * Defines all employee-related operations.
 */
public interface EmployeeModuleApi {

    // ============ Employee CRUD Operations ============
    
    /**
     * Create new employee.
     */
    Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command);
    
    /**
     * Get employee by ID.
     */
    Mono<EmployeeViewDto> getEmployee(UUID employeeId);
    
    /**
     * Get employee by code.
     */
    Mono<EmployeeViewDto> getEmployeeByCode(String employeeCode);
    
    /**
     * Get all employees with pagination.
     */
    Flux<EmployeeViewDto> getAllEmployees(String tenantId, Pageable pageable);
    
    /**
     * Update employee information.
     */
    Mono<EmployeeViewDto> updateEmployee(
            UUID employeeId,
            UpdateEmployeeCommandDto command);
    
    /**
     * Delete employee (soft delete).
     */
    Mono<Void> deleteEmployee(UUID employeeId);

    // ============ Employee Search & Filtering ============
    
    /**
     * Search employees by name or code.
     */
    Flux<EmployeeViewDto> searchEmployees(
            String tenantId,
            String searchTerm,
            Pageable pageable);
    
    /**
     * Get employees by department.
     */
    Flux<EmployeeViewDto> getEmployeesByDepartment(UUID departmentId);
    
    /**
     * Get employees by reporting manager.
     */
    Flux<EmployeeViewDto> getTeamMembers(UUID managerId);
    
    /**
     * Get employees by employment status.
     */
    Flux<EmployeeViewDto> getEmployeesByStatus(
            String tenantId,
            String status);
    
    /**
     * Get employees by employment type.
     */
    Flux<EmployeeViewDto> getEmployeesByType(
            String tenantId,
            String employmentType);

    // ============ Oman Compliance Operations ============
    
    /**
     * Get Omani employees for Omanisation tracking.
     */
    Flux<EmployeeViewDto> getOmaniEmployees(String tenantId);
    
    /**
     * Get expatriate employees.
     */
    Flux<EmployeeViewDto> getExpatriateEmployees(String tenantId);
    
    /**
     * Get employees with expiring documents.
     * Used for compliance alerts and renewal tracking.
     */
    Flux<EmployeeViewDto> getEmployeesWithExpiringDocuments(
            String tenantId,
            LocalDate beforeDate);
    
    /**
     * Get Omanisation statistics.
     */
    Mono<OmanisationStatisticsDto> getOmanisationStatistics(String tenantId);

    // ============ Document Management ============
    
    /**
     * Upload employee document (passport, visa, labour card, etc.).
     */
    Mono<EmployeeDocumentDto> uploadDocument(
            UUID employeeId,
            String documentType,
            String fileUrl,
            LocalDate expiryDate);
    
    /**
     * Get employee documents.
     */
    Flux<EmployeeDocumentDto> getEmployeeDocuments(UUID employeeId);
    
    /**
     * Delete employee document.
     */
    Mono<Void> deleteDocument(UUID documentId);

    // ============ Dependent Management ============
    
    /**
     * Add employee dependent.
     */
    Mono<EmployeeDependentDto> addDependent(
            UUID employeeId,
            AddDependentCommandDto command);
    
    /**
     * Get employee dependents.
     */
    Flux<EmployeeDependentDto> getEmployeeDependents(UUID employeeId);
    
    /**
     * Update dependent information.
     */
    Mono<EmployeeDependentDto> updateDependent(
            UUID dependentId,
            UpdateDependentCommandDto command);
    
    /**
     * Delete dependent.
     */
    Mono<Void> deleteDependent(UUID dependentId);

    // ============ Employment History ============
    
    /**
     * Get employee employment history.
     */
    Flux<EmploymentHistoryDto> getEmploymentHistory(UUID employeeId);
    
    /**
     * Add employment history record (promotion, transfer, etc.).
     */
    Mono<EmploymentHistoryDto> addEmploymentHistory(
            UUID employeeId,
            AddEmploymentHistoryCommandDto command);

    // ============ Bank & Payment Details ============
    
    /**
     * Update employee bank details.
     */
    Mono<EmployeeViewDto> updateBankDetails(
            UUID employeeId,
            UpdateBankDetailsCommandDto command);

    // ============ Statistics & Reporting ============
    
    /**
     * Get employee count by status.
     */
    Mono<Long> getEmployeeCountByStatus(String tenantId, String status);
    
    /**
     * Get employee statistics.
     */
    Mono<EmployeeStatisticsDto> getEmployeeStatistics(String tenantId);
}
