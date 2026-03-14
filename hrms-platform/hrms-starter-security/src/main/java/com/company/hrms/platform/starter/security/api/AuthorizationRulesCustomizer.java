package com.company.hrms.platform.starter.security.api;

import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface AuthorizationRulesCustomizer {

    void customize(ServerHttpSecurity.AuthorizeExchangeSpec exchanges);
}
