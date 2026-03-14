package com.company.hrms.wps.domain;

public record WpsExportPayload(
        String fileName,
        String contentType,
        String payload,
        String contentHash
) {
}
