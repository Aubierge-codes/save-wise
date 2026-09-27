package com.savewise.auth;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.savewise.auth.AuthDtos.LoginRequest;
import com.savewise.auth.AuthDtos.RegisterRequest;
import com.savewise.common.ApiException;
import com.savewise.user.User;
import com.savewise.user.UserRepository;

@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid email or password";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttempts;
    /** Compared against when the email is unknown, so both paths cost one hash. */
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, LoginAttemptService loginAttempts) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.loginAttempts = loginAttempts;
        this.dummyHash = passwordEncoder.encode("savewise-timing-equaliser");
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = normalise(request.email());
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        try {
            return users.saveAndFlush(new User(email, passwordEncoder.encode(request.password()), request.fullName().strip()));
        } catch (DataIntegrityViolationException e) {
            // Lost a race with a concurrent registration for the same email.
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = normalise(request.email());
        if (loginAttempts.isBlocked(email)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed attempts. Please try again later.");
        }
        User user = users.findByEmail(email).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(request.password(), hash);
        if (user == null || !matches) {
            loginAttempts.recordFailure(email);
            throw new ApiException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        loginAttempts.recordSuccess(email);
        return user;
    }

    private static String normalise(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
