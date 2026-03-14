package com.company.hrms.attendance.config;

import com.company.hrms.attendance.api.AttendanceModuleApi;
import com.company.hrms.attendance.api.AttendanceModuleClient;
import com.company.hrms.attendance.infrastructure.client.LocalAttendanceModuleClient;
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
