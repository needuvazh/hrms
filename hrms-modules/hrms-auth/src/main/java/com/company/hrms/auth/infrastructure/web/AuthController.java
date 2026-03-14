package com.company.hrms.auth.infrastructure.web;

import com.company.hrms.auth.api.AuthModuleApi;
import com.company.hrms.auth.api.AuthTokenCommand;
import com.company.hrms.auth.api.AuthTokenView;
import com.company.hrms.auth.api.CurrentUserView;
import com.company.hrms.auth.api.RoleView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and current-user authorization APIs")
public class AuthController {

    private final AuthModuleApi authModuleApi;

    public AuthController(AuthModuleApi authModuleApi) {
        this.authModuleApi = authModuleApi;
    }

    @PostMapping("/token")
    @Operation(summary = "Issue access token", description = "Authenticates the user credentials and returns a token for subsequent secured API calls.")
    public Mono<AuthTokenView> issueToken(@Valid @RequestBody AuthTokenRequest request) {
        return authModuleApi.issueToken(new AuthTokenCommand(request.username(), request.password()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns details of the currently authenticated user resolved from the security context.")
    public Mono<CurrentUserView> currentUser() {
        return authModuleApi.currentUser();
    }

    @GetMapping("/me/roles")
    @Operation(summary = "Get current user roles", description = "Returns the role assignments for the currently authenticated user within the active tenant.")
    public Flux<RoleView> currentUserRoles() {
        return authModuleApi.getRolesForCurrentUser();
    }

    public record AuthTokenRequest(
            @Schema(description = "Unique user login name", example = "admin")
            @NotBlank String username,
            @Schema(description = "Raw account password", example = "P@ssw0rd")
            @NotBlank String password
    ) {
    }
}
