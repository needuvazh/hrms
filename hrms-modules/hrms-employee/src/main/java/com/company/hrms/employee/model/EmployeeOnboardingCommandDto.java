package com.company.hrms.employee.model;

import com.company.hrms.document.model.DocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EmployeeOnboardingCommandDto(
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String departmentCode,
        String jobTitle,
        String actor,
        UserAccountSetup userAccountSetup,
        LeaveBalanceSetup leaveBalanceSetup,
        AttendanceSetup attendanceSetup,
        boolean triggerOnboardingWorkflow,
        boolean sendWelcomeNotification,
        List<DocumentSetup> documents
) {

    public record UserAccountSetup(
            boolean create,
            String username,
            String rawPassword,
            String roleCode
    ) {
    }

    public record LeaveBalanceSetup(
            boolean initialize,
            UUID leaveTypeId,
            int leaveYear,
            int totalDays
    ) {
    }

    public record AttendanceSetup(
            boolean initialize,
            UUID shiftId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
    }

    public record DocumentSetup(
            DocumentType documentType,
            String fileName,
            String contentType,
            long sizeBytes,
            String objectKey,
            String checksum,
            LocalDate expiryDate
    ) {
    }
}
