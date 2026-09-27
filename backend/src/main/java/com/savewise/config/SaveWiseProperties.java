package com.savewise.config;

import java.time.Duration;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application settings bound from {@code savewise.*}.
 *
 * @param jwt            signing settings for the session token
 * @param cookie         settings for the session cookie that carries the token
 * @param login          brute-force protection for the login endpoint
 * @param allowedOrigins browser origins allowed to call the API directly (CORS). Empty when the
 *                       frontend proxies {@code /api} on its own origin, which is the default setup.
 */
@Validated
@ConfigurationProperties("savewise")
public record SaveWiseProperties(
        @NotNull Jwt jwt,
        @NotNull Cookie cookie,
        @NotNull Login login,
        List<String> allowedOrigins) {

    public SaveWiseProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    /**
     * @param secret HMAC-SHA256 key; at least 32 bytes
     * @param ttl    how long a session token stays valid
     * @param issuer value of the {@code iss} claim
     */
    public record Jwt(
            @NotBlank @Size(min = 32, message = "must be at least 32 characters") String secret,
            @NotNull Duration ttl,
            @NotBlank String issuer) {
    }

    /**
     * @param name   session cookie name
     * @param secure send the cookie over HTTPS only; disable for plain-HTTP local development
     */
    public record Cookie(@NotBlank String name, boolean secure) {
    }

    /**
     * @param maxFailures failed attempts allowed per email before login is blocked
     * @param lockout     how long the block lasts after the last failure
     */
    public record Login(int maxFailures, @NotNull Duration lockout) {
    }
}
