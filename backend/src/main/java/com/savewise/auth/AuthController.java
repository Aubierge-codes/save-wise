package com.savewise.auth;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.savewise.auth.AuthDtos.CsrfResponse;
import com.savewise.auth.AuthDtos.LoginRequest;
import com.savewise.auth.AuthDtos.RegisterRequest;
import com.savewise.auth.AuthDtos.UserResponse;
import com.savewise.config.SaveWiseProperties;
import com.savewise.user.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final SaveWiseProperties.Cookie cookieSettings;

    public AuthController(AuthService authService, JwtService jwtService, SaveWiseProperties properties) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieSettings = properties.cookie();
    }

    /**
     * Hands the SPA a CSRF token. Loading the token here also makes Spring Security write the
     * {@code XSRF-TOKEN} cookie, which the frontend echoes back in the {@code X-XSRF-TOKEN} header.
     */
    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return withSession(HttpStatus.CREATED, user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        return withSession(HttpStatus.OK, user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionCookie("", 0).toString())
                .build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return UserResponse.from(user);
    }

    private ResponseEntity<UserResponse> withSession(HttpStatus status, User user) {
        String token = jwtService.issue(user);
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, sessionCookie(token, jwtService.ttlSeconds()).toString())
                .body(new UserResponse(user.getId(), user.getEmail(), user.getFullName()));
    }

    private ResponseCookie sessionCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(cookieSettings.name(), value)
                .httpOnly(true)
                .secure(cookieSettings.secure())
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
