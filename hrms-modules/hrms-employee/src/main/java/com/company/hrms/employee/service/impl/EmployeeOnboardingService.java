package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.auth.service.AuthModuleApi;
import com.company.hrms.contracts.auth.ProvisionUserAccountCommandDto;
import com.company.hrms.contracts.document.AttachDocumentCommandDto;
import com.company.hrms.document.service.DocumentModuleApi;
import com.company.hrms.contracts.document.ExpiryDateDto;
import com.company.hrms.contracts.document.VerificationStatus;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.model.EmployeeOnboardingApi;
import com.company.hrms.employee.model.EmployeeOnboardingCommandDto;
import com.company.hrms.employee.model.EmployeeOnboardingViewDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.leave.model.InitializeLeaveBalanceCommandDto;
import com.company.hrms.leave.service.LeaveModuleApi;
import com.company.hrms.notification.model.CreateNotificationCommandDto;
import com.company.hrms.notification.model.NotificationChannel;
import com.company.hrms.notification.service.NotificationModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.service.AttendanceModuleClient;
import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.contracts.workflow.StartWorkflowCommandDto;
import com.company.hrms.workflow.service.WorkflowModuleApi;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeOnboardingService implements EmployeeOnboardingApi {

    private static final String ONBOARDING_WORKFLOW_KEY = "employee.onboarding";

    private final EmployeeModuleApi employeeModuleApi;
    private final AuthModuleApi authModuleApi;
    private final LeaveModuleApi leaveModuleApi;
    private final AttendanceModuleClient attendanceModuleClient;
    private final WorkflowModuleApi workflowModuleApi;
    private final NotificationModuleApi notificationModuleApi;
    private final DocumentModuleApi documentModuleApi;
    private final TenantContextAccessor tenantContextAccessor;
    private final AuditEventPublisher auditEventPublisher;

    public EmployeeOnboardingService(
            EmployeeModuleApi employeeModuleApi,
            AuthModuleApi authModuleApi,
            LeaveModuleApi leaveModuleApi,
            AttendanceModuleClient attendanceModuleClient,
            WorkflowModuleApi workflowModuleApi,
            NotificationModuleApi notificationModuleApi,
            DocumentModuleApi documentModuleApi,
            TenantContextAccessor tenantContextAccessor,
            AuditEventPublisher auditEventPublisher
    ) {
        this.employeeModuleApi = employeeModuleApi;
        this.authModuleApi = authModuleApi;
        this.leaveModuleApi = leaveModuleApi;
        this.attendanceModuleClient = attendanceModuleClient;
        this.workflowModuleApi = workflowModuleApi;
        this.notificationModuleApi = notificationModuleApi;
        this.documentModuleApi = documentModuleApi;
        this.tenantContextAccessor = tenantContextAccessor;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Override
    public Mono<EmployeeOnboardingViewDto> onboardEmployee(EmployeeOnboardingCommandDto command) {
        validate(command);

        return requireTenant().flatMap(tenantId -> auditEventPublisher.publish(AuditEvent.of(
                        resolveActor(command.actor()),
                        tenantId,
                        "ONBOARDING_STARTED",
                        "EMPLOYEE_ONBOARDING",
                        command.email(),
                        Map.of("email", command.email())))
                .then(employeeModuleApi.createEmployee(new CreateEmployeeCommandDto(
                        command.employeeCode(),
                        command.firstName(),
                        command.lastName(),
                        command.email(),
                        command.departmentCode(),
                        command.jobTitle())))
                .flatMap(employee -> runCrossModuleSteps(tenantId, command, employee)
                        .flatMap(result -> {
                            EmployeeOnboardingViewDto view = toView(employee, tenantId, result.steps, result.workflowInstanceId);
                            return auditEventPublisher.publish(AuditEvent.of(
                                            resolveActor(command.actor()),
                                            tenantId,
                                            "ONBOARDING_COMPLETED",
                                            "EMPLOYEE",
                                            employee.id().toString(),
                                            Map.of("status", view.status().name(), "steps", Integer.toString(view.steps().size()))))
                                    .thenReturn(view);
                        }))); 
    }

    private Mono<OnboardingResult> runCrossModuleSteps(String tenantId, EmployeeOnboardingCommandDto command, EmployeeViewDto employee) {
        List<EmployeeOnboardingViewDto.OnboardingStepView> steps = new ArrayList<>();
        AtomicReference<UUID> workflowInstanceIdRef = new AtomicReference<>();

        return runUserAccountStep(tenantId, command, employee, steps)
                .then(runLeaveBalanceStep(tenantId, command, employee, steps))
                .then(runAttendanceStep(tenantId, command, employee, steps))
                .then(runWorkflowStep(tenantId, command, employee, steps))
                .doOnNext(workflowInstanceIdRef::set)
                .then(runNotificationStep(tenantId, command, employee, steps))
                .then(runDocumentStep(tenantId, command, employee, steps))
                .then(Mono.fromSupplier(() -> new OnboardingResult(steps, workflowInstanceIdRef.get())));
    }

    private Mono<Void> runUserAccountStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommandDto.UserAccountSetup setup = command.userAccountSetup();
        if (setup == null || !setup.create()) {
            return addSkippedStep(tenantId, employee.id(), steps, "AUTH_USER_PROVISION", "UserDto account setup skipped");
        }

        String username = StringUtils.hasText(setup.username()) ? setup.username().trim() : command.email();
        String rawPassword = StringUtils.hasText(setup.rawPassword()) ? setup.rawPassword() : "ChangeMe@123";

        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "AUTH_USER_PROVISION",
                authModuleApi.provisionUserAccount(new ProvisionUserAccountCommandDto(
                        username,
                        command.email(),
                        rawPassword,
                        setup.roleCode()))
                        .then(),
                "UserDto account provisioned",
                "UserDto account provisioning failed");
    }

    private Mono<Void> runLeaveBalanceStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommandDto.LeaveBalanceSetup setup = command.leaveBalanceSetup();
        if (setup == null || !setup.initialize()) {
            return addSkippedStep(tenantId, employee.id(), steps, "LEAVE_BALANCE_INIT", "Leave balance initialization skipped");
        }

        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "LEAVE_BALANCE_INIT",
                leaveModuleApi.initializeLeaveBalance(new InitializeLeaveBalanceCommandDto(
                                employee.id(),
                                setup.leaveTypeId(),
                                setup.leaveYear(),
                                setup.totalDays()))
                        .then(),
                "Leave balance initialized",
                "Leave balance initialization failed");
    }

    private Mono<Void> runAttendanceStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommandDto.AttendanceSetup setup = command.attendanceSetup();
        if (setup == null || !setup.initialize()) {
            return addSkippedStep(tenantId, employee.id(), steps, "ATTENDANCE_SETUP", "Attendance setup skipped");
        }
        if (setup.shiftId() == null) {
            return addSuccessStep(tenantId, employee.id(), steps, "ATTENDANCE_SETUP", "Attendance profile placeholder created");
        }

        LocalDate effectiveFrom = setup.effectiveFrom() == null ? LocalDate.now() : setup.effectiveFrom();
        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "ATTENDANCE_SETUP",
                attendanceModuleClient.assignShift(new AssignShiftCommandDto(
                                employee.id(),
                                setup.shiftId(),
                                effectiveFrom,
                                setup.effectiveTo()))
                        .then(),
                "Attendance shift assigned",
                "Attendance setup failed");
    }

    private Mono<UUID> runWorkflowStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        if (!command.triggerOnboardingWorkflow()) {
            return addSkippedStep(tenantId, employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow skipped")
                    .then(Mono.empty());
        }

        return workflowModuleApi.startWorkflow(new StartWorkflowCommandDto(
                        ONBOARDING_WORKFLOW_KEY,
                        "EMPLOYEE",
                        employee.id().toString(),
                        resolveActor(command.actor()),
                        "EmployeeDto onboarding started"))
                .flatMap(workflow -> addSuccessStep(tenantId, employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow started")
                        .thenReturn(workflow.id()))
                .onErrorResume(error -> addFailedStep(tenantId, resolveActor(command.actor()), employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow failed", error)
                        .then(Mono.empty()));
    }

    private Mono<Void> runNotificationStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        if (!command.sendWelcomeNotification()) {
            return addSkippedStep(tenantId, employee.id(), steps, "WELCOME_NOTIFICATION", "Welcome notification skipped");
        }

        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "WELCOME_NOTIFICATION",
                notificationModuleApi.createNotification(new CreateNotificationCommandDto(
                                NotificationChannel.EMAIL,
                                employee.email(),
                                "Welcome to HRMS",
                                "Welcome " + employee.firstName() + ", your onboarding is in progress.",
                                null,
                                Map.of("employeeId", employee.id().toString()),
                                "EMPLOYEE",
                                employee.id().toString()))
                        .then(),
                "Welcome notification queued",
                "Welcome notification failed");
    }

    private Mono<Void> runDocumentStep(
            String tenantId,
            EmployeeOnboardingCommandDto command,
            EmployeeViewDto employee,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps
    ) {
        if (command.documents() == null || command.documents().isEmpty()) {
            return addSkippedStep(tenantId, employee.id(), steps, "DOCUMENT_ATTACH", "No onboarding documents provided");
        }

        return Flux.fromIterable(command.documents())
                .concatMap(document -> documentModuleApi.attachDocument(new AttachDocumentCommandDto(
                        document.documentType(),
                        "EMPLOYEE",
                        employee.id().toString(),
                        document.fileName(),
                        document.contentType(),
                        document.sizeBytes(),
                        document.objectKey(),
                        document.checksum(),
                        document.expiryDate() == null ? ExpiryDateDto.empty() : ExpiryDateDto.of(document.expiryDate()),
                        VerificationStatus.PENDING,
                        resolveActor(command.actor()))))
                .then(addSuccessStep(tenantId, employee.id(), steps, "DOCUMENT_ATTACH", "Onboarding documents attached"))
                .onErrorResume(error -> addFailedStep(tenantId, resolveActor(command.actor()), employee.id(), steps, "DOCUMENT_ATTACH", "Document attachment failed", error));
    }

    private Mono<Void> safeStep(
            String tenantId,
            String actor,
            UUID employeeId,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            String stepName,
            Mono<Void> action,
            String successMessage,
            String failureMessage
    ) {
        return action
                .then(addSuccessStep(tenantId, employeeId, steps, stepName, successMessage))
                .onErrorResume(error -> addFailedStep(tenantId, actor, employeeId, steps, stepName, failureMessage, error));
    }

    private Mono<Void> addSuccessStep(
            String tenantId,
            UUID employeeId,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            String step,
            String message
    ) {
        steps.add(new EmployeeOnboardingViewDto.OnboardingStepView(step, "SUCCESS", message));
        return auditEventPublisher.publish(AuditEvent.of(
                "system",
                tenantId,
                "ONBOARDING_STEP_SUCCESS",
                "EMPLOYEE",
                employeeId.toString(),
                Map.of("step", step, "message", message)));
    }

    private Mono<Void> addSkippedStep(
            String tenantId,
            UUID employeeId,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            String step,
            String message
    ) {
        steps.add(new EmployeeOnboardingViewDto.OnboardingStepView(step, "SKIPPED", message));
        return auditEventPublisher.publish(AuditEvent.of(
                "system",
                tenantId,
                "ONBOARDING_STEP_SKIPPED",
                "EMPLOYEE",
                employeeId.toString(),
                Map.of("step", step, "message", message)));
    }

    private Mono<Void> addFailedStep(
            String tenantId,
            String actor,
            UUID employeeId,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            String step,
            String message,
            Throwable error
    ) {
        String errorMessage = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        steps.add(new EmployeeOnboardingViewDto.OnboardingStepView(step, "FAILED", message + ": " + errorMessage));
        return auditEventPublisher.publish(AuditEvent.of(
                actor,
                tenantId,
                "ONBOARDING_STEP_FAILED",
                "EMPLOYEE",
                employeeId.toString(),
                Map.of("step", step, "error", error.getClass().getSimpleName(), "message", errorMessage)));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validate(EmployeeOnboardingCommandDto command) {
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
        if (!StringUtils.hasText(command.actor())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "ACTOR_REQUIRED", "Actor is required");
        }
    }

    private String resolveActor(String actor) {
        return StringUtils.hasText(actor) ? actor.trim() : "system";
    }

    private EmployeeOnboardingViewDto toView(
            EmployeeViewDto employee,
            String tenantId,
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            UUID workflowInstanceId
    ) {
        boolean anyFailed = steps.stream().anyMatch(step -> "FAILED".equals(step.status()));
        EmployeeOnboardingViewDto.OnboardingStatus status = anyFailed
                ? EmployeeOnboardingViewDto.OnboardingStatus.PARTIAL_FAILED
                : EmployeeOnboardingViewDto.OnboardingStatus.SUCCESS;

        return new EmployeeOnboardingViewDto(
                employee.id(),
                tenantId,
                status,
                workflowInstanceId,
                List.copyOf(steps),
                Instant.now());
    }

    private record OnboardingResult(
            List<EmployeeOnboardingViewDto.OnboardingStepView> steps,
            UUID workflowInstanceId
    ) {
    }
}
