package com.company.hrms.core.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Employee R2dbc Repository
 */
@Repository
public interface EmployeeR2dbcRepository extends R2dbcRepository<Employee, UUID> {
    @Query("""
        SELECT e.*, c.company_name, d.department_name, jp.position_name
        FROM employees e
        LEFT JOIN companies c ON e.company_id = c.id
        LEFT JOIN departments d ON e.department_id = d.id
        LEFT JOIN job_positions jp ON e.job_position_id = jp.id
        WHERE e.tenant_id = :tenantId
        ORDER BY e.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Employee> findByTenantIdPaginated(String tenantId, int limit, int offset);

    @Query("""
        SELECT e.* FROM employees e
        WHERE e.tenant_id = :tenantId AND e.employee_code = :employeeCode
        """)
    Mono<Employee> findByEmployeeCode(String tenantId, String employeeCode);

    @Query("""
        SELECT e.* FROM employees e
        WHERE e.tenant_id = :tenantId AND e.department_id = :departmentId
        """)
    Flux<Employee> findByDepartmentId(String tenantId, UUID departmentId);

    @Query("""
        SELECT e.* FROM employees e
        WHERE e.tenant_id = :tenantId 
        AND (e.passport_expiry_date <= CURRENT_DATE + INTERVAL '30 days'
        OR e.visa_expiry_date <= CURRENT_DATE + INTERVAL '30 days'
        OR e.labour_card_expiry_date <= CURRENT_DATE + INTERVAL '30 days')
        """)
    Flux<Employee> findEmployeesWithExpiringDocuments(String tenantId);

    @Query("""
        SELECT COUNT(*) FROM employees
        WHERE tenant_id = :tenantId AND employment_status = 'ACTIVE'
        """)
    Mono<Long> countActiveEmployees(String tenantId);
}

/**
 * Leave Application R2dbc Repository
 */
@Repository
public interface LeaveApplicationR2dbcRepository extends R2dbcRepository<LeaveApplication, UUID> {
    @Query("""
        SELECT la.*, lt.leave_type_name, e.employee_name
        FROM leave_applications la
        LEFT JOIN leave_types lt ON la.leave_type_id = lt.id
        LEFT JOIN employees e ON la.employee_id = e.id
        WHERE la.tenant_id = :tenantId
        AND la.application_status IN (:statuses)
        ORDER BY la.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<LeaveApplication> findByStatusPaginated(String tenantId, java.util.List<String> statuses, int limit, int offset);

    @Query("""
        SELECT la.* FROM leave_applications la
        WHERE la.tenant_id = :tenantId 
        AND la.employee_id = :employeeId
        AND la.application_status = 'APPROVED'
        AND la.start_date <= CURRENT_DATE
        AND la.end_date >= CURRENT_DATE
        """)
    Mono<LeaveApplication> findActiveLeaveForEmployee(String tenantId, UUID employeeId);

    @Query("""
        SELECT SUM(number_of_days) FROM leave_applications
        WHERE tenant_id = :tenantId
        AND employee_id = :employeeId
        AND leave_type_id = :leaveTypeId
        AND YEAR(start_date) = YEAR(CURRENT_DATE)
        AND application_status = 'APPROVED'
        """)
    Mono<Double> calculateLeaveBalance(String tenantId, UUID employeeId, UUID leaveTypeId);

    @Query("""
        SELECT la.* FROM leave_applications la
        WHERE la.tenant_id = :tenantId
        AND la.application_status = 'PENDING'
        AND (la.approver_level_1_id = :userId OR la.approver_level_2_id = :userId)
        ORDER BY la.created_at ASC
        """)
    Flux<LeaveApplication> findPendingApprovalsForUser(String tenantId, UUID userId);
}

/**
 * Attendance R2dbc Repository
 */
@Repository
public interface AttendanceR2dbcRepository extends R2dbcRepository<Attendance, UUID> {
    @Query("""
        SELECT a.*, e.employee_name, s.shift_name
        FROM attendance a
        LEFT JOIN employees e ON a.employee_id = e.id
        LEFT JOIN shifts s ON a.shift_id = s.id
        WHERE a.tenant_id = :tenantId
        AND a.attendance_date BETWEEN :startDate AND :endDate
        ORDER BY a.attendance_date DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Attendance> findByDateRangePaginated(String tenantId, java.time.LocalDate startDate, 
                                              java.time.LocalDate endDate, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM attendance
        WHERE tenant_id = :tenantId
        AND employee_id = :employeeId
        AND attendance_date = CURRENT_DATE
        """)
    Mono<Long> checkTodayAttendance(String tenantId, UUID employeeId);

