package hu.springconfig.exception;

import hu.springconfig.validator.error.TypeValidationError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Why 409 and not 400?
 * <p>
 * RFC7231:
 * The 409 (Conflict) status code indicates that the request could not
 * be completed due to a conflict with the current state of the target
 * resource.  This code is used in situations where the user might be
 * able to resolve the conflict and resubmit the request.  The server
 * SHOULD generate a payload that includes enough information for a user
 * to recognize the source of the conflict.
 */
public class ValidationException extends ResponseException {
    @Getter
    private TypeValidationError error;

    public ValidationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT);
    }

    public ValidationException(Throwable cause) {
        super(cause, HttpStatus.CONFLICT);
    }

    public ValidationException(String message, TypeValidationError error) {
        super(message, HttpStatus.CONFLICT);
        this.error = error;
    }
}
