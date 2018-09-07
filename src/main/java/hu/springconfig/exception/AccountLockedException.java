package hu.springconfig.exception;

import hu.springconfig.config.message.entity.IdentityMessages;
import org.springframework.http.HttpStatus;

public class AccountLockedException extends ResponseException {

    public AccountLockedException() {
        super(IdentityMessages.IDENTITY_LOCKED, HttpStatus.FORBIDDEN);
    }

    public AccountLockedException(Throwable cause) {
        super(IdentityMessages.IDENTITY_LOCKED, cause, HttpStatus.FORBIDDEN);
    }

}
