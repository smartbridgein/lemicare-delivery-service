package com.lemicare.delivery.service.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * A custom runtime exception used to represent any error that occurs while
 * communicating with an external delivery partner's API.
 * <p>
 * This exception carries important contextual information like the partner's name,
 * the HTTP status code received, and any specific error code from the partner's
 * response body, which is crucial for logging and debugging.
 */
@Getter
public class PartnerApiException extends RuntimeException {

    private static final long serialVersionUID = 781555452899725514L;

    /**
     * The name of the partner that the error occurred with (e.g., "SHIPROCKET").
     */
    private final String partnerName;

    /**
     * The HTTP status code received from the partner's API.
     */
    private final HttpStatus httpStatusCode;

    /**
     * A specific error code or reason provided by the partner in their response body. Can be null.
     */
    private final String partnerErrorCode;


    /**
     * The most detailed constructor.
     *
     * @param message A clear, internal-facing message describing what went wrong.
     * @param partnerName The name of the partner.
     * @param httpStatusCode The HTTP status received from the partner.
     * @param partnerErrorCode A specific error code from the partner's response.
     * @param cause The original root cause exception (e.g., an IOException).
     */
    public PartnerApiException(String message, String partnerName, HttpStatus httpStatusCode, String partnerErrorCode, Throwable cause) {
        super(message, cause);
        this.partnerName = partnerName;
        this.httpStatusCode = httpStatusCode;
        this.partnerErrorCode = partnerErrorCode;
    }

    /**
     * A convenience constructor for when there is no specific partner error code.
     */
    public PartnerApiException(String message, String partnerName, HttpStatus httpStatusCode, Throwable cause) {
        this(message, partnerName, httpStatusCode, null, cause);
    }

    /**
     * A convenience constructor for when the failure is not caused by another exception.
     */
    public PartnerApiException(String message, String partnerName, HttpStatus httpStatusCode) {
        this(message, partnerName, httpStatusCode, null, null);
    }


}
