package com.company.hrms.platform.sharedkernel.remote;

public record ModuleClientDescriptor(
        String moduleKey,
        ModuleClientMode mode,
        String baseUrl
) {

    public boolean isRemote() {
        return mode == ModuleClientMode.REMOTE;
    }
}
