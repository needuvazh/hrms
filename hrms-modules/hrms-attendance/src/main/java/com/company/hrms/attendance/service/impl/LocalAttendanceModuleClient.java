package com.company.hrms.attendance.service.impl;

import com.company.hrms.attendance.model.*;
import com.company.hrms.attendance.repository.*;
import com.company.hrms.attendance.service.*;

import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.service.AttendanceModuleApi;
import com.company.hrms.attendance.service.AttendanceModuleClient;
import com.company.hrms.attendance.model.AttendanceQueryDto;
import com.company.hrms.attendance.model.AttendanceRecordViewDto;
import com.company.hrms.attendance.model.CreateShiftCommandDto;
import com.company.hrms.attendance.model.PunchEventViewDto;
import com.company.hrms.attendance.model.RecordPunchCommandDto;
import com.company.hrms.attendance.model.ShiftAssignmentViewDto;
import com.company.hrms.attendance.model.ShiftViewDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class LocalAttendanceModuleClient implements AttendanceModuleClient {

    private final AttendanceModuleApi delegate;

    public LocalAttendanceModuleClient(AttendanceModuleApi delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<ShiftViewDto> createShift(CreateShiftCommandDto command) {
        return delegate.createShift(command);
    }

    @Override
    public Mono<ShiftAssignmentViewDto> assignShift(AssignShiftCommandDto command) {
        return delegate.assignShift(command);
    }

    @Override
    public Mono<PunchEventViewDto> recordPunch(RecordPunchCommandDto command) {
        return delegate.recordPunch(command);
    }

    @Override
    public Flux<AttendanceRecordViewDto> attendanceByEmployee(AttendanceQueryDto query) {
        return delegate.attendanceByEmployee(query);
    }
}
