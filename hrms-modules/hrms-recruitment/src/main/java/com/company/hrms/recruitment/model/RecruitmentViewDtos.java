package com.company.hrms.recruitment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Job Requisition View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JobRequisitionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final UUID departmentId;
    private final String departmentName;
    private final UUID jobPositionId;
    private final String jobPositionName;
    private final String requisitionCode;
    private final Integer numberOfPositions;
    private final LocalDate requisitionDate;
    private final LocalDate targetClosureDate;
    private final String requisitionStatus; // DRAFT, APPROVED, OPEN, CLOSED, CANCELLED
    private final String jobDescription;
    private final String requiredQualifications;
    private final String requiredExperience;
    private final BigDecimal budgetedSalary;
    private final String salaryRange;
    private final String jobType; // PERMANENT, CONTRACT, TEMPORARY
    private final String employmentType; // FULL_TIME, PART_TIME, INTERNSHIP
    private final UUID createdBy;
    private final UUID approvedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Candidate View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class CandidateViewDto {
    private final UUID id;
    private final String tenantId;
    private final String candidateCode;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String nationality;
    private final String currentCompany;
    private final String currentDesignation;
    private final String currentSalary;
    private final String expectedSalary;
    private final String highestQualification;
    private final Integer totalExperienceYears;
    private final String resumeUrl;
    private final String candidateStatus; // NEW, SHORTLISTED, INTERVIEWED, SELECTED, REJECTED, OFFER_EXTENDED, JOINED
    private final String candidateSource; // PORTAL, REFERRAL, AGENCY, DIRECT, LINKEDIN
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Job Application View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class JobApplicationViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID jobRequisitionId;
    private final String jobRequisitionCode;
    private final String jobPositionName;
    private final UUID candidateId;
    private final String candidateCode;
    private final String candidateName;
    private final String candidateEmail;
    private final String candidatePhone;
    private final LocalDate applicationDate;
    private final String applicationStatus; // APPLIED, SHORTLISTED, REJECTED, SELECTED
    private final String interviewStatus; // NOT_SCHEDULED, SCHEDULED, COMPLETED, PASSED, FAILED
    private final LocalDate interviewDate;
    private final String interviewRound; // PHONE, TECHNICAL, HR, FINAL
    private final String interviewerName;
    private final String interviewFeedback;
    private final Integer interviewRating;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Offer Letter View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class OfferLetterViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID jobApplicationId;
    private final UUID candidateId;
    private final String candidateCode;
    private final String candidateName;
    private final String candidateEmail;
    private final UUID jobPositionId;
    private final String jobPositionName;
    private final String offerCode;
    private final LocalDate offerDate;
    private final LocalDate offerValidityDate;
    private final BigDecimal offeredSalary;
    private final String salaryFrequency;
    private final String jobType; // PERMANENT, CONTRACT, TEMPORARY
    private final LocalDate expectedJoiningDate;
    private final String offerStatus; // DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED
    private final LocalDate acceptanceDate;
    private final String offerDocumentUrl;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Interview Schedule View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class InterviewScheduleViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID jobApplicationId;
    private final String candidateCode;
    private final String candidateName;
    private final String candidateEmail;
    private final LocalDate interviewDate;
    private final String interviewTime;
    private final String interviewRound; // PHONE, TECHNICAL, HR, FINAL
    private final String interviewMode; // IN_PERSON, VIDEO_CALL, PHONE_CALL
    private final String interviewLocation;
    private final String interviewerName;
    private final String interviewerEmail;
    private final String meetingLink;
    private final String scheduleStatus; // SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Interview Feedback View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class InterviewFeedbackViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID interviewScheduleId;
    private final UUID jobApplicationId;
    private final String candidateCode;
    private final String candidateName;
    private final String interviewerName;
    private final String interviewRound;
    private final Integer technicalSkillsRating;
    private final Integer communicationRating;
    private final Integer cultureFitRating;
    private final Integer overallRating;
    private final String strengths;
    private final String weaknesses;
    private final String recommendation; // PROCEED, HOLD, REJECT
    private final String feedbackComments;
    private final String feedbackStatus; // DRAFT, SUBMITTED
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Onboarding Checklist View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class OnboardingChecklistViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate joiningDate;
    private final String checklistStatus; // PENDING, IN_PROGRESS, COMPLETED
    private final OnboardingTask documentCollection;
    private final OnboardingTask systemAccess;
    private final OnboardingTask trainingSchedule;
    private final OnboardingTask equipmentAllocation;
    private final OnboardingTask orientationSession;
    private final OnboardingTask departmentIntroduction;
    private final OnboardingTask policyAcknowledgment;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Onboarding Task
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class OnboardingTask {
    private final String taskCode;
    private final String taskName;
    private final String taskNameArabic;
    private final String taskStatus; // PENDING, IN_PROGRESS, COMPLETED
    private final UUID assignedTo;
    private final LocalDate dueDate;
    private final LocalDate completionDate;
    private final String remarks;
}
