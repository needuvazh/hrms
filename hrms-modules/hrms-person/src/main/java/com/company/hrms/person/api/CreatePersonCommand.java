package com.company.hrms.person.api;

public record CreatePersonCommand(
        String personCode,
        String firstName,
        String lastName,
        String email,
        String mobile,
        String countryCode,
        String nationalityCode
) {
}
