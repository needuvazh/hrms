package com.company.hrms.leave.infrastructure.web;

import com.company.hrms.leave.api.ApplyLeaveCommand;
import com.company.hrms.leave.api.DefineLeaveTypeCommand;
import com.company.hrms.leave.api.InitializeLeaveBalanceCommand;
import com.company.hrms.leave.api.LeaveBalanceView;
import com.company.hrms.leave.api.LeaveModuleApi;
import com.company.hrms.leave.api.LeaveRequestView;
import com.company.hrms.leave.api.LeaveTypeView;
import com.company.hrms.leave.api.ReviewLeaveCommand;
import com.company.hrms.leave.domain.LeaveStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/leaves")
@Tag(name = "Leave", description = "Leave policy, balance, request, and approval APIs")
public class LeaveController {

    private final LeaveModuleApi leaveModuleApi;

    public LeaveController(LeaveModuleApi leaveModuleApi) {
        this.leaveModuleApi = leaveModuleApi;
    }

    @PostMapping("/types")
    @Operation(summary = "Define leave type", description = "Creates a leave type policy such as annual, sick, or maternity leave.")
    public Mono<LeaveTypeView> defineLeaveType(@Valid @RequestBody DefineLeaveTypeRequest request) {
        return leaveModuleApi.defineLeaveType(new DefineLeaveTypeCommand(
                request.leaveCode(),
                request.name(),
                request.paid(),
                request.annualLimitDays()));
    }

    @PostMapping("/balances")
    @Operation(summary = "Initialize leave balance", description = "Creates an opening leave balance for an employee and leave type in a given year.")
    public Mono<LeaveBalanceView> initializeLeaveBalance(@Valid @RequestBody InitializeLeaveBalanceRequest request) {
        return leaveModuleApi.initializeLeaveBalance(new InitializeLeaveBalanceCommand(
                request.employeeId(),
                request.leaveTypeId(),
                request.leaveYear(),
                request.totalDays()));
    }

    @PostMapping("/requests")
    @Operation(summary = "Apply leave", description = "Submits a leave request for an employee and date range.")
    public Mono<LeaveRequestView> applyLeave(@Valid @RequestBody ApplyLeaveRequest request) {
        return leaveModuleApi.applyLeave(new ApplyLeaveCommand(
                request.employeeId(),
                request.leaveTypeId(),
                request.fromDate(),
                request.toDate(),
                request.reason(),
                request.requestedBy()));
    }

    @PostMapping("/requests/{leaveRequestId}/decision")
    @Operation(summary = "Review leave request", description = "Approves or rejects a leave request and records reviewer comments.")
    public Mono<LeaveRequestView> reviewLeave(
            @Parameter(description = "Unique leave request identifier", example = "d6ddf1ed-4ef9-41fc-a8e8-50d5486f166a")
            @PathVariable("leaveRequestId") UUID leaveRequestId,
            @Valid @RequestBody ReviewLeaveRequest request
    ) {
        return leaveModuleApi.reviewLeave(new ReviewLeaveCommand(
                leaveRequestId,
                request.decision(),
                request.reviewer(),
                request.comments()));
    }

    @GetMapping("/balances")
    @Operation(summary = "Employee leave balances", description = "Returns leave balances for one employee and leave year.")
    public Flux<LeaveBalanceView> balances(
            @Parameter(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @RequestParam("employeeId") UUID employeeId,
            @Parameter(description = "Leave year", example = "2026")
            @RequestParam("year") int year
    ) {
        return leaveModuleApi.balances(employeeId, year);
    }

    @GetMapping("/history")
    @Operation(summary = "Employee leave history", description = "Returns leave requests for one employee within the provided date range.")
    public Flux<LeaveRequestView> leaveHistory(
            @Parameter(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @RequestParam("employeeId") UUID employeeId,
            @Parameter(description = "Start date in yyyy-MM-dd format", example = "2026-01-01")
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date in yyyy-MM-dd format", example = "2026-12-31")
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return leaveModuleApi.leaveHistory(employeeId, fromDate, toDate);
    }

    public record DefineLeaveTypeRequest(
            @Schema(description = "Business leave code", example = "ANNUAL")
            @NotBlank String leaveCode,
            @Schema(description = "Leave type display name", example = "Annual Leave")
            @NotBlank String name,
            @Schema(description = "Whether leave days are paid")
            boolean paid,
            @Schema(description = "Maximum leave days allowed per year", example = "30")
            @Min(1) @Max(365) int annualLimitDays
    ) {
    }

    public record InitializeLeaveBalanceRequest(
            @Schema(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @NotNull UUID employeeId,
            @Schema(description = "Leave type identifier", example = "73f12cd8-5f0f-4513-91f0-56de3374a9cc")
            @NotNull UUID leaveTypeId,
            @Schema(description = "Target leave year", example = "2026")
            int leaveYear,
            @Schema(description = "Opening leave days", example = "30")
            @Min(0) int totalDays
    ) {
    }

    public record ApplyLeaveRequest(
            @Schema(description = "Employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @NotNull UUID employeeId,
            @Schema(description = "Leave type identifier", example = "73f12cd8-5f0f-4513-91f0-56de3374a9cc")
            @NotNull UUID leaveTypeId,
            @Schema(description = "Leave start date", example = "2026-02-10")
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Schema(description = "Leave end date", example = "2026-02-14")
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Schema(description = "Employee reason for leave", example = "Family travel")
            String reason,
            @Schema(description = "User submitting the leave request", example = "emp.20001")
            @NotBlank String requestedBy
    ) {
    }

    public record ReviewLeaveRequest(
            @Schema(description = "Leave decision status", example = "APPROVED")
            @NotNull LeaveStatus decision,
            @Schema(description = "Reviewer user identifier", example = "manager.100")
            @NotBlank String reviewer,
            @Schema(description = "Optional review comments", example = "Approved based on project plan")
            String comments
    ) {
    }
}
