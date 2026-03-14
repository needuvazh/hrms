package com.company.hrms.document.model;

import java.time.LocalDate;
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
public class DocumentExpiryQueryDto {
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final boolean includeArchived;
}
