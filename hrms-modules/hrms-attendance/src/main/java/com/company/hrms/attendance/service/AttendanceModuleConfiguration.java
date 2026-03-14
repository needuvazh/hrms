package com.company.hrms.attendance.service;

import com.company.hrms.attendance.model.*;

import com.company.hrms.attendance.service.AttendanceModuleApi;
import com.company.hrms.attendance.service.AttendanceModuleClient;
import com.company.hrms.attendance.service.impl.LocalAttendanceModuleClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceModuleConfiguration {

    @Bean
    @ConditionalOnMissingBean(AttendanceModuleClient.class)
    AttendanceModuleClient localAttendanceModuleClient(AttendanceModuleApi attendanceModuleApi) {
        return new LocalAttendanceModuleClient(attendanceModuleApi);
    }
}
