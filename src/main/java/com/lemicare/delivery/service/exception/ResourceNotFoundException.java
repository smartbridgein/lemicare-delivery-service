package com.lemicare.delivery.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A custom runtime exception thrown when a requested resource is not found in the system.
 *
 * This exception is specifically used to represent a "404 Not Found" scenario in the
 * application's business logic.
 *
 * The @ResponseStatus(HttpStatus.NOT_FOUND) annotation is a powerful Spring feature.
 * If this exception is thrown and not caught by any other handler (like a @ControllerAdvice),
 * Spring will automatically translate it into an HTTP 404 Not Found response. This provides
 * a sensible default behavior.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * The message should clearly state which resource was not found and with what identifier.
     *
     * @param message The detail message. Example: "Delivery not found with id: 12345"
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param cause The root cause of the exception.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
