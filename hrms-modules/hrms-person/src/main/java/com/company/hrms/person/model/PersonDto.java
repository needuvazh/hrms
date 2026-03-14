package com.company.hrms.person.model;

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
public class PersonDto {
    private final UUID id;
    private final String tenantId;
    private final String personCode;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String mobile;
    private final String countryCode;
    private final String nationalityCode;
    private final Instant createdAt;
    private final Instant updatedAt;
}
