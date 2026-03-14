package com.company.hrms.leave.service.impl;

import com.company.hrms.leave.model.*;
import com.company.hrms.leave.repository.*;
import com.company.hrms.leave.service.*;

import com.company.hrms.leave.model.ApplyLeaveCommandDto;
import com.company.hrms.leave.model.DefineLeaveTypeCommandDto;
import com.company.hrms.leave.model.InitializeLeaveBalanceCommandDto;
import com.company.hrms.leave.model.LeaveBalanceViewDto;
import com.company.hrms.leave.service.LeaveModuleApi;
import com.company.hrms.leave.model.LeaveRequestViewDto;
import com.company.hrms.leave.model.LeaveTypeViewDto;
import com.company.hrms.leave.model.ReviewLeaveCommandDto;
import com.company.hrms.leave.model.LeaveBalanceDto;
import com.company.hrms.leave.repository.LeaveRepository;
import com.company.hrms.leave.model.LeaveRequestDto;
import com.company.hrms.leave.model.LeaveStatus;
import com.company.hrms.leave.model.LeaveTypeDto;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import com.company.hrms.workflow.model.AdvanceWorkflowCommandDto;
import com.company.hrms.workflow.model.StartWorkflowCommandDto;
import com.company.hrms.workflow.model.WorkflowAction;
import com.company.hrms.workflow.service.WorkflowModuleApi;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LeaveApplicationService implements LeaveModuleApi {

    private static final String LEAVE_WORKFLOW_KEY = "leave.approval";

    private final LeaveRepository leaveRepository;
    private final WorkflowModuleApi workflowModuleApi;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final OutboxPublisher outboxPublisher;

    public LeaveApplicationService(
            LeaveRepository leaveRepository,
            WorkflowModuleApi workflowModuleApi,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            OutboxPublisher outboxPublisher
    ) {
        this.leaveRepository = leaveRepository;
        this.workflowModuleApi = workflowModuleApi;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public Mono<LeaveTypeViewDto> defineLeaveType(DefineLeaveTypeCommandDto command) {
        validateLeaveType(command);

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    Instant now = Instant.now();
                    LeaveTypeDto leaveType = new LeaveTypeDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.leaveCode().trim().toUpperCase(),
                            command.name().trim(),
                            command.paid(),
                            command.annualLimitDays(),
                            true,
                            now,
                            now);
                    return leaveRepository.saveLeaveType(leaveType).map(this::toLeaveTypeView);
                });
    }

    @Override
    public Mono<LeaveBalanceViewDto> initializeLeaveBalance(InitializeLeaveBalanceCommandDto command) {
        validateInitializeBalance(command);

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMap(tenantId -> leaveRepository.findLeaveTypeById(tenantId, command.leaveTypeId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "LEAVE_TYPE_NOT_FOUND", "Leave type not found")))
                        .flatMap(leaveType -> {
                            Instant now = Instant.now();
                            LeaveBalanceDto leaveBalance = new LeaveBalanceDto(
                                    UUID.randomUUID(),
                                    tenantId,
                                    command.employeeId(),
                                    command.leaveTypeId(),
                                    command.leaveYear(),
                                    command.totalDays(),
                                    0,
                                    command.totalDays(),
                                    now,
                                    now);
                            return leaveRepository.saveLeaveBalance(leaveBalance).map(this::toLeaveBalanceView);
                        }));
    }

    @Override
    public Mono<LeaveRequestViewDto> applyLeave(ApplyLeaveCommandDto command) {
        validateApply(command);

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    int requestedDays = calculateDays(command.fromDate(), command.toDate());
                    int leaveYear = command.fromDate().getYear();

                    return leaveRepository.findLeaveBalance(tenantId, command.employeeId(), command.leaveTypeId(), leaveYear)
                            .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_BALANCE_NOT_FOUND", "Leave balance not initialized")))
                            .flatMap(balance -> {
                                if (balance.remainingDays() < requestedDays) {
                                    return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_LEAVE_BALANCE", "Insufficient leave balance"));
                                }

                                Instant now = Instant.now();
                                LeaveRequestDto draft = new LeaveRequestDto(
                                        UUID.randomUUID(),
                                        tenantId,
                                        command.employeeId(),
                                        command.leaveTypeId(),
                                        command.fromDate(),
                                        command.toDate(),
                                        requestedDays,
                                        command.reason(),
                                        LeaveStatus.SUBMITTED,
                                        null,
                                        command.requestedBy().trim(),
                                        null,
                                        now,
                                        now);

                                return leaveRepository.saveLeaveRequest(draft)
                                        .flatMap(saved -> workflowModuleApi.startWorkflow(new StartWorkflowCommandDto(
                                                        LEAVE_WORKFLOW_KEY,
                                                        "LEAVE_REQUEST",
                                                        saved.id().toString(),
                                                        command.requestedBy().trim(),
                                                        command.reason()))
                                                .flatMap(workflow -> leaveRepository.updateLeaveRequest(new LeaveRequestDto(
                                                        saved.id(),
                                                        saved.tenantId(),
                                                        saved.employeeId(),
                                                        saved.leaveTypeId(),
                                                        saved.fromDate(),
                                                        saved.toDate(),
                                                        saved.requestedDays(),
                                                        saved.reason(),
                                                        saved.status(),
                                                        workflow.id(),
                                                        saved.requestedBy(),
                                                        saved.reviewedBy(),
                                                        saved.createdAt(),
                                                        Instant.now())))
                                                .flatMap(updated -> outboxPublisher.publish(new OutboxEvent(
                                                                tenantId,
                                                                "LEAVE_REQUEST",
                                                                updated.id().toString(),
                                                                "LeaveRequested",
                                                                leaveRequestedPayload(updated),
                                                                now))
                                                        .thenReturn(updated)))
                                        .map(this::toLeaveRequestView);
                            });
                });
    }

    @Override
    public Mono<LeaveRequestViewDto> reviewLeave(ReviewLeaveCommandDto command) {
        validateReview(command);

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMap(tenantId -> leaveRepository.findLeaveRequestById(tenantId, command.leaveRequestId())
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "LEAVE_REQUEST_NOT_FOUND", "Leave request not found")))
                        .flatMap(existing -> {
                            if (existing.status() != LeaveStatus.SUBMITTED) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_ALREADY_DECIDED", "Leave request already decided"));
                            }
                            if (existing.workflowInstanceId() == null) {
                                return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "WORKFLOW_INSTANCE_REQUIRED", "Workflow instance is required"));
                            }

                            WorkflowAction action = mapAction(command.decision());
                            return workflowModuleApi.advanceWorkflow(new AdvanceWorkflowCommandDto(
                                            existing.workflowInstanceId(),
                                            action,
                                            command.reviewer().trim(),
                                            command.comments()))
                                    .then(updateLeaveDecision(existing, command));
                        })
                        .map(this::toLeaveRequestView));
    }

    @Override
    public Flux<LeaveBalanceViewDto> balances(UUID employeeId, int leaveYear) {
        if (employeeId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required"));
        }

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMapMany(tenantId -> leaveRepository.findLeaveBalances(tenantId, employeeId, leaveYear)
                        .map(this::toLeaveBalanceView));
    }

    @Override
    public Flux<LeaveRequestViewDto> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate) {
        if (employeeId == null) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required"));
        }
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
            return Flux.error(new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "Invalid date range"));
        }

        return enablementGuard.requireModuleEnabled("leave")
                .then(requireTenant())
                .flatMapMany(tenantId -> leaveRepository.findLeaveRequests(tenantId, employeeId, fromDate, toDate)
                        .map(this::toLeaveRequestView));
    }

    private Mono<LeaveRequestDto> updateLeaveDecision(LeaveRequestDto existing, ReviewLeaveCommandDto command) {
        Instant now = Instant.now();
        LeaveRequestDto updated = existing.decide(command.decision(), command.reviewer().trim(), now);

        Mono<LeaveRequestDto> decisionUpdate = leaveRepository.updateLeaveRequest(updated);
        if (command.decision() != LeaveStatus.APPROVED) {
            return decisionUpdate;
        }

        int leaveYear = existing.fromDate().getYear();
        return leaveRepository.findLeaveBalance(existing.tenantId(), existing.employeeId(), existing.leaveTypeId(), leaveYear)
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_BALANCE_NOT_FOUND", "Leave balance not found")))
                .flatMap(balance -> {
                    if (balance.remainingDays() < existing.requestedDays()) {
                        return Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_LEAVE_BALANCE", "Insufficient leave balance for approval"));
                    }
                    return leaveRepository.updateLeaveBalance(balance.consume(existing.requestedDays(), now)).then(decisionUpdate);
                });
    }

    private WorkflowAction mapAction(LeaveStatus decision) {
        return switch (decision) {
            case APPROVED -> WorkflowAction.APPROVE;
            case REJECTED -> WorkflowAction.REJECT;
            case CANCELLED -> WorkflowAction.CANCEL;
            case SUBMITTED -> throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_LEAVE_DECISION", "Invalid leave decision");
        };
    }

    private int calculateDays(LocalDate fromDate, LocalDate toDate) {
        return (int) ChronoUnit.DAYS.between(fromDate, toDate) + 1;
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validateLeaveType(DefineLeaveTypeCommandDto command) {
        if (!StringUtils.hasText(command.leaveCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_CODE_REQUIRED", "Leave code is required");
        }
        if (!StringUtils.hasText(command.name())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_NAME_REQUIRED", "Leave name is required");
        }
        if (command.annualLimitDays() <= 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_LIMIT_REQUIRED", "Annual leave limit must be greater than zero");
        }
    }

    private void validateInitializeBalance(InitializeLeaveBalanceCommandDto command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }
        if (command.leaveTypeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_TYPE_REQUIRED", "Leave type id is required");
        }
        if (command.leaveYear() <= 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_YEAR_REQUIRED", "Leave year is required");
        }
        if (command.totalDays() < 0) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_TOTAL_DAYS", "Total days cannot be negative");
        }
    }

    private void validateApply(ApplyLeaveCommandDto command) {
        if (command.employeeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMPLOYEE_REQUIRED", "EmployeeDto id is required");
        }
        if (command.leaveTypeId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_TYPE_REQUIRED", "Leave type id is required");
        }
        if (command.fromDate() == null || command.toDate() == null || command.toDate().isBefore(command.fromDate())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "INVALID_LEAVE_DATES", "Invalid leave dates");
        }
        if (!StringUtils.hasText(command.requestedBy())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "REQUESTED_BY_REQUIRED", "Requested by is required");
        }
    }

    private void validateReview(ReviewLeaveCommandDto command) {
        if (command.leaveRequestId() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_REQUEST_REQUIRED", "Leave request id is required");
        }
        if (command.decision() == null) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "LEAVE_DECISION_REQUIRED", "Leave decision is required");
        }
        if (!StringUtils.hasText(command.reviewer())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "REVIEWER_REQUIRED", "Reviewer is required");
        }
    }

    private LeaveTypeViewDto toLeaveTypeView(LeaveTypeDto leaveType) {
        return new LeaveTypeViewDto(
                leaveType.id(),
                leaveType.tenantId(),
                leaveType.leaveCode(),
                leaveType.name(),
                leaveType.paid(),
                leaveType.annualLimitDays(),
                leaveType.active(),
                leaveType.createdAt(),
                leaveType.updatedAt());
    }

    private LeaveBalanceViewDto toLeaveBalanceView(LeaveBalanceDto leaveBalance) {
        return new LeaveBalanceViewDto(
                leaveBalance.id(),
                leaveBalance.tenantId(),
                leaveBalance.employeeId(),
                leaveBalance.leaveTypeId(),
                leaveBalance.leaveYear(),
                leaveBalance.totalDays(),
                leaveBalance.usedDays(),
                leaveBalance.remainingDays(),
                leaveBalance.createdAt(),
                leaveBalance.updatedAt());
    }

    private LeaveRequestViewDto toLeaveRequestView(LeaveRequestDto leaveRequest) {
        return new LeaveRequestViewDto(
                leaveRequest.id(),
                leaveRequest.tenantId(),
                leaveRequest.employeeId(),
                leaveRequest.leaveTypeId(),
                leaveRequest.fromDate(),
                leaveRequest.toDate(),
                leaveRequest.requestedDays(),
                leaveRequest.reason(),
                leaveRequest.status(),
                leaveRequest.workflowInstanceId(),
                leaveRequest.requestedBy(),
                leaveRequest.reviewedBy(),
                leaveRequest.createdAt(),
                leaveRequest.updatedAt());
    }

    private String leaveRequestedPayload(LeaveRequestDto leaveRequest) {
        return "{\"leaveRequestId\":\"%s\",\"employeeId\":\"%s\",\"fromDate\":\"%s\",\"toDate\":\"%s\",\"requestedDays\":%d}"
                .formatted(
                        leaveRequest.id(),
                        leaveRequest.employeeId(),
                        leaveRequest.fromDate(),
                        leaveRequest.toDate(),
                        leaveRequest.requestedDays());
    }
}
