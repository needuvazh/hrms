package com.company.hrms.platform.starter.security.config;

import com.company.hrms.platform.starter.security.api.AuthorizationRulesCustomizer;
import com.company.hrms.platform.starter.security.api.CurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.platform.starter.security.context.ReactiveCurrentUserContextAccessor;
import com.company.hrms.platform.starter.security.token.NimbusJwtTokenService;
import com.company.hrms.platform.starter.security.web.TenantAuthenticationAlignmentWebFilter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@AutoConfiguration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityJwtProperties.class)
public class SecurityStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SecurityWebFilterChain hrmsSecurityWebFilterChain(
            ServerHttpSecurity http,
            ObjectProvider<AuthorizationRulesCustomizer> authorizationRulesCustomizers,
            TenantAuthenticationAlignmentWebFilter tenantAuthenticationAlignmentWebFilter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .addFilterAfter(tenantAuthenticationAlignmentWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(spec -> {
                    spec.pathMatchers(
                                    "/actuator/health",
                                    "/actuator/info",
                                    "/api/v1/ping",
                                    "/api/v1/auth/token",
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/webjars/swagger-ui/**",
                                    "/v3/api-docs",
                                    "/v3/api-docs/**")
                            .permitAll();
                    authorizationRulesCustomizers.orderedStream().forEach(customizer -> customizer.customize(spec));
                    spec.anyExchange().authenticated();
                })
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    TenantAuthenticationAlignmentWebFilter tenantAuthenticationAlignmentWebFilter() {
        return new TenantAuthenticationAlignmentWebFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    SecretKey jwtSecretKey(SecurityJwtProperties properties) {
        return new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    @ConditionalOnMissingBean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    @ConditionalOnMissingBean
    ReactiveJwtDecoder reactiveJwtDecoder(SecretKey secretKey) {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    JwtTokenService jwtTokenService(JwtEncoder jwtEncoder, SecurityJwtProperties properties) {
        return new NimbusJwtTokenService(jwtEncoder, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    CurrentUserContextAccessor currentUserContextAccessor() {
        return new ReactiveCurrentUserContextAccessor();
    }

    @Bean
    @ConditionalOnMissingBean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, extractAuthorities(jwt), jwt.getSubject()));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority("PERM_" + permission)));
        }

        return authorities;
    }
}
