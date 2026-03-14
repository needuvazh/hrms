package com.company.hrms.employee.application;

import com.company.hrms.auth.api.AuthModuleApi;
import com.company.hrms.auth.api.ProvisionUserAccountCommand;
import com.company.hrms.document.api.AttachDocumentCommand;
import com.company.hrms.document.api.DocumentModuleApi;
import com.company.hrms.document.api.ExpiryDate;
import com.company.hrms.document.api.VerificationStatus;
import com.company.hrms.employee.api.CreateEmployeeCommand;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeOnboardingApi;
import com.company.hrms.employee.api.EmployeeOnboardingCommand;
import com.company.hrms.employee.api.EmployeeOnboardingView;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.leave.api.InitializeLeaveBalanceCommand;
import com.company.hrms.leave.api.LeaveModuleApi;
import com.company.hrms.notification.api.CreateNotificationCommand;
import com.company.hrms.notification.api.NotificationChannel;
import com.company.hrms.notification.api.NotificationModuleApi;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceModuleClient;
import com.company.hrms.workflow.api.StartWorkflowCommand;
import com.company.hrms.workflow.api.WorkflowModuleApi;
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
    public Mono<EmployeeOnboardingView> onboardEmployee(EmployeeOnboardingCommand command) {
        validate(command);

        return requireTenant().flatMap(tenantId -> auditEventPublisher.publish(AuditEvent.of(
                        resolveActor(command.actor()),
                        tenantId,
                        "ONBOARDING_STARTED",
                        "EMPLOYEE_ONBOARDING",
                        command.email(),
                        Map.of("email", command.email())))
                .then(employeeModuleApi.createEmployee(new CreateEmployeeCommand(
                        command.employeeCode(),
                        command.firstName(),
                        command.lastName(),
                        command.email(),
                        command.departmentCode(),
                        command.jobTitle())))
                .flatMap(employee -> runCrossModuleSteps(tenantId, command, employee)
                        .flatMap(result -> {
                            EmployeeOnboardingView view = toView(employee, tenantId, result.steps, result.workflowInstanceId);
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

    private Mono<OnboardingResult> runCrossModuleSteps(String tenantId, EmployeeOnboardingCommand command, EmployeeView employee) {
        List<EmployeeOnboardingView.OnboardingStepView> steps = new ArrayList<>();
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
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommand.UserAccountSetup setup = command.userAccountSetup();
        if (setup == null || !setup.create()) {
            return addSkippedStep(tenantId, employee.id(), steps, "AUTH_USER_PROVISION", "User account setup skipped");
        }

        String username = StringUtils.hasText(setup.username()) ? setup.username().trim() : command.email();
        String rawPassword = StringUtils.hasText(setup.rawPassword()) ? setup.rawPassword() : "ChangeMe@123";

        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "AUTH_USER_PROVISION",
                authModuleApi.provisionUserAccount(new ProvisionUserAccountCommand(
                        username,
                        command.email(),
                        rawPassword,
                        setup.roleCode()))
                        .then(),
                "User account provisioned",
                "User account provisioning failed");
    }

    private Mono<Void> runLeaveBalanceStep(
            String tenantId,
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommand.LeaveBalanceSetup setup = command.leaveBalanceSetup();
        if (setup == null || !setup.initialize()) {
            return addSkippedStep(tenantId, employee.id(), steps, "LEAVE_BALANCE_INIT", "Leave balance initialization skipped");
        }

        return safeStep(
                tenantId,
                resolveActor(command.actor()),
                employee.id(),
                steps,
                "LEAVE_BALANCE_INIT",
                leaveModuleApi.initializeLeaveBalance(new InitializeLeaveBalanceCommand(
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
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
    ) {
        EmployeeOnboardingCommand.AttendanceSetup setup = command.attendanceSetup();
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
                attendanceModuleClient.assignShift(new AssignShiftCommand(
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
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
    ) {
        if (!command.triggerOnboardingWorkflow()) {
            return addSkippedStep(tenantId, employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow skipped")
                    .then(Mono.empty());
        }

        return workflowModuleApi.startWorkflow(new StartWorkflowCommand(
                        ONBOARDING_WORKFLOW_KEY,
                        "EMPLOYEE",
                        employee.id().toString(),
                        resolveActor(command.actor()),
                        "Employee onboarding started"))
                .flatMap(workflow -> addSuccessStep(tenantId, employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow started")
                        .thenReturn(workflow.id()))
                .onErrorResume(error -> addFailedStep(tenantId, resolveActor(command.actor()), employee.id(), steps, "ONBOARDING_WORKFLOW", "Onboarding workflow failed", error)
                        .then(Mono.empty()));
    }

    private Mono<Void> runNotificationStep(
            String tenantId,
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
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
                notificationModuleApi.createNotification(new CreateNotificationCommand(
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
            EmployeeOnboardingCommand command,
            EmployeeView employee,
            List<EmployeeOnboardingView.OnboardingStepView> steps
    ) {
        if (command.documents() == null || command.documents().isEmpty()) {
            return addSkippedStep(tenantId, employee.id(), steps, "DOCUMENT_ATTACH", "No onboarding documents provided");
        }

        return Flux.fromIterable(command.documents())
                .concatMap(document -> documentModuleApi.attachDocument(new AttachDocumentCommand(
                        document.documentType(),
                        "EMPLOYEE",
                        employee.id().toString(),
                        document.fileName(),
                        document.contentType(),
                        document.sizeBytes(),
                        document.objectKey(),
                        document.checksum(),
                        document.expiryDate() == null ? ExpiryDate.empty() : ExpiryDate.of(document.expiryDate()),
                        VerificationStatus.PENDING,
                        resolveActor(command.actor()))))
                .then(addSuccessStep(tenantId, employee.id(), steps, "DOCUMENT_ATTACH", "Onboarding documents attached"))
                .onErrorResume(error -> addFailedStep(tenantId, resolveActor(command.actor()), employee.id(), steps, "DOCUMENT_ATTACH", "Document attachment failed", error));
    }

    private Mono<Void> safeStep(
            String tenantId,
            String actor,
            UUID employeeId,
            List<EmployeeOnboardingView.OnboardingStepView> steps,
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
            List<EmployeeOnboardingView.OnboardingStepView> steps,
            String step,
            String message
    ) {
        steps.add(new EmployeeOnboardingView.OnboardingStepView(step, "SUCCESS", message));
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
            List<EmployeeOnboardingView.OnboardingStepView> steps,
            String step,
            String message
    ) {
        steps.add(new EmployeeOnboardingView.OnboardingStepView(step, "SKIPPED", message));
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
            List<EmployeeOnboardingView.OnboardingStepView> steps,
            String step,
            String message,
            Throwable error
    ) {
        String errorMessage = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        steps.add(new EmployeeOnboardingView.OnboardingStepView(step, "FAILED", message + ": " + errorMessage));
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

    private void validate(EmployeeOnboardingCommand command) {
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

    private EmployeeOnboardingView toView(
            EmployeeView employee,
            String tenantId,
            List<EmployeeOnboardingView.OnboardingStepView> steps,
            UUID workflowInstanceId
    ) {
        boolean anyFailed = steps.stream().anyMatch(step -> "FAILED".equals(step.status()));
        EmployeeOnboardingView.OnboardingStatus status = anyFailed
                ? EmployeeOnboardingView.OnboardingStatus.PARTIAL_FAILED
                : EmployeeOnboardingView.OnboardingStatus.SUCCESS;

        return new EmployeeOnboardingView(
                employee.id(),
                tenantId,
                status,
                workflowInstanceId,
                List.copyOf(steps),
                Instant.now());
    }

    private record OnboardingResult(
            List<EmployeeOnboardingView.OnboardingStepView> steps,
            UUID workflowInstanceId
    ) {
    }
}
