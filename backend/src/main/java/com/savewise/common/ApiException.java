package com.savewise.common;

import org.springframework.http.HttpStatus;

/**
 * An error whose message is safe to show to the API client.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiException notFound(String what) {
        return new ApiException(HttpStatus.NOT_FOUND, what + " not found");
    }

    public HttpStatus getStatus() {
        return status;
    }
}
