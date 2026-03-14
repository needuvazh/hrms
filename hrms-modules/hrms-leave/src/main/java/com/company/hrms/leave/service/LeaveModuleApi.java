package com.company.hrms.leave.service;

import com.company.hrms.leave.model.*;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveModuleApi {

    Mono<LeaveTypeViewDto> defineLeaveType(DefineLeaveTypeCommandDto command);

    Mono<LeaveBalanceViewDto> initializeLeaveBalance(InitializeLeaveBalanceCommandDto command);

    Mono<LeaveRequestViewDto> applyLeave(ApplyLeaveCommandDto command);

    Mono<LeaveRequestViewDto> reviewLeave(ReviewLeaveCommandDto command);

    Flux<LeaveBalanceViewDto> balances(UUID employeeId, int leaveYear);

    Flux<LeaveRequestViewDto> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
