package com.lemicare.delivery.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Custom exception for errors related to Shiprocket API authentication.
 * This is an unchecked exception (extends RuntimeException) as authentication
 * failures are typically critical and indicate a configuration issue
 * or an unavailable partner service, not something that can be recovered from
 * in the immediate call stack.
 */
public class ShiprocketAuthenticationException extends RuntimeException {

    private final HttpStatusCode httpStatus;

    public ShiprocketAuthenticationException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // Default for cases where status isn't known
    }

    public ShiprocketAuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ShiprocketAuthenticationException(String message, HttpStatusCode httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ShiprocketAuthenticationException(String message, HttpStatusCode httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatusCode getHttpStatus() {
        return httpStatus;
    }
}
