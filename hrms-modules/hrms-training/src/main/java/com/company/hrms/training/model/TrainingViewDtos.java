package com.company.hrms.training.model;

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
 * Training Need Analysis View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TrainingNeedAnalysisViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String skillGap;
    private final String trainingNeed;
    private final String trainingCategory; // TECHNICAL, SOFT_SKILLS, COMPLIANCE, LEADERSHIP
    private final String priority; // HIGH, MEDIUM, LOW
    private final String tnaStatus; // IDENTIFIED, APPROVED, SCHEDULED, COMPLETED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Training Program View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TrainingProgramViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String programCode;
    private final String programName;
    private final String programNameArabic;
    private final String programDescription;
    private final String trainingCategory;
    private final String trainingType; // INTERNAL, EXTERNAL, ONLINE, HYBRID
    private final LocalDate programStartDate;
    private final LocalDate programEndDate;
    private final Integer totalDays;
    private final Integer totalHours;
    private final String trainerName;
    private final String trainerQualifications;
    private final String trainingLocation;
    private final String programStatus; // DRAFT, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    private final Integer targetParticipants;
    private final Integer actualParticipants;
    private final BigDecimal programCost;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Course View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class CourseViewDto {
    private final UUID id;
    private final String tenantId;
    private final String courseCode;
    private final String courseName;
    private final String courseNameArabic;
    private final String courseDescription;
    private final String courseCategory;
    private final String courseLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private final Integer durationHours;
    private final String courseType; // CLASSROOM, ONLINE, HYBRID, SELF_PACED
    private final String courseStatus; // ACTIVE, INACTIVE, ARCHIVED
    private final String courseProvider;
    private final BigDecimal courseCost;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Training Enrollment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TrainingEnrollmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID trainingProgramId;
    private final String programCode;
    private final String programName;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate enrollmentDate;
    private final String enrollmentStatus; // ENROLLED, COMPLETED, DROPPED, DEFERRED
    private final String attendanceStatus; // ATTENDED, ABSENT, PARTIAL
    private final Integer attendanceDays;
    private final Integer totalDays;
    private final BigDecimal attendancePercentage;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Training Assessment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TrainingAssessmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID trainingEnrollmentId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String assessmentType; // PRE_TEST, POST_TEST, PRACTICAL, PROJECT
    private final Integer totalMarks;
    private final Integer obtainedMarks;
    private final BigDecimal percentage;
    private final String assessmentStatus; // PASS, FAIL
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Certification View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class CertificationViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String certificationName;
    private final String certificationNameArabic;
    private final String certificationCode;
    private final String issuingBody;
    private final LocalDate certificationDate;
    private final LocalDate expiryDate;
    private final String certificationStatus; // ACTIVE, EXPIRED, RENEWED
    private final String certificateUrl;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Training Feedback View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TrainingFeedbackViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID trainingEnrollmentId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String programName;
    private final Integer contentRating;
    private final Integer trainerRating;
    private final Integer facilitiesRating;
    private final Integer organizationRating;
    private final BigDecimal overallRating;
    private final String strengths;
    private final String areasForImprovement;
    private final String feedbackComments;
    private final String feedbackStatus; // DRAFT, SUBMITTED
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Training Budget View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class TrainingBudgetViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final Integer budgetYear;
    private final BigDecimal allocatedBudget;
    private final BigDecimal utilizedBudget;
    private final BigDecimal remainingBudget;
    private final String budgetStatus; // DRAFT, APPROVED, ACTIVE, CLOSED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Skill Inventory View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class SkillInventoryViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String skillCode;
    private final String skillName;
    private final String skillCategory;
    private final Integer proficiencyLevel; // 1-5
    private final String proficiencyStatus; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private final LocalDate acquiredDate;
    private final String skillStatus; // ACTIVE, INACTIVE, DEPRECATED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
