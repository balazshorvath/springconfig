package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ResponseException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(Throwable cause) {
        super(cause, HttpStatus.FORBIDDEN);
    }
}
