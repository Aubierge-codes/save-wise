package com.savewise.auth;

/**
 * The authenticated principal, rebuilt from the session token on every request.
 */
public record AuthUser(long id, String email, String fullName) {
}
