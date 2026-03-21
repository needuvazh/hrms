package com.company.hrms.contracts.leave;

import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Create Leave Application command DTO.
 * Request DTO for submitting a new leave application.
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateLeaveApplicationCommandDto {
    private final String tenantId;
    private final UUID employeeId;
    private final UUID leaveTypeId;
    private final LocalDate leaveStartDate;
    private final LocalDate leaveEndDate;
    private final Boolean isHalfDay;
    private final String halfDayType; // FIRST_HALF, SECOND_HALF
    private final String reason;
    private final String contactPhoneNumber;
    private final String contactAddress;
    private final String alternateContactPerson;
    private final Boolean isEncashmentRequested;
    private final Boolean isAirTicketEncashmentRequested;
    private final String attachmentUrl;
}
