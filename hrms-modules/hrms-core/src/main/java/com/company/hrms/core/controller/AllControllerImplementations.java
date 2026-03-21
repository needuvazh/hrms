package com.company.hrms.core.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/**
 * Employee REST Controller
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public Flux<EmployeeViewDto> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String departmentId) {
        return employeeService.getAllEmployees(page, size, departmentId);
    }

    @GetMapping("/{id}")
    public Mono<EmployeeViewDto> getEmployeeById(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public Mono<EmployeeViewDto> createEmployee(@RequestBody CreateEmployeeCommandDto dto) {
        return employeeService.createEmployee(dto);
    }

    @PutMapping("/{id}")
    public Mono<EmployeeViewDto> updateEmployee(
            @PathVariable UUID id,
            @RequestBody UpdateEmployeeCommandDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @GetMapping("/expiring-documents")
    public Flux<EmployeeViewDto> getEmployeesWithExpiringDocuments() {
        return employeeService.getEmployeesWithExpiringDocuments();
    }

    @PostMapping("/{id}/documents")
    public Mono<DocumentViewDto> uploadDocument(
            @PathVariable UUID id,
            @RequestBody UploadDocumentCommandDto dto) {
        return employeeService.uploadDocument(id, dto);
    }
}

/**
 * Leave Application REST Controller
 */
@RestController
@RequestMapping("/api/v1/leave-applications")
public class LeaveApplicationController {
    
    private final LeaveApplicationService leaveService;

    public LeaveApplicationController(LeaveApplicationService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public Flux<LeaveApplicationViewDto> getLeaveApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return leaveService.getLeaveApplications(page, size, status);
    }

    @GetMapping("/{id}")
    public Mono<LeaveApplicationViewDto> getLeaveApplicationById(@PathVariable UUID id) {
        return leaveService.getLeaveApplicationById(id);
    }

    @PostMapping
    public Mono<LeaveApplicationViewDto> applyForLeave(
            @RequestBody CreateLeaveApplicationCommandDto dto) {
        return leaveService.applyForLeave(dto);
    }

    @PutMapping("/{id}/approve")
    public Mono<LeaveApplicationViewDto> approveLeave(
            @PathVariable UUID id,
            @RequestBody ApproveLeaveCommandDto dto) {
        return leaveService.approveLeave(id, dto);
    }

    @PutMapping("/{id}/reject")
    public Mono<LeaveApplicationViewDto> rejectLeave(
            @PathVariable UUID id,
            @RequestBody RejectLeaveCommandDto dto) {
        return leaveService.rejectLeave(id, dto);
    }

    @GetMapping("/balance/{employeeId}")
    public Mono<LeaveBalanceViewDto> getLeaveBalance(@PathVariable UUID employeeId) {
        return leaveService.getLeaveBalance(employeeId);
    }

    @GetMapping("/pending-approvals")
    public Flux<LeaveApplicationViewDto> getPendingApprovalsForCurrentUser() {
        return leaveService.getPendingApprovalsForCurrentUser();
    }
}

/**
 * Attendance REST Controller
 */
@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public Flux<AttendanceViewDto> getAttendanceRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return attendanceService.getAttendanceRecords(page, size, startDate, endDate);
    }

    @PostMapping("/punch-in")
    public Mono<AttendanceViewDto> punchIn(@RequestBody PunchInCommandDto dto) {
        return attendanceService.punchIn(dto);
    }

    @PostMapping("/punch-out")
    public Mono<AttendanceViewDto> punchOut(@RequestBody PunchOutCommandDto dto) {
        return attendanceService.punchOut(dto);
    }

    @GetMapping("/today/{employeeId}")
    public Mono<AttendanceViewDto> getTodayAttendance(@PathVariable UUID employeeId) {
        return attendanceService.getTodayAttendance(employeeId);
    }

    @GetMapping("/absent-records")
    public Flux<AttendanceViewDto> getAbsentRecords(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return attendanceService.getAbsentRecords(startDate, endDate);
    }
}

/**
 * Payroll REST Controller
 */
@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {
    
    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/periods")
    public Flux<PayrollPeriodViewDto> getPayrollPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return payrollService.getPayrollPeriods(page, size);
    }

    @PostMapping("/periods")
    public Mono<PayrollPeriodViewDto> createPayrollPeriod(
            @RequestBody CreatePayrollPeriodCommandDto dto) {
        return payrollService.createPayrollPeriod(dto);
    }

    @PostMapping("/process")
    public Mono<PayrollPeriodViewDto> processPayroll(
            @RequestBody ProcessPayrollCommandDto dto) {
        return payrollService.processPayroll(dto);
    }

    @GetMapping("/employee/{employeeId}")
    public Flux<EmployeePayrollViewDto> getEmployeePayroll(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return payrollService.getEmployeePayroll(employeeId, page, size);
    }

    @GetMapping("/payslips/{employeeId}")
    public Flux<PayslipViewDto> getPayslips(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return payrollService.getPayslips(employeeId, page, size);
    }

    @PostMapping("/wps/export")
    public Mono<WpsExportViewDto> exportWps(
            @RequestBody ExportWpsCommandDto dto) {
        return payrollService.exportWps(dto);
    }
}

/**
 * Resignation REST Controller
 */
@RestController
@RequestMapping("/api/v1/resignations")
public class ResignationController {
    
    private final ResignationService resignationService;

    public ResignationController(ResignationService resignationService) {
        this.resignationService = resignationService;
    }

