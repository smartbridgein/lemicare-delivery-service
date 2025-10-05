package com.lemicare.delivery.service.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@ControllerAdvice

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // You can inject MessageSource if you have error messages in properties files
    // @Autowired
    // private MessageSource messageSource;

    /**
     * Handles AccessDeniedException, which is thrown by Spring Security or our
     * service layer when a user is not authorized to access a resource.
     * Returns a 403 Forbidden with a ProblemDetail body.
     *
     * @param ex      The caught AccessDeniedException.
     * @param request The current web request.
     * @return A ResponseEntity with an HTTP 403 Forbidden status and a ProblemDetail body.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.warn("Access Denied for request {}: {}", request.getDescription(false), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("Access Denied");
        problemDetail.setType(URI.create("https://lemicare.com/errors/access-denied")); // A custom URI for this error type
        problemDetail.setInstance(URI.create(request.getDescription(false).substring(request.getDescription(false).indexOf("uri=") + 4))); // Extract URI from request description
        problemDetail.setProperty("timestamp", Instant.now());
        // You could add more properties from the exception or security context if useful

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(problemDetail);
    }

    // You can add more @ExceptionHandler methods for other common exceptions like:
    // @ExceptionHandler(IllegalArgumentException.class)
    // public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    //     ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    //     problemDetail.setTitle("Invalid Request Parameter");
    //     problemDetail.setType(URI.create("https://lemicare.com/errors/invalid-parameter"));
    //     problemDetail.setInstance(URI.create(request.getDescription(false).substring(request.getDescription(false).indexOf("uri=") + 4)));
    //     problemDetail.setProperty("timestamp", Instant.now());
    //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    // }

    // ... handle other exceptions as needed
}