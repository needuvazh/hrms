package com.company.hrms.document.model;

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
public class StorageReferenceDto {
    private final String provider;
    private final String bucket;
    private final String objectKey;
    private final String checksum;
    private final String contentType;
    private final long sizeBytes;
}
