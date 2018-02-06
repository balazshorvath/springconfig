package hu.springconfig.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

/**
 * In case of any error, that has no specific exception, use this.
 * Pass the message and HTTP status to the new object, and a response will be created.
 * See {@link hu.springconfig.config.error.GlobalExceptionHandler}.
 *
 * If you want to have {@link hu.springconfig.config.error.GlobalExceptionHandler} to load
 * the message from message source, you can set {@link #messageId}, otherwise set it to null.
 */
public class ResponseException extends RuntimeException {
    private HttpStatus status;
    private String messageId;
    private Date time;

    public ResponseException(String message, HttpStatus status, String messageId) {
        super(message);
        this.status = status;
        this.messageId = messageId;
        this.time = new Date();
    }

    public ResponseException(String message, Throwable cause, HttpStatus status, String messageId) {
        super(message, cause);
        this.status = status;
        this.messageId = messageId;
        this.time = new Date();
    }

    public ResponseException(Throwable cause, HttpStatus status, String messageId) {
        super(cause);
        this.status = status;
        this.messageId = messageId;
        this.time = new Date();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Date getTime() {
        return time;
    }

    public String getMessageId() {
        return messageId;
    }
}
