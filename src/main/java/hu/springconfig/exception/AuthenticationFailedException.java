package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends ResponseException {
    public AuthenticationFailedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationFailedException(Throwable cause) {
        super(cause, HttpStatus.UNAUTHORIZED);
    }
}