    @Query("""
        SELECT a.* FROM attendance a
        WHERE a.tenant_id = :tenantId
        AND a.employee_id = :employeeId
        AND a.attendance_date = :attendanceDate
        """)
    Mono<Attendance> findByEmployeeAndDate(String tenantId, UUID employeeId, java.time.LocalDate attendanceDate);

    @Query("""
        SELECT a.* FROM attendance a
        WHERE a.tenant_id = :tenantId
        AND a.attendance_status = 'ABSENT'
        AND a.attendance_date BETWEEN :startDate AND :endDate
        ORDER BY a.attendance_date DESC
        """)
    Flux<Attendance> findAbsentRecords(String tenantId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}

/**
 * Payroll R2dbc Repository
 */
@Repository
public interface PayrollR2dbcRepository extends R2dbcRepository<EmployeePayroll, UUID> {
    @Query("""
        SELECT ep.*, e.employee_name, pp.period_code
        FROM employee_payroll ep
        LEFT JOIN employees e ON ep.employee_id = e.id
        LEFT JOIN payroll_periods pp ON ep.payroll_period_id = pp.id
        WHERE ep.tenant_id = :tenantId
        AND ep.payroll_period_id = :payrollPeriodId
        ORDER BY e.employee_code ASC
        LIMIT :limit OFFSET :offset
        """)
    Flux<EmployeePayroll> findByPayrollPeriodPaginated(String tenantId, UUID payrollPeriodId, int limit, int offset);

    @Query("""
        SELECT ep.* FROM employee_payroll ep
        WHERE ep.tenant_id = :tenantId
        AND ep.employee_id = :employeeId
        AND YEAR(ep.created_at) = :year
        AND MONTH(ep.created_at) = :month
        """)
    Mono<EmployeePayroll> findByEmployeeAndMonth(String tenantId, UUID employeeId, int year, int month);

    @Query("""
        SELECT SUM(net_amount) FROM employee_payroll
        WHERE tenant_id = :tenantId
        AND payroll_period_id = :payrollPeriodId
        """)
    Mono<Double> calculateTotalNetAmount(String tenantId, UUID payrollPeriodId);

