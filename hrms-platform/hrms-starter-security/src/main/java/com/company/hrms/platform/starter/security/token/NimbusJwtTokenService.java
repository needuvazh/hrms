package com.company.hrms.platform.starter.security.token;

import com.company.hrms.platform.starter.security.api.JwtTokenClaims;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.platform.starter.security.api.JwtTokenValue;
import com.company.hrms.platform.starter.security.config.SecurityJwtProperties;
import java.time.Instant;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import reactor.core.publisher.Mono;

public class NimbusJwtTokenService implements JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final SecurityJwtProperties securityJwtProperties;

    public NimbusJwtTokenService(JwtEncoder jwtEncoder, SecurityJwtProperties securityJwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.securityJwtProperties = securityJwtProperties;
    }

    @Override
    public Mono<JwtTokenValue> issueToken(JwtTokenClaims claims) {
        return Mono.fromSupplier(() -> {
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plusSeconds(securityJwtProperties.getExpirySeconds());

            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                    .issuer(securityJwtProperties.getIssuer())
                    .subject(claims.username())
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .claim("uid", claims.userId().toString())
                    .claim("tenant", claims.tenantId())
                    .claim("roles", claims.roles())
                    .claim("permissions", claims.permissions())
                    .build();

            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
            String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
            return new JwtTokenValue(token, issuedAt, expiresAt);
        });
    }
}
