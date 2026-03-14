package com.company.hrms.auth.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthModuleApi {

    Mono<AuthTokenView> issueToken(AuthTokenCommand command);

    Mono<CurrentUserView> currentUser();

    Flux<RoleView> getRolesForCurrentUser();

    Mono<ProvisionedUserAccountView> provisionUserAccount(ProvisionUserAccountCommand command);
}
