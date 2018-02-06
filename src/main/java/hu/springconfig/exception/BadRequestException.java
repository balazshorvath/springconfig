package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ResponseException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(Throwable cause) {
        super(cause, HttpStatus.BAD_REQUEST);
    }
}
