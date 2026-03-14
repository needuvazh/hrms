package com.company.hrms.attendance.infrastructure.client;

import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceModuleClient;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.AttendanceRecordView;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.PunchEventView;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.api.ShiftAssignmentView;
import com.company.hrms.attendance.api.ShiftView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalAttendanceModuleClient implements AttendanceModuleClient {

    private final AttendanceModuleApi delegate;

    public LocalAttendanceModuleClient(AttendanceModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<ShiftView> createShift(CreateShiftCommand command) {
        return delegate.createShift(command);
    }

    @Override
    public Mono<ShiftAssignmentView> assignShift(AssignShiftCommand command) {
        return delegate.assignShift(command);
    }

    @Override
    public Mono<PunchEventView> recordPunch(RecordPunchCommand command) {
        return delegate.recordPunch(command);
    }

    @Override
    public Flux<AttendanceRecordView> attendanceByEmployee(AttendanceQuery query) {
        return delegate.attendanceByEmployee(query);
    }
}
