package com.company.hrms.employee.application;

import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.AttendanceRecordView;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.PunchEventView;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.api.ShiftAssignmentView;
import com.company.hrms.attendance.api.ShiftView;
import com.company.hrms.auth.api.AuthModuleApi;
import com.company.hrms.auth.api.AuthTokenCommand;
import com.company.hrms.auth.api.AuthTokenView;
import com.company.hrms.auth.api.CurrentUserView;
import com.company.hrms.auth.api.ProvisionUserAccountCommand;
import com.company.hrms.auth.api.ProvisionedUserAccountView;
import com.company.hrms.auth.api.RoleView;
import com.company.hrms.document.api.AttachDocumentCommand;
import com.company.hrms.document.api.DocumentExpiryQuery;
import com.company.hrms.document.api.DocumentListQuery;
import com.company.hrms.document.api.DocumentModuleApi;
import com.company.hrms.document.api.DocumentRecordView;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeOnboardingCommand;
import com.company.hrms.employee.api.EmployeeOnboardingView;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.leave.api.ApplyLeaveCommand;
import com.company.hrms.leave.api.DefineLeaveTypeCommand;
import com.company.hrms.leave.api.InitializeLeaveBalanceCommand;
import com.company.hrms.leave.api.LeaveBalanceView;
import com.company.hrms.leave.api.LeaveModuleApi;
import com.company.hrms.leave.api.LeaveRequestView;
import com.company.hrms.leave.api.LeaveTypeView;
import com.company.hrms.leave.api.ReviewLeaveCommand;
import com.company.hrms.leave.domain.LeaveStatus;
import com.company.hrms.notification.api.CreateNotificationCommand;
import com.company.hrms.notification.api.NotificationModuleApi;
import com.company.hrms.notification.api.NotificationView;
import com.company.hrms.notification.api.NotificationChannel;
import com.company.hrms.notification.api.NotificationStatus;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.workflow.api.AdvanceWorkflowCommand;
import com.company.hrms.workflow.api.StartWorkflowCommand;
import com.company.hrms.workflow.api.WorkflowInstanceView;
import com.company.hrms.workflow.api.WorkflowModuleApi;
import com.company.hrms.workflow.api.ApprovalStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeOnboardingServiceTest {

    @Test
    void onboardingSuccessAcrossModules() {
        StubEmployeeModuleApi employeeApi = new StubEmployeeModuleApi();
        RecordingAuditPublisher auditPublisher = new RecordingAuditPublisher();

        EmployeeOnboardingService service = new EmployeeOnboardingService(
                employeeApi,
                new StubAuthApi(false),
                new StubLeaveApi(false),
                new StubAttendanceApi(false),
                new StubWorkflowApi(false),
                new StubNotificationApi(false),
                new StubDocumentApi(false),
                new DefaultTenantContextAccessor(),
                auditPublisher);

        EmployeeOnboardingCommand command = new EmployeeOnboardingCommand(
                "EMP-9001",
                "Jane",
                "Doe",
                "jane@default.hrms",
                "ENG",
                "Engineer",
                "hr-admin",
                new EmployeeOnboardingCommand.UserAccountSetup(true, "jane", "Pwd@123", null),
                new EmployeeOnboardingCommand.LeaveBalanceSetup(true, UUID.randomUUID(), 2026, 12),
                new EmployeeOnboardingCommand.AttendanceSetup(true, UUID.randomUUID(), LocalDate.now(), null),
                true,
                true,
                List.of());

        StepVerifier.create(service.onboardEmployee(command)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> {
                    assertEquals(EmployeeOnboardingView.OnboardingStatus.SUCCESS, view.status());
                    assertEquals(6, view.steps().size());
                    assertTrue(view.steps().stream().allMatch(step -> "SUCCESS".equals(step.status()) || "SKIPPED".equals(step.status())));
                })
                .verifyComplete();

        assertTrue(auditPublisher.actions.contains("ONBOARDING_STARTED"));
        assertTrue(auditPublisher.actions.contains("ONBOARDING_COMPLETED"));
    }

    @Test
    void onboardingContinuesWithPartialFailures() {
        EmployeeOnboardingService service = new EmployeeOnboardingService(
                new StubEmployeeModuleApi(),
                new StubAuthApi(false),
                new StubLeaveApi(true),
                new StubAttendanceApi(false),
                new StubWorkflowApi(false),
                new StubNotificationApi(true),
                new StubDocumentApi(false),
                new DefaultTenantContextAccessor(),
                event -> Mono.empty());

        EmployeeOnboardingCommand command = new EmployeeOnboardingCommand(
                "EMP-9002",
                "John",
                "Smith",
                "john@default.hrms",
                "OPS",
                "Analyst",
                "hr-admin",
                new EmployeeOnboardingCommand.UserAccountSetup(true, "john", "Pwd@123", null),
                new EmployeeOnboardingCommand.LeaveBalanceSetup(true, UUID.randomUUID(), 2026, 8),
                new EmployeeOnboardingCommand.AttendanceSetup(true, UUID.randomUUID(), LocalDate.now(), null),
                true,
                true,
                List.of());

        StepVerifier.create(service.onboardEmployee(command)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> {
                    assertEquals(EmployeeOnboardingView.OnboardingStatus.PARTIAL_FAILED, view.status());
                    assertTrue(view.steps().stream().anyMatch(step -> "FAILED".equals(step.status()) && "LEAVE_BALANCE_INIT".equals(step.step())));
                    assertTrue(view.steps().stream().anyMatch(step -> "FAILED".equals(step.status()) && "WELCOME_NOTIFICATION".equals(step.step())));
                    assertTrue(view.steps().stream().anyMatch(step -> "SUCCESS".equals(step.status()) && "ONBOARDING_WORKFLOW".equals(step.step())));
                })
                .verifyComplete();
    }

    static class RecordingAuditPublisher implements AuditEventPublisher {
        private final CopyOnWriteArrayList<String> actions = new CopyOnWriteArrayList<>();

        @Override
        public Mono<Void> publish(AuditEvent event) {
            actions.add(event.action());
            return Mono.empty();
        }
    }

    static class StubEmployeeModuleApi implements EmployeeModuleApi {
        @Override
        public Mono<EmployeeView> createEmployee(com.company.hrms.employee.api.CreateEmployeeCommand command) {
            return Mono.just(new EmployeeView(
                    UUID.randomUUID(),
                    "default",
                    command.employeeCode(),
                    command.firstName(),
                    command.lastName(),
                    command.email(),
                    command.departmentCode(),
                    command.jobTitle(),
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Mono<EmployeeView> getEmployee(UUID employeeId) {
            return Mono.empty();
        }

        @Override
        public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
            return Flux.empty();
        }
    }

    static class StubAuthApi implements AuthModuleApi {
        private final boolean shouldFail;

        StubAuthApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<AuthTokenView> issueToken(AuthTokenCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<CurrentUserView> currentUser() {
            return Mono.just(new CurrentUserView(UUID.randomUUID(), "admin", "default", Set.of(), Set.of()));
        }

        @Override
        public Flux<RoleView> getRolesForCurrentUser() {
            return Flux.empty();
        }

        @Override
        public Mono<ProvisionedUserAccountView> provisionUserAccount(ProvisionUserAccountCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "AUTH_FAIL", "Auth failed"));
            }
            return Mono.just(new ProvisionedUserAccountView(UUID.randomUUID(), "default", command.username(), command.email(), command.roleCode()));
        }
    }

    static class StubLeaveApi implements LeaveModuleApi {
        private final boolean shouldFail;

        StubLeaveApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<LeaveTypeView> defineLeaveType(DefineLeaveTypeCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveBalanceView> initializeLeaveBalance(InitializeLeaveBalanceCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "LEAVE_FAIL", "Leave init failed"));
            }
            return Mono.just(new LeaveBalanceView(UUID.randomUUID(), "default", command.employeeId(), command.leaveTypeId(), command.leaveYear(), command.totalDays(), 0, command.totalDays(), Instant.now(), Instant.now()));
        }

        @Override
        public Mono<LeaveRequestView> applyLeave(ApplyLeaveCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveRequestView> reviewLeave(ReviewLeaveCommand command) {
            return Mono.empty();
        }

        @Override
        public Flux<LeaveBalanceView> balances(UUID employeeId, int leaveYear) {
            return Flux.empty();
        }

        @Override
        public Flux<LeaveRequestView> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.empty();
        }
    }

    static class StubAttendanceApi implements AttendanceModuleApi {
        private final boolean shouldFail;

        StubAttendanceApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<ShiftView> createShift(CreateShiftCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<ShiftAssignmentView> assignShift(AssignShiftCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "ATT_FAIL", "Attendance failed"));
            }
            return Mono.just(new ShiftAssignmentView(UUID.randomUUID(), "default", command.employeeId(), command.shiftId(), command.effectiveFrom(), command.effectiveTo(), true, Instant.now(), Instant.now()));
        }

        @Override
        public Mono<PunchEventView> recordPunch(RecordPunchCommand command) {
            return Mono.empty();
        }

        @Override
        public Flux<AttendanceRecordView> attendanceByEmployee(AttendanceQuery query) {
            return Flux.empty();
        }
    }

    static class StubWorkflowApi implements WorkflowModuleApi {
        private final boolean shouldFail;

        StubWorkflowApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<WorkflowInstanceView> startWorkflow(StartWorkflowCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "WF_FAIL", "Workflow failed"));
            }
            return Mono.just(new WorkflowInstanceView(
                    UUID.randomUUID(),
                    "default",
                    command.workflowKey(),
                    command.targetType(),
                    command.targetId(),
                    ApprovalStatus.SUBMITTED,
                    command.actor(),
                    null,
                    Instant.now(),
                    null,
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Mono<WorkflowInstanceView> advanceWorkflow(AdvanceWorkflowCommand command) {
            return Mono.empty();
        }

        @Override
        public Mono<WorkflowInstanceView> getWorkflowInstance(UUID workflowInstanceId) {
            return Mono.empty();
        }
    }

    static class StubNotificationApi implements NotificationModuleApi {
        private final boolean shouldFail;

        StubNotificationApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<NotificationView> createNotification(CreateNotificationCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "NOTIFY_FAIL", "Notification failed"));
            }
            return Mono.just(new NotificationView(
                    UUID.randomUUID(),
                    "default",
                    NotificationChannel.EMAIL,
                    command.recipient(),
                    command.subject(),
                    command.body(),
                    command.templateCode(),
                    command.referenceType(),
                    command.referenceId(),
                    NotificationStatus.QUEUED,
                    null,
                    null,
                    Instant.now(),
                    Instant.now()));
        }

        @Override
        public Flux<NotificationView> dispatchQueuedNotifications(int limit) {
            return Flux.empty();
        }

        @Override
        public Mono<NotificationView> getNotification(UUID notificationId) {
            return Mono.empty();
        }
    }

    static class StubDocumentApi implements DocumentModuleApi {
        private final boolean shouldFail;

        StubDocumentApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<DocumentRecordView> attachDocument(AttachDocumentCommand command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "DOC_FAIL", "Document failed"));
            }
            return Mono.empty();
        }

        @Override
        public Flux<DocumentRecordView> listDocuments(DocumentListQuery query) {
            return Flux.empty();
        }

        @Override
        public Flux<DocumentRecordView> findExpiringDocuments(DocumentExpiryQuery query) {
            return Flux.empty();
        }

        @Override
        public Mono<DocumentRecordView> archiveDocument(UUID documentId) {
            return Mono.empty();
        }

        @Override
        public Mono<DocumentRecordView> getDocument(UUID documentId) {
            return Mono.empty();
        }
    }
}
