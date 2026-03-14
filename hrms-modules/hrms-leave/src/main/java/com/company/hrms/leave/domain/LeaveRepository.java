package com.company.hrms.leave.domain;

import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRepository {

    Mono<LeaveType> saveLeaveType(LeaveType leaveType);

    Mono<LeaveType> findLeaveTypeById(String tenantId, UUID leaveTypeId);

    Mono<LeaveBalance> saveLeaveBalance(LeaveBalance leaveBalance);

    Mono<LeaveBalance> updateLeaveBalance(LeaveBalance leaveBalance);

    Mono<LeaveBalance> findLeaveBalance(String tenantId, UUID employeeId, UUID leaveTypeId, int leaveYear);

    Flux<LeaveBalance> findLeaveBalances(String tenantId, UUID employeeId, int leaveYear);

    Mono<LeaveRequest> saveLeaveRequest(LeaveRequest leaveRequest);

    Mono<LeaveRequest> updateLeaveRequest(LeaveRequest leaveRequest);

    Mono<LeaveRequest> findLeaveRequestById(String tenantId, UUID leaveRequestId);

    Flux<LeaveRequest> findLeaveRequests(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate);
}
