package hu.springconfig.config.error;

import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.exception.ResponseException;
import hu.springconfig.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    private MessageProvider messageProvider;

    @ExceptionHandler(value = ResponseException.class)
    public ResponseEntity<Object> handleResponseException(ResponseException exception, WebRequest request){
        HttpHeaders headers = new HttpHeaders();
        APIError error = new APIError();

        error.setException(exception.getClass());
        error.setStatus(exception.getStatus());
        error.setWhen(exception.getTime());

        if(exception.getCause() != null){
            error.setOriginalException(exception.getCause().getClass());
        }
        if(Util.notNullAndNotEmpty(exception.getMessageId())){
            error.setMessage(messageProvider.getMessage(exception.getMessageId()));
        }else {
            error.setMessage(exception.getMessage());
        }

        return handleExceptionInternal(exception, error, headers, error.getStatus(), request);
    }
}
