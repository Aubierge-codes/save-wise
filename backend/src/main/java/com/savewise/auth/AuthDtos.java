package com.savewise.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 128, message = "must be between 8 and 128 characters") String password) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record UserResponse(long id, String email, String fullName) {

        static UserResponse from(AuthUser user) {
            return new UserResponse(user.id(), user.email(), user.fullName());
        }
    }

    public record CsrfResponse(String headerName, String token) {
    }
}
