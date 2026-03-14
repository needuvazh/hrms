package com.company.hrms.employee.model;

import reactor.core.publisher.Mono;

public interface EmployeeOnboardingApi {

    Mono<EmployeeOnboardingViewDto> onboardEmployee(EmployeeOnboardingCommandDto command);
}
