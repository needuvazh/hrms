package com.company.hrms.auth.api;

public record ProvisionUserAccountCommand(
        String username,
        String email,
        String rawPassword,
        String roleCode
) {
}
