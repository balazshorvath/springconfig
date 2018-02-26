package hu.springconfig.config.error;

import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    private MessageProvider messageProvider;

    @ExceptionHandler(value = ResponseException.class)
    public ResponseEntity<Object> handleResponseException(ResponseException exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        APIError error = new APIError(exception);
        error.setMessage(messageProvider.getMessage(exception.getMessage()));

        logger.error("ResponseException handled: " + error, exception);
        return handleExceptionInternal(exception, error, headers, exception.getStatus(), request);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        APIError error = new APIError();

        error.setException(ForbiddenException.class);
        error.setStatus(HttpStatus.FORBIDDEN.value());
        error.setWhen(new Date());

        if (exception.getCause() != null) {
            error.setOriginalException(exception.getCause().getClass());
        } else {
            error.setOriginalException(exception.getClass());
        }
        error.setMessage(messageProvider.getMessage("forbidden.message"));

        logger.error("AccessDeniedException handled: " + error, exception);
        return handleExceptionInternal(exception, error, headers, HttpStatus.FORBIDDEN, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        APIError error = new APIError(new BadRequestException(ex.getMessage(), ex));
        error.setMessage(messageProvider.getMessage("http.bad.request"));
        return handleExceptionInternal(ex, error, headers, HttpStatus.BAD_REQUEST, request);
    }
}
