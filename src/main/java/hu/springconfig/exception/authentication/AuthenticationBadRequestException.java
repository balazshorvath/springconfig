package hu.springconfig.exception.authentication;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationBadRequestException extends AuthenticationException {
    public AuthenticationBadRequestException(String msg, Throwable t) {
        super(msg, t);
    }

    public AuthenticationBadRequestException(String msg) {
        super(msg);
    }

    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
