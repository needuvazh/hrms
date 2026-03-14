package com.company.hrms.auth.api;

public record AuthTokenCommand(
        String username,
        String password
) {
}
