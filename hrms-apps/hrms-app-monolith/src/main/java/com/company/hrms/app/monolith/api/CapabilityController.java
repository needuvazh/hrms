package com.company.hrms.app.monolith.api;

import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/capabilities")
public class CapabilityController {

    private final FeatureToggleService featureToggleService;

    public CapabilityController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/modules/{moduleKey}")
    public Mono<ModuleCapabilityView> moduleCapability(@PathVariable("moduleKey") String moduleKey) {
        return featureToggleService.currentTenantHasModule(moduleKey)
                .map(enabled -> new ModuleCapabilityView(moduleKey, enabled));
    }

    public record ModuleCapabilityView(
            String moduleKey,
            boolean enabled
    ) {
    }
}
