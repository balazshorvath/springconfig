package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

//TODO: extend this class with an entity and id, or value field
public class NotFoundException extends ResponseException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(Throwable cause) {
        super(cause, HttpStatus.NOT_FOUND);
    }
}
