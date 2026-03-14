package com.company.hrms.employee.service.impl;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.repository.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.service.AttendanceModuleClient;
import com.company.hrms.attendance.model.AttendanceQueryDto;
import com.company.hrms.attendance.model.AttendanceRecordViewDto;
import com.company.hrms.attendance.model.CreateShiftCommandDto;
import com.company.hrms.attendance.model.PunchEventViewDto;
import com.company.hrms.attendance.model.RecordPunchCommandDto;
import com.company.hrms.attendance.model.ShiftAssignmentViewDto;
import com.company.hrms.attendance.model.ShiftViewDto;
import com.company.hrms.auth.service.AuthModuleApi;
import com.company.hrms.auth.model.AuthTokenCommandDto;
import com.company.hrms.auth.model.AuthTokenViewDto;
import com.company.hrms.auth.model.CurrentUserViewDto;
import com.company.hrms.contracts.auth.ProvisionUserAccountCommandDto;
import com.company.hrms.auth.model.ProvisionedUserAccountViewDto;
import com.company.hrms.auth.model.RoleViewDto;
import com.company.hrms.contracts.document.AttachDocumentCommandDto;
import com.company.hrms.document.model.DocumentExpiryQueryDto;
import com.company.hrms.document.model.DocumentListQueryDto;
import com.company.hrms.document.service.DocumentModuleApi;
import com.company.hrms.document.model.DocumentRecordViewDto;
import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.model.EmployeeOnboardingCommandDto;
import com.company.hrms.employee.model.EmployeeOnboardingViewDto;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
import com.company.hrms.leave.model.ApplyLeaveCommandDto;
import com.company.hrms.leave.model.DefineLeaveTypeCommandDto;
import com.company.hrms.leave.model.InitializeLeaveBalanceCommandDto;
import com.company.hrms.leave.model.LeaveBalanceViewDto;
import com.company.hrms.leave.service.LeaveModuleApi;
import com.company.hrms.leave.model.LeaveRequestViewDto;
import com.company.hrms.leave.model.LeaveTypeViewDto;
import com.company.hrms.leave.model.ReviewLeaveCommandDto;
import com.company.hrms.leave.model.LeaveStatus;
import com.company.hrms.notification.model.CreateNotificationCommandDto;
import com.company.hrms.notification.service.NotificationModuleApi;
import com.company.hrms.notification.model.NotificationViewDto;
import com.company.hrms.notification.model.NotificationChannel;
import com.company.hrms.notification.model.NotificationStatus;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import com.company.hrms.contracts.workflow.AdvanceWorkflowCommandDto;
import com.company.hrms.contracts.workflow.StartWorkflowCommandDto;
import com.company.hrms.workflow.model.WorkflowInstanceViewDto;
import com.company.hrms.workflow.service.WorkflowModuleApi;
import com.company.hrms.workflow.model.ApprovalStatus;
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

        EmployeeOnboardingCommandDto command = new EmployeeOnboardingCommandDto(
                "EMP-9001",
                "Jane",
                "Doe",
                "jane@default.hrms",
                "ENG",
                "Engineer",
                "hr-admin",
                new EmployeeOnboardingCommandDto.UserAccountSetup(true, "jane", "Pwd@123", null),
                new EmployeeOnboardingCommandDto.LeaveBalanceSetup(true, UUID.randomUUID(), 2026, 12),
                new EmployeeOnboardingCommandDto.AttendanceSetup(true, UUID.randomUUID(), LocalDate.now(), null),
                true,
                true,
                List.of());

        StepVerifier.create(service.onboardEmployee(command)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> {
                    assertEquals(EmployeeOnboardingViewDto.OnboardingStatus.SUCCESS, view.status());
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

        EmployeeOnboardingCommandDto command = new EmployeeOnboardingCommandDto(
                "EMP-9002",
                "John",
                "Smith",
                "john@default.hrms",
                "OPS",
                "Analyst",
                "hr-admin",
                new EmployeeOnboardingCommandDto.UserAccountSetup(true, "john", "Pwd@123", null),
                new EmployeeOnboardingCommandDto.LeaveBalanceSetup(true, UUID.randomUUID(), 2026, 8),
                new EmployeeOnboardingCommandDto.AttendanceSetup(true, UUID.randomUUID(), LocalDate.now(), null),
                true,
                true,
                List.of());

        StepVerifier.create(service.onboardEmployee(command)
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(view -> {
                    assertEquals(EmployeeOnboardingViewDto.OnboardingStatus.PARTIAL_FAILED, view.status());
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
        public Mono<EmployeeViewDto> createEmployee(CreateEmployeeCommandDto command) {
            return Mono.just(new EmployeeViewDto(
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
        public Mono<EmployeeViewDto> getEmployee(UUID employeeId) {
            return Mono.empty();
        }

        @Override
        public Flux<EmployeeViewDto> searchEmployees(EmployeeSearchQueryDto query) {
            return Flux.empty();
        }
    }

    static class StubAuthApi implements AuthModuleApi {
        private final boolean shouldFail;

        StubAuthApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<AuthTokenViewDto> issueToken(AuthTokenCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<CurrentUserViewDto> currentUser() {
            return Mono.just(new CurrentUserViewDto(UUID.randomUUID(), "admin", "default", Set.of(), Set.of()));
        }

        @Override
        public Flux<RoleViewDto> getRolesForCurrentUser() {
            return Flux.empty();
        }

        @Override
        public Mono<ProvisionedUserAccountViewDto> provisionUserAccount(ProvisionUserAccountCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "AUTH_FAIL", "Auth failed"));
            }
            return Mono.just(new ProvisionedUserAccountViewDto(UUID.randomUUID(), "default", command.username(), command.email(), command.roleCode()));
        }
    }

    static class StubLeaveApi implements LeaveModuleApi {
        private final boolean shouldFail;

        StubLeaveApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<LeaveTypeViewDto> defineLeaveType(DefineLeaveTypeCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveBalanceViewDto> initializeLeaveBalance(InitializeLeaveBalanceCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "LEAVE_FAIL", "Leave init failed"));
            }
            return Mono.just(new LeaveBalanceViewDto(UUID.randomUUID(), "default", command.employeeId(), command.leaveTypeId(), command.leaveYear(), command.totalDays(), 0, command.totalDays(), Instant.now(), Instant.now()));
        }

        @Override
        public Mono<LeaveRequestViewDto> applyLeave(ApplyLeaveCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<LeaveRequestViewDto> reviewLeave(ReviewLeaveCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Flux<LeaveBalanceViewDto> balances(UUID employeeId, int leaveYear) {
            return Flux.empty();
        }

        @Override
        public Flux<LeaveRequestViewDto> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.empty();
        }
    }

    static class StubAttendanceApi implements AttendanceModuleClient {
        private final boolean shouldFail;

        StubAttendanceApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<ShiftViewDto> createShift(CreateShiftCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<ShiftAssignmentViewDto> assignShift(AssignShiftCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "ATT_FAIL", "Attendance failed"));
            }
            return Mono.just(new ShiftAssignmentViewDto(UUID.randomUUID(), "default", command.employeeId(), command.shiftId(), command.effectiveFrom(), command.effectiveTo(), true, Instant.now(), Instant.now()));
        }

        @Override
        public Mono<PunchEventViewDto> recordPunch(RecordPunchCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Flux<AttendanceRecordViewDto> attendanceByEmployee(AttendanceQueryDto query) {
            return Flux.empty();
        }
    }

    static class StubWorkflowApi implements WorkflowModuleApi {
        private final boolean shouldFail;

        StubWorkflowApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<WorkflowInstanceViewDto> startWorkflow(StartWorkflowCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "WF_FAIL", "Workflow failed"));
            }
            return Mono.just(new WorkflowInstanceViewDto(
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
        public Mono<WorkflowInstanceViewDto> advanceWorkflow(AdvanceWorkflowCommandDto command) {
            return Mono.empty();
        }

        @Override
        public Mono<WorkflowInstanceViewDto> getWorkflowInstance(UUID workflowInstanceId) {
            return Mono.empty();
        }
    }

    static class StubNotificationApi implements NotificationModuleApi {
        private final boolean shouldFail;

        StubNotificationApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<NotificationViewDto> createNotification(CreateNotificationCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "NOTIFY_FAIL", "NotificationDto failed"));
            }
            return Mono.just(new NotificationViewDto(
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
        public Flux<NotificationViewDto> dispatchQueuedNotifications(int limit) {
            return Flux.empty();
        }

        @Override
        public Mono<NotificationViewDto> getNotification(UUID notificationId) {
            return Mono.empty();
        }
    }

    static class StubDocumentApi implements DocumentModuleApi {
        private final boolean shouldFail;

        StubDocumentApi(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public Mono<DocumentRecordViewDto> attachDocument(AttachDocumentCommandDto command) {
            if (shouldFail) {
                return Mono.error(new HrmsException(org.springframework.http.HttpStatus.BAD_REQUEST, "DOC_FAIL", "Document failed"));
            }
            return Mono.empty();
        }

        @Override
        public Flux<DocumentRecordViewDto> listDocuments(DocumentListQueryDto query) {
            return Flux.empty();
        }

        @Override
        public Flux<DocumentRecordViewDto> findExpiringDocuments(DocumentExpiryQueryDto query) {
            return Flux.empty();
        }

        @Override
        public Mono<DocumentRecordViewDto> archiveDocument(UUID documentId) {
            return Mono.empty();
        }

        @Override
        public Mono<DocumentRecordViewDto> getDocument(UUID documentId) {
            return Mono.empty();
        }
    }
}
