package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ResponseException {
    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED);
    }

    public InvalidTokenException(Throwable cause) {
        super(cause, HttpStatus.UNAUTHORIZED);
    }
}
