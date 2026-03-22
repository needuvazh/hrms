package com.company.hrms.performance.model;

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
 * Appraisal View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AppraisalViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String employeeDesignation;
    private final UUID appraisalPeriodId;
    private final String appraisalPeriodName;
    private final LocalDate appraisalStartDate;
    private final LocalDate appraisalEndDate;
    private final String appraisalType; // ANNUAL, MID_YEAR, PROBATION, PROMOTION
    private final UUID appraiserId;
    private final String appraiserName;
    private final BigDecimal performanceRating;
    private final String performanceGrade; // EXCELLENT, GOOD, SATISFACTORY, NEEDS_IMPROVEMENT, POOR
    private final String appraisalStatus; // DRAFT, SUBMITTED, APPROVED, COMPLETED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Appraisal Goal View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class AppraisalGoalViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID appraisalId;
    private final String goalCode;
    private final String goalName;
    private final String goalDescription;
    private final String goalCategory; // INDIVIDUAL, TEAM, ORGANIZATIONAL
    private final String goalType; // QUANTITATIVE, QUALITATIVE
    private final String goalStatus; // DRAFT, ACTIVE, ACHIEVED, MISSED
    private final Integer weightage;
    private final String targetValue;
    private final String actualValue;
    private final BigDecimal achievementPercentage;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Competency Assessment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class CompetencyAssessmentViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID appraisalId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String competencyCode;
    private final String competencyName;
    private final String competencyCategory;
    private final Integer requiredLevel;
    private final Integer assessedLevel;
    private final String assessmentStatus; // NOT_ASSESSED, ASSESSED, APPROVED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * 360 Degree Feedback View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class FeedbackViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID appraisalId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final UUID feedbackProviderId;
    private final String feedbackProviderName;
    private final String feedbackProviderRelationship; // MANAGER, PEER, SUBORDINATE, CUSTOMER
    private final Integer communicationRating;
    private final Integer leadershipRating;
    private final Integer teamworkRating;
    private final Integer innovationRating;
    private final Integer customerFocusRating;
    private final BigDecimal overallRating;
    private final String strengths;
    private final String areasForImprovement;
    private final String feedbackComments;
    private final String feedbackStatus; // DRAFT, SUBMITTED, REVIEWED
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Performance Development Plan View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PerformanceDevelopmentPlanViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final UUID appraisalId;
    private final String planCode;
    private final LocalDate planStartDate;
    private final LocalDate planEndDate;
    private final String developmentArea;
    private final String developmentObjective;
    private final String actionItems;
    private final String supportRequired;
    private final String planStatus; // DRAFT, ACTIVE, COMPLETED, CANCELLED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Performance Improvement Plan View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PerformanceImprovementPlanViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String pipCode;
    private final LocalDate pipStartDate;
    private final LocalDate pipEndDate;
    private final Integer pipDurationDays;
    private final String performanceIssue;
    private final String expectedImprovement;
    private final String supportProvided;
    private final String pipStatus; // ACTIVE, COMPLETED, FAILED, EXTENDED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Appraisal Period View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class AppraisalPeriodViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID companyId;
    private final String companyName;
    private final String periodCode;
    private final String periodName;
    private final LocalDate periodStartDate;
    private final LocalDate periodEndDate;
    private final String periodStatus; // DRAFT, ACTIVE, CLOSED
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Promotion View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class PromotionViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final String currentDesignation;
    private final String promotedDesignation;
    private final String currentSalaryGrade;
    private final String promotedSalaryGrade;
    private final BigDecimal currentSalary;
    private final BigDecimal promotedSalary;
    private final LocalDate promotionDate;
    private final String promotionReason;
    private final String promotionStatus; // PROPOSED, APPROVED, EFFECTIVE, CANCELLED
    private final UUID approvedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}

/**
 * Increment View DTO
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class IncrementViewDto {
    private final UUID id;
    private final String tenantId;
    private final UUID employeeId;
    private final String employeeCode;
    private final String employeeName;
    private final LocalDate incrementDate;
    private final BigDecimal currentSalary;
    private final BigDecimal incrementAmount;
    private final BigDecimal incrementPercentage;
    private final BigDecimal newSalary;
    private final String incrementReason;
    private final String incrementStatus; // PROPOSED, APPROVED, EFFECTIVE, CANCELLED
    private final UUID approvedBy;
    private final String remarks;
    private final Instant createdAt;
    private final Instant updatedAt;
}
