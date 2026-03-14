package com.company.hrms.leave.repository;

import com.company.hrms.leave.model.*;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRepository {

    Mono<LeaveTypeDto> saveLeaveType(LeaveTypeDto leaveType);

    Mono<LeaveTypeDto> findLeaveTypeById(String tenantId, UUID leaveTypeId);

    Mono<LeaveBalanceDto> saveLeaveBalance(LeaveBalanceDto leaveBalance);

    Mono<LeaveBalanceDto> updateLeaveBalance(LeaveBalanceDto leaveBalance);

    Mono<LeaveBalanceDto> findLeaveBalance(String tenantId, UUID employeeId, UUID leaveTypeId, int leaveYear);

    Flux<LeaveBalanceDto> findLeaveBalances(String tenantId, UUID employeeId, int leaveYear);

    Mono<LeaveRequestDto> saveLeaveRequest(LeaveRequestDto leaveRequest);

    Mono<LeaveRequestDto> updateLeaveRequest(LeaveRequestDto leaveRequest);

    Mono<LeaveRequestDto> findLeaveRequestById(String tenantId, UUID leaveRequestId);

    Flux<LeaveRequestDto> findLeaveRequests(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
