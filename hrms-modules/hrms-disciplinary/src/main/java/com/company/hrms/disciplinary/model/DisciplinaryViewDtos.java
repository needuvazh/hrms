package com.company.hrms.disciplinary.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Disciplinary Action View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DisciplinaryActionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String actionCode;
    private final String actionType; // VERBAL_WARNING, WRITTEN_WARNING, SUSPENSION, TERMINATION
    private final LocalDate actionDate;
    private final String reason;
    private final String detailedDescription;
    private final String actionStatus; // DRAFT, ISSUED, ACKNOWLEDGED, APPEALED, CLOSED
    private final UUID issuedBy;
    private final String issuedByName;
    private final LocalDate acknowledgedDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Warning Letter View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class WarningLetterViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID disciplinaryActionId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String letterCode;
    private final String warningType; // VERBAL, WRITTEN, FINAL
    private final LocalDate letterDate;
    private final String letterSubject;
    private final String letterContent;
    private final LocalDate effectiveDate;
    private final String letterStatus; // DRAFT, ISSUED, ACKNOWLEDGED
    private final String documentUrl;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Grievance View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GrievanceViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String grievanceCode;
    private final LocalDate grievanceDate;
    private final String grievanceCategory; // HARASSMENT, DISCRIMINATION, UNFAIR_TREATMENT, SAFETY, OTHERS
    private final String grievanceSubject;
    private final String grievanceDescription;
    private final String grievanceStatus; // FILED, ACKNOWLEDGED, UNDER_INVESTIGATION, RESOLVED, CLOSED
    private final UUID assignedTo;
    private final String assignedToName;
    private final LocalDate targetResolutionDate;
    private final String resolution;
    private final LocalDate resolutionDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Grievance Investigation View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GrievanceInvestigationViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID grievanceId;
    private final String grievanceCode;
    private final UUID investigatorId;
    private final String investigatorName;
    private final LocalDate investigationStartDate;
    private final LocalDate investigationEndDate;
    private final String investigationStatus; // IN_PROGRESS, COMPLETED, PENDING
    private final String investigationFindings;
    private final String investigationRecommendation;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Grievance Resolution View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class GrievanceResolutionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID grievanceId;
    private final String grievanceCode;
    private final LocalDate resolutionDate;
    private final String resolutionType; // SETTLEMENT, COMPENSATION, ACTION_TAKEN, DISMISSED
    private final String resolutionDetails;
    private final String compensationAmount;
    private final String actionTaken;
    private final String resolutionStatus; // PROPOSED, ACCEPTED, REJECTED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Appeal View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class AppealViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID disciplinaryActionId;
    private final UUID grievanceId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String appealCode;
    private final LocalDate appealDate;
    private final String appealReason;
    private final String appealDescription;
    private final String appealStatus; // FILED, ACKNOWLEDGED, UNDER_REVIEW, APPROVED, REJECTED
    private final UUID reviewedBy;
    private final String reviewedByName;
    private final LocalDate reviewDate;
    private final String reviewFindings;
    private final String appealOutcome;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Conduct Record View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ConductRecordViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String conductType; // POSITIVE, NEGATIVE, NEUTRAL
    private final LocalDate recordDate;
    private final String description;
    private final String recordStatus; // ACTIVE, ARCHIVED
    private final UUID recordedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Disciplinary Committee View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class DisciplinaryCommitteeViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String committeeName;
    private final String committeeName_Arabic;
    private final String committeeType; // DISCIPLINARY, GRIEVANCE, APPEALS
    private final String committeeStatus; // ACTIVE, INACTIVE
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Committee Member View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class CommitteeMemberViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID committeeId;
    private final String committeeName;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String memberRole; // CHAIRMAN, MEMBER, SECRETARY
    private final String memberStatus; // ACTIVE, INACTIVE
    private final LocalDate appointmentDate;
    private final LocalDate resignationDate;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Hearing View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class HearingViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID disciplinaryActionId;
    private final UUID grievanceId;
    private final String hearingCode;
    private final LocalDate hearingDate;
    private final String hearingTime;
    private final String hearingLocation;
    private final String hearingStatus; // SCHEDULED, HELD, POSTPONED, CANCELLED
    private final String hearingOutcome;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
