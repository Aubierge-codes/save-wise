package com.savewise.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.savewise.config.SaveWiseProperties;
import com.savewise.user.User;

/**
 * Issues and verifies the HS256 session tokens carried in the session cookie.
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";

    private final SaveWiseProperties.Jwt settings;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtService(SaveWiseProperties properties) {
        this.settings = properties.jwt();
        SecretKey key = new SecretKeySpec(settings.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        nimbusDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(settings.issuer()));
        this.decoder = nimbusDecoder;
    }

    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(settings.issuer())
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(settings.ttl()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_NAME, user.getFullName())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Returns the user a token was issued to, or empty if the token is malformed, forged or expired.
     */
    public Optional<AuthUser> verify(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            return Optional.of(new AuthUser(
                    Long.parseLong(jwt.getSubject()),
                    jwt.getClaimAsString(CLAIM_EMAIL),
                    jwt.getClaimAsString(CLAIM_NAME)));
        } catch (JwtException | NumberFormatException e) {
            return Optional.empty();
        }
    }

    public long ttlSeconds() {
        return settings.ttl().toSeconds();
    }
}
