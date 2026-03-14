package com.company.hrms.wps.model;

import java.util.List;

public interface WpsExportFormatter {

    String type();

    WpsExportPayloadDto format(WpsBatchDto batch, List<WpsEmployeeEntryDto> entries);
}
