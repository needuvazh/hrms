package com.company.hrms.attendance.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AttendanceModuleApi {

    Mono<ShiftView> createShift(CreateShiftCommand command);

    Mono<ShiftAssignmentView> assignShift(AssignShiftCommand command);

    Mono<PunchEventView> recordPunch(RecordPunchCommand command);

    Flux<AttendanceRecordView> attendanceByEmployee(AttendanceQuery query);
}
