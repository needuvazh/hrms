package com.company.hrms.employee.api;

import reactor.core.publisher.Mono;

public interface EmployeeOnboardingApi {

    Mono<EmployeeOnboardingView> onboardEmployee(EmployeeOnboardingCommand command);
}
