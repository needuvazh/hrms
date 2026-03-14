package com.company.hrms.platform.featuretoggle.api;

public record ModuleCatalog(
        String moduleKey,
        String moduleName,
        boolean active
) {
}
