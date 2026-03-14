package com.company.hrms.attendance.service;

import com.company.hrms.attendance.model.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AttendanceModuleApi {

    Mono<ShiftViewDto> createShift(CreateShiftCommandDto command);

    Mono<ShiftAssignmentViewDto> assignShift(AssignShiftCommandDto command);

    Mono<PunchEventViewDto> recordPunch(RecordPunchCommandDto command);

    Flux<AttendanceRecordViewDto> attendanceByEmployee(AttendanceQueryDto query);
}
