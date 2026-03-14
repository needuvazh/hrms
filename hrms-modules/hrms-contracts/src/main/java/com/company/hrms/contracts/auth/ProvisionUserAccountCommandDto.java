package com.company.hrms.contracts.auth;

public record ProvisionUserAccountCommandDto(
        String username,
        String email,
        String rawPassword,
        String roleCode
) {
}