    @Query("""
        SELECT ep.* FROM employee_payroll ep
        WHERE ep.tenant_id = :tenantId
        AND ep.payment_status = 'PENDING'
        ORDER BY ep.created_at ASC
        """)
    Flux<EmployeePayroll> findPendingPayments(String tenantId);
}

/**
 * Payslip R2dbc Repository
 */
@Repository
public interface PayslipR2dbcRepository extends R2dbcRepository<Payslip, UUID> {
    @Query("""
        SELECT p.*, e.employee_name, pp.period_code
        FROM payslips p
        LEFT JOIN employees e ON p.employee_id = e.id
        LEFT JOIN payroll_periods pp ON p.payroll_period_id = pp.id
        WHERE p.tenant_id = :tenantId
        AND p.employee_id = :employeeId
        ORDER BY p.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Payslip> findByEmployeePaginated(String tenantId, UUID employeeId, int limit, int offset);

    @Query("""
        SELECT p.* FROM payslips p
        WHERE p.tenant_id = :tenantId
        AND p.payslip_status = 'GENERATED'
        AND p.created_at >= CURRENT_DATE - INTERVAL '7 days'
        ORDER BY p.created_at DESC
        """)
    Flux<Payslip> findRecentPayslips(String tenantId);

    @Query("""
        SELECT COUNT(*) FROM payslips
        WHERE tenant_id = :tenantId
        AND payslip_status = 'SENT'
        AND MONTH(created_at) = :month
        """)
    Mono<Long> countSentPayslipsForMonth(String tenantId, int month);
}

/**
 * Resignation R2dbc Repository
 */
@Repository
public interface ResignationR2dbcRepository extends R2dbcRepository<Resignation, UUID> {
    @Query("""
        SELECT r.*, e.employee_name
        FROM resignations r
        LEFT JOIN employees e ON r.employee_id = e.id
        WHERE r.tenant_id = :tenantId
        AND r.resignation_status IN (:statuses)
        ORDER BY r.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Resignation> findByStatusPaginated(String tenantId, java.util.List<String> statuses, int limit, int offset);

    @Query("""
        SELECT r.* FROM resignations r
        WHERE r.tenant_id = :tenantId
        AND r.employee_id = :employeeId
        AND r.resignation_status IN ('SUBMITTED', 'ACCEPTED')
        ORDER BY r.created_at DESC
        LIMIT 1
        """)
    Mono<Resignation> findLatestResignationForEmployee(String tenantId, UUID employeeId);

    @Query("""
        SELECT r.* FROM resignations r
        WHERE r.tenant_id = :tenantId
        AND r.notice_period_end_date <= CURRENT_DATE + INTERVAL '7 days'
        AND r.resignation_status = 'ACCEPTED'
        ORDER BY r.notice_period_end_date ASC
        """)
    Flux<Resignation> findUpcomingSeparations(String tenantId);
}

/**
 * EOSB Calculation R2dbc Repository
 */
@Repository
public interface EosbCalculationR2dbcRepository extends R2dbcRepository<EosbCalculation, UUID> {
    @Query("""
        SELECT ec.*, e.employee_name
        FROM eosb_calculations ec
        LEFT JOIN employees e ON ec.employee_id = e.id
        WHERE ec.tenant_id = :tenantId
        AND ec.eosb_status = :status
        ORDER BY ec.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<EosbCalculation> findByStatusPaginated(String tenantId, String status, int limit, int offset);

    @Query("""
        SELECT ec.* FROM eosb_calculations ec
        WHERE ec.tenant_id = :tenantId
        AND ec.employee_id = :employeeId
        ORDER BY ec.created_at DESC
        LIMIT 1
        """)
    Mono<EosbCalculation> findLatestEosbForEmployee(String tenantId, UUID employeeId);

    @Query("""
        SELECT SUM(eosb_amount) FROM eosb_calculations
        WHERE tenant_id = :tenantId
        AND eosb_status = 'PAID'
        AND YEAR(payment_date) = :year
        """)
    Mono<Double> calculateTotalEosbPaidForYear(String tenantId, int year);
}

/**
 * Job Application R2dbc Repository
 */
@Repository
public interface JobApplicationR2dbcRepository extends R2dbcRepository<JobApplication, UUID> {
    @Query("""
        SELECT ja.*, c.candidate_name, jp.position_name
        FROM job_applications ja
        LEFT JOIN candidates c ON ja.candidate_id = c.id
        LEFT JOIN job_positions jp ON ja.job_position_id = jp.id
        WHERE ja.tenant_id = :tenantId
        AND ja.application_status IN (:statuses)
        ORDER BY ja.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<JobApplication> findByStatusPaginated(String tenantId, java.util.List<String> statuses, int limit, int offset);

    @Query("""
        SELECT COUNT(*) FROM job_applications
        WHERE tenant_id = :tenantId
        AND job_position_id = :jobPositionId
        AND application_status = 'SELECTED'
        """)
    Mono<Long> countSelectedCandidates(String tenantId, UUID jobPositionId);

    @Query("""
        SELECT ja.* FROM job_applications ja
        WHERE ja.tenant_id = :tenantId
        AND ja.candidate_id = :candidateId
        ORDER BY ja.created_at DESC
        LIMIT 1
        """)
    Mono<JobApplication> findLatestApplicationForCandidate(String tenantId, UUID candidateId);
}

/**
 * Appraisal R2dbc Repository
 */
@Repository
public interface AppraisalR2dbcRepository extends R2dbcRepository<Appraisal, UUID> {
    @Query("""
        SELECT a.*, e.employee_name, ap.period_name
        FROM appraisals a
        LEFT JOIN employees e ON a.employee_id = e.id
        LEFT JOIN appraisal_periods ap ON a.appraisal_period_id = ap.id
        WHERE a.tenant_id = :tenantId
        AND a.appraisal_status = :status
        ORDER BY a.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Appraisal> findByStatusPaginated(String tenantId, String status, int limit, int offset);

    @Query("""
        SELECT a.* FROM appraisals a
        WHERE a.tenant_id = :tenantId
        AND a.employee_id = :employeeId
        AND YEAR(a.created_at) = :year
        ORDER BY a.created_at DESC
        """)
    Flux<Appraisal> findByEmployeeAndYear(String tenantId, UUID employeeId, int year);

    @Query("""
        SELECT AVG(performance_rating) FROM appraisals
        WHERE tenant_id = :tenantId
        AND appraisal_period_id = :appraisalPeriodId
        """)
    Mono<Double> calculateAveragePerformanceRating(String tenantId, UUID appraisalPeriodId);
}

/**
 * Training Program R2dbc Repository
 */
@Repository
public interface TrainingProgramR2dbcRepository extends R2dbcRepository<TrainingProgram, UUID> {
    @Query("""
        SELECT tp.* FROM training_programs tp
        WHERE tp.tenant_id = :tenantId
        AND tp.program_status = 'ACTIVE'
        AND tp.start_date >= CURRENT_DATE
        ORDER BY tp.start_date ASC
        LIMIT :limit OFFSET :offset
        """)
    Flux<TrainingProgram> findUpcomingPrograms(String tenantId, int limit, int offset);

    @Query("""
        SELECT tp.* FROM training_programs tp
        WHERE tp.tenant_id = :tenantId
        AND tp.training_category = :category
        AND tp.program_status = 'ACTIVE'
        ORDER BY tp.start_date DESC
        """)
    Flux<TrainingProgram> findByCategory(String tenantId, String category);

    @Query("""
        SELECT COUNT(*) FROM training_enrollments
        WHERE tenant_id = :tenantId
        AND training_program_id = :programId
        AND enrollment_status = 'ENROLLED'
        """)
    Mono<Long> countEnrolledParticipants(String tenantId, UUID programId);
}

/**
 * Grievance R2dbc Repository
 */
@Repository
public interface GrievanceR2dbcRepository extends R2dbcRepository<Grievance, UUID> {
    @Query("""
        SELECT g.*, e.employee_name
        FROM grievances g
        LEFT JOIN employees e ON g.employee_id = e.id
        WHERE g.tenant_id = :tenantId
        AND g.grievance_status IN (:statuses)
        ORDER BY g.created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Grievance> findByStatusPaginated(String tenantId, java.util.List<String> statuses, int limit, int offset);

    @Query("""
        SELECT g.* FROM grievances g
        WHERE g.tenant_id = :tenantId
        AND g.grievance_status = 'FILED'
        AND g.created_at >= CURRENT_DATE - INTERVAL '30 days'
        ORDER BY g.created_at DESC
        """)
    Flux<Grievance> findRecentGrievances(String tenantId);

    @Query("""
        SELECT COUNT(*) FROM grievances
        WHERE tenant_id = :tenantId
        AND grievance_status = 'RESOLVED'
        AND MONTH(created_at) = :month
        """)
    Mono<Long> countResolvedGrievancesForMonth(String tenantId, int month);
}
