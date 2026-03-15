package com.company.hrms.organization.application;

import com.company.hrms.organization.model.OrganizationModels;

public final class OrganizationMapper {

    private OrganizationMapper() {
    }

    public static OrganizationModels.SearchQuery toSearchQuery(String q, Boolean active, int limit, int offset) {
        return new OrganizationModels.SearchQuery(q, active, limit, offset);
    }

    public static OrganizationModels.StatusUpdateCommand toStatusUpdate(boolean active) {
        return new OrganizationModels.StatusUpdateCommand(active);
    }
}
