package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * In case of any error, that has no specific exception, use this.
 * Pass the message and HTTP status to the new object, and a response will be created.
 * See {@link hu.springconfig.config.error.GlobalExceptionHandler}.
 * <p>
 * {@link hu.springconfig.config.error.GlobalExceptionHandler} will try to load the message
 * field from the message source.
 */
public class ResponseException extends RuntimeException {
    private HttpStatus status;
    private Date time = new Date();

    public ResponseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ResponseException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public ResponseException(Throwable cause, HttpStatus status) {
        super(cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Date getTime() {
        return time;
    }

}