    @GetMapping
    public Flux<ResignationViewDto> getResignations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return resignationService.getResignations(page, size, status);
    }

    @PostMapping
    public Mono<ResignationViewDto> submitResignation(
            @RequestBody SubmitResignationCommandDto dto) {
        return resignationService.submitResignation(dto);
    }

    @PutMapping("/{id}/accept")
    public Mono<ResignationViewDto> acceptResignation(
            @PathVariable UUID id,
            @RequestBody AcceptResignationCommandDto dto) {
        return resignationService.acceptResignation(id, dto);
    }

    @GetMapping("/eosb/{resignationId}")
    public Mono<EosbCalculationViewDto> calculateEosb(
            @PathVariable UUID resignationId) {
        return resignationService.calculateEosb(resignationId);
    }

    @GetMapping("/upcoming-separations")
    public Flux<ResignationViewDto> getUpcomingSeparations() {
        return resignationService.getUpcomingSeparations();
    }
}

/**
 * Job Application REST Controller
 */
@RestController
@RequestMapping("/api/v1/job-applications")
public class JobApplicationController {
    
    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @GetMapping
    public Flux<JobApplicationViewDto> getJobApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return jobApplicationService.getJobApplications(page, size, status);
    }

    @PostMapping
    public Mono<JobApplicationViewDto> createJobApplication(
            @RequestBody CreateJobApplicationCommandDto dto) {
        return jobApplicationService.createJobApplication(dto);
    }

    @PutMapping("/{id}/shortlist")
    public Mono<JobApplicationViewDto> shortlistCandidate(
            @PathVariable UUID id) {
        return jobApplicationService.shortlistCandidate(id);
    }

    @PutMapping("/{id}/select")
    public Mono<JobApplicationViewDto> selectCandidate(
            @PathVariable UUID id,
            @RequestBody SelectCandidateCommandDto dto) {
        return jobApplicationService.selectCandidate(id, dto);
    }

    @PostMapping("/{id}/offer-letter")
    public Mono<OfferLetterViewDto> generateOfferLetter(
            @PathVariable UUID id,
            @RequestBody GenerateOfferLetterCommandDto dto) {
        return jobApplicationService.generateOfferLetter(id, dto);
    }
}

/**
 * Appraisal REST Controller
 */
@RestController
@RequestMapping("/api/v1/appraisals")
public class AppraisalController {
    
    private final AppraisalService appraisalService;

    public AppraisalController(AppraisalService appraisalService) {
        this.appraisalService = appraisalService;
    }

    @GetMapping
    public Flux<AppraisalViewDto> getAppraisals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return appraisalService.getAppraisals(page, size, status);
    }

    @PostMapping
    public Mono<AppraisalViewDto> createAppraisal(
            @RequestBody CreateAppraisalCommandDto dto) {
        return appraisalService.createAppraisal(dto);
    }

    @PutMapping("/{id}/submit")
    public Mono<AppraisalViewDto> submitAppraisal(
            @PathVariable UUID id,
            @RequestBody SubmitAppraisalCommandDto dto) {
        return appraisalService.submitAppraisal(id, dto);
    }

    @GetMapping("/employee/{employeeId}")
    public Flux<AppraisalViewDto> getEmployeeAppraisals(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return appraisalService.getEmployeeAppraisals(employeeId, page, size);
    }
}

/**
 * Training Program REST Controller
 */
@RestController
@RequestMapping("/api/v1/training-programs")
public class TrainingProgramController {
    
    private final TrainingProgramService trainingService;

    public TrainingProgramController(TrainingProgramService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public Flux<TrainingProgramViewDto> getTrainingPrograms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        return trainingService.getTrainingPrograms(page, size, category);
    }

    @GetMapping("/upcoming")
    public Flux<TrainingProgramViewDto> getUpcomingPrograms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return trainingService.getUpcomingPrograms(page, size);
    }

    @PostMapping
    public Mono<TrainingProgramViewDto> createTrainingProgram(
            @RequestBody CreateTrainingProgramCommandDto dto) {
        return trainingService.createTrainingProgram(dto);
    }

    @PostMapping("/{id}/enroll")
    public Mono<TrainingEnrollmentViewDto> enrollEmployee(
            @PathVariable UUID id,
            @RequestBody EnrollEmployeeCommandDto dto) {
        return trainingService.enrollEmployee(id, dto);
    }
}

/**
 * Grievance REST Controller
 */
@RestController
@RequestMapping("/api/v1/grievances")
public class GrievanceController {
    
    private final GrievanceService grievanceService;

    public GrievanceController(GrievanceService grievanceService) {
        this.grievanceService = grievanceService;
    }

    @GetMapping
    public Flux<GrievanceViewDto> getGrievances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return grievanceService.getGrievances(page, size, status);
    }

    @PostMapping
    public Mono<GrievanceViewDto> fileGrievance(
            @RequestBody FileGrievanceCommandDto dto) {
        return grievanceService.fileGrievance(dto);
    }

    @PutMapping("/{id}/acknowledge")
    public Mono<GrievanceViewDto> acknowledgeGrievance(
            @PathVariable UUID id) {
        return grievanceService.acknowledgeGrievance(id);
    }

    @PutMapping("/{id}/resolve")
    public Mono<GrievanceViewDto> resolveGrievance(
            @PathVariable UUID id,
            @RequestBody ResolveGrievanceCommandDto dto) {
        return grievanceService.resolveGrievance(id, dto);
    }

    @GetMapping("/recent")
    public Flux<GrievanceViewDto> getRecentGrievances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return grievanceService.getRecentGrievances(page, size);
    }
}
