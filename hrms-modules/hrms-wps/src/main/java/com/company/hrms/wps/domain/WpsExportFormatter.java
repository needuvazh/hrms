package com.company.hrms.wps.domain;

import java.util.List;

public interface WpsExportFormatter {

    String type();

    WpsExportPayload format(WpsBatch batch, List<WpsEmployeeEntry> entries);
}
