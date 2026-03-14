package com.company.hrms.recruitment.model;

import com.company.hrms.recruitment.model.CandidateStatus;
import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CandidateDto {
    private final UUID id;
    private final String tenantId;
    private final UUID personId;
    private final String candidateCode;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String jobPostingCode;
    private final CandidateStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
