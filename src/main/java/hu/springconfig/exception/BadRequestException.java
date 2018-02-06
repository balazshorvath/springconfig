package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ResponseException {
    public BadRequestException(String message, String messageId) {
        super(message, HttpStatus.BAD_REQUEST, messageId);
    }

    public BadRequestException(String message, Throwable cause, String messageId) {
        super(message, cause, HttpStatus.BAD_REQUEST, messageId);
    }
    public BadRequestException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, null);
    }

    public BadRequestException(Throwable cause, String messageId) {
        super(cause, HttpStatus.BAD_REQUEST, messageId);
    }
}
