package com.company.hrms.employee.controller;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.contracts.document.DocumentType;
import com.company.hrms.employee.model.EmployeeOnboardingApi;
import com.company.hrms.employee.model.EmployeeOnboardingCommandDto;
import com.company.hrms.employee.model.EmployeeOnboardingViewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/employees")
@Tag(name = "EmployeeDto Onboarding", description = "Cross-module employee onboarding orchestration APIs")
public class EmployeeOnboardingController {

    private final EmployeeOnboardingApi employeeOnboardingApi;

    public EmployeeOnboardingController(EmployeeOnboardingApi employeeOnboardingApi) {
        this.employeeOnboardingApi = employeeOnboardingApi;
    }

    @PostMapping("/onboarding")
    @Operation(summary = "Onboard employee", description = "Creates an employee and optionally provisions account, leave, attendance, workflow, notifications and documents in one orchestrated request.")
    public Mono<EmployeeOnboardingViewDto> onboardEmployee(@Valid @RequestBody EmployeeOnboardingRequest request) {
        return employeeOnboardingApi.onboardEmployee(new EmployeeOnboardingCommandDto(
                request.employeeCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.departmentCode(),
                request.jobTitle(),
                request.actor(),
                request.userAccountSetup() == null ? null : new EmployeeOnboardingCommandDto.UserAccountSetup(
                        request.userAccountSetup().create(),
                        request.userAccountSetup().username(),
                        request.userAccountSetup().rawPassword(),
                        request.userAccountSetup().roleCode()),
                request.leaveBalanceSetup() == null ? null : new EmployeeOnboardingCommandDto.LeaveBalanceSetup(
                        request.leaveBalanceSetup().initialize(),
                        request.leaveBalanceSetup().leaveTypeId(),
                        request.leaveBalanceSetup().leaveYear(),
                        request.leaveBalanceSetup().totalDays()),
                request.attendanceSetup() == null ? null : new EmployeeOnboardingCommandDto.AttendanceSetup(
                        request.attendanceSetup().initialize(),
                        request.attendanceSetup().shiftId(),
                        request.attendanceSetup().effectiveFrom(),
                        request.attendanceSetup().effectiveTo()),
                request.triggerOnboardingWorkflow(),
                request.sendWelcomeNotification(),
                request.documents() == null ? List.of() : request.documents().stream()
                        .map(document -> new EmployeeOnboardingCommandDto.DocumentSetup(
                                document.documentType(),
                                document.fileName(),
                                document.contentType(),
                                document.sizeBytes(),
                                document.objectKey(),
                                document.checksum(),
                                document.expiryDate()))
                        .toList()));
    }

    public record EmployeeOnboardingRequest(
            @Schema(description = "Business employee code", example = "EMP-20001")
            @NotBlank String employeeCode,
            @Schema(description = "Given name", example = "Aisha")
            @NotBlank String firstName,
            @Schema(description = "Family name", example = "Khan")
            String lastName,
            @Schema(description = "Primary work email", example = "aisha.khan@acme.com")
            @NotBlank @Email String email,
            @Schema(description = "Department code for placement", example = "FIN")
            String departmentCode,
            @Schema(description = "Job title for placement", example = "Senior Accountant")
            String jobTitle,
            @Schema(description = "Actor performing onboarding action for audit", example = "hr.admin")
            @NotBlank String actor,
            @Schema(description = "Optional user account provisioning details")
            UserAccountSetupRequest userAccountSetup,
            @Schema(description = "Optional leave balance initialization details")
            LeaveBalanceSetupRequest leaveBalanceSetup,
            @Schema(description = "Optional attendance shift initialization details")
            AttendanceSetupRequest attendanceSetup,
            @Schema(description = "Trigger onboarding workflow after provisioning")
            boolean triggerOnboardingWorkflow,
            @Schema(description = "Send welcome notification to employee after onboarding")
            boolean sendWelcomeNotification,
            @Schema(description = "Optional onboarding documents metadata")
            List<DocumentSetupRequest> documents
    ) {
    }

    public record UserAccountSetupRequest(
            @Schema(description = "Whether a system account should be created")
            boolean create,
            @Schema(description = "Username for the new account", example = "aisha.khan")
            String username,
            @Schema(description = "Temporary raw password for the new account", example = "Temp#1234")
            String rawPassword,
            @Schema(description = "RoleDto code to assign to the account", example = "EMPLOYEE")
            String roleCode
    ) {
    }

    public record LeaveBalanceSetupRequest(
            @Schema(description = "Whether leave balance should be initialized")
            boolean initialize,
            @Schema(description = "Leave type identifier", example = "73f12cd8-5f0f-4513-91f0-56de3374a9cc")
            UUID leaveTypeId,
            @Schema(description = "Target leave year", example = "2026")
            int leaveYear,
            @Schema(description = "Allocated leave days", example = "30")
            int totalDays
    ) {
    }

    public record AttendanceSetupRequest(
            @Schema(description = "Whether shift assignment should be initialized")
            boolean initialize,
            @Schema(description = "ShiftDto identifier", example = "911f4648-72a6-4f05-840e-7af62097204d")
            UUID shiftId,
            @Schema(description = "ShiftDto assignment start date", example = "2026-01-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @Schema(description = "ShiftDto assignment end date, null means open-ended", example = "2026-12-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo
    ) {
    }

    public record DocumentSetupRequest(
            @Schema(description = "Document classification", example = "PASSPORT")
            @NotNull DocumentType documentType,
            @Schema(description = "Original file name", example = "passport.pdf")
            @NotBlank String fileName,
            @Schema(description = "MIME content type", example = "application/pdf")
            @NotBlank String contentType,
            @Schema(description = "Document size in bytes", example = "24567")
            long sizeBytes,
            @Schema(description = "Object storage key", example = "tenant/acme/employees/EMP-20001/passport.pdf")
            @NotBlank String objectKey,
            @Schema(description = "Optional checksum for integrity verification", example = "SHA256:abcdef123456")
            String checksum,
            @Schema(description = "Optional document expiry date", example = "2030-06-30")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate
    ) {
    }
}
