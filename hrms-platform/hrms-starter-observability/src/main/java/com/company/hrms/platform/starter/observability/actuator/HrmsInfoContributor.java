package com.company.hrms.platform.starter.observability.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

public class HrmsInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("platform", "hrms");
        builder.withDetail("stack", "spring-webflux");
    }
}
