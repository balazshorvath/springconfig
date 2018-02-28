package hu.springconfig.exception;

import hu.springconfig.validator.error.TypeValidationError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ResponseException {
    @Getter
    private TypeValidationError error;

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(Throwable cause) {
        super(cause, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, TypeValidationError error) {
        super(message, HttpStatus.BAD_REQUEST);
        this.error = error;
    }
}
