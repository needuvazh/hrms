package com.company.hrms.leave.api;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveModuleApi {

    Mono<LeaveTypeView> defineLeaveType(DefineLeaveTypeCommand command);

    Mono<LeaveBalanceView> initializeLeaveBalance(InitializeLeaveBalanceCommand command);

    Mono<LeaveRequestView> applyLeave(ApplyLeaveCommand command);

    Mono<LeaveRequestView> reviewLeave(ReviewLeaveCommand command);

    Flux<LeaveBalanceView> balances(UUID employeeId, int leaveYear);

    Flux<LeaveRequestView> leaveHistory(UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
