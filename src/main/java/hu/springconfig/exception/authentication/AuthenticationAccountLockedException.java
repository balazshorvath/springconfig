package hu.springconfig.exception.authentication;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationAccountLockedException extends AuthenticationException {
    public AuthenticationAccountLockedException(String msg, Throwable t) {
        super(msg, t);
    }

    public AuthenticationAccountLockedException(String msg) {
        super(msg);
    }

    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

}
