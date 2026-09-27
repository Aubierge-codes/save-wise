package com.savewise.auth;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.stereotype.Service;

import com.savewise.config.SaveWiseProperties;

/**
 * Counts failed logins per email and blocks further attempts once the limit is reached.
 * The count expires {@code lockout} after the most recent failure.
 */
@Service
public class LoginAttemptService {

    private final int maxFailures;
    private final Cache<String, AtomicInteger> failures;

    public LoginAttemptService(SaveWiseProperties properties) {
        this.maxFailures = properties.login().maxFailures();
        this.failures = Caffeine.newBuilder()
                .expireAfterWrite(properties.login().lockout())
                .maximumSize(100_000)
                .build();
    }

    public boolean isBlocked(String email) {
        AtomicInteger count = failures.getIfPresent(email);
        return count != null && count.get() >= maxFailures;
    }

    public void recordFailure(String email) {
        AtomicInteger count = failures.get(email, key -> new AtomicInteger());
        count.incrementAndGet();
        // Re-put so expireAfterWrite restarts from this failure.
        failures.put(email, count);
    }

    public void recordSuccess(String email) {
        failures.invalidate(email);
    }
}
