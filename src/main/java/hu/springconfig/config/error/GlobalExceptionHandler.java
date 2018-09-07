package hu.springconfig.config.error;

import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.config.message.application.HttpMessages;
import hu.springconfig.exception.*;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
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

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        APIValidationError error = new APIValidationError(exception);
        error.setMessage(messageProvider.getMessage(exception.getMessage()));
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
        error.setMessage(messageProvider.getMessage(HttpMessages.HTTP_FORBIDDEN_MESSAGE));

        logger.error("AccessDeniedException handled: " + error, exception);
        return handleExceptionInternal(exception, error, headers, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        APIError error = new APIError(new InternalErrorException(exception.getMessage(), exception));
        error.setMessage(messageProvider.getMessage(HttpMessages.HTTP_INTERNAL_ERROR));
        return handleExceptionInternal(exception, error, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException exception,
            WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        APIValidationError error = new APIValidationError(new ValidationException(exception.getMessage(), exception));
        error.setMessage(messageProvider.getMessage(HttpMessages.HTTP_CONFLICT_ERROR));
        error.setError(new TypeValidationError());
        return handleExceptionInternal(exception, error, headers, HttpStatus.CONFLICT, request);
    }


    @ExceptionHandler(value = PropertyReferenceException.class)
    public ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException exception, WebRequest
            request) {
        HttpHeaders headers = new HttpHeaders();
        return handleBadRequests(exception, headers, request, HttpMessages.SPECIFICATIONS_PROPERTY_NOT_FOUND);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(PropertyReferenceException exception, WebRequest
            request) {
        HttpHeaders headers = new HttpHeaders();
        return handleBadRequests(exception, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
            HttpHeaders headers, HttpStatus status, WebRequest
            request) {
        return handleBadRequests(exception, headers, request);
    }

    private ResponseEntity<Object> handleBadRequests(Exception exception, HttpHeaders headers, WebRequest request,
            String message) {
        APIError error = new APIError(new BadRequestException(exception.getMessage(), exception));
        error.setMessage(messageProvider.getMessage(message));
        return handleExceptionInternal(exception, error, headers, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> handleBadRequests(Exception exception, HttpHeaders headers, WebRequest request) {
        return handleBadRequests(exception, headers, request, HttpMessages.HTTP_BAD_REQUEST);
    }
}
