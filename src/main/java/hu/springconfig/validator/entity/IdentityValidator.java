package hu.springconfig.validator.entity;

import hu.springconfig.config.message.IdentityMessages;
import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.util.Util;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IdentityValidator implements ITypeValidator<Identity> {
    @Value("${password.length.min}")
    private Integer passwordMin;
    @Value("${password.length.max}")
    private Integer passwordMax;
    @Value("${password.charset}")
    private String passwordCharset;

    @Value("${username.length.min}")
    private Integer usernameMin;
    @Value("${username.length.max}")
    private Integer usernameMax;
    @Value("${username.charset}")
    private String usernameCharset;

    @Value("${email.length.min}")
    private Integer emailMin;
    @Value("${email.length.max}")
    private Integer emailMax;

    @Autowired
    private MessageProvider messageProvider;

    @Override
    public void validate(Identity entity) throws ValidationException {
        TypeValidationError typeValidationError = createTypeValidationError();
        FieldValidationError error = validateEmail(entity.getEmail());
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        error = validateUsername(entity.getUsername());
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        if (typeValidationError.getErrors().size() > 0) {
            throw new ValidationException(IdentityMessages.IDENTITY_VALIDATION_ERROR, typeValidationError);
        }
    }

    public void validateWithPasswords(Identity entity, String password, String passwordConfirm) throws
            ValidationException {
        TypeValidationError error = createTypeValidationError();
        error.addErrorIfNotNull(validateEmail(entity.getEmail()));
        error.addErrorIfNotNull(validateUsername(entity.getUsername()));
        error.addErrorIfNotNull(validatePasswordConfirm(password, passwordConfirm));
        checkResult(error);
    }

    private FieldValidationError validatePasswordConfirm(String password, String passwordConfirm) {
        if (!Util.validateString(password, passwordMin, passwordMax, passwordCharset)) {
            return new FieldValidationError("password", IdentityMessages.IDENTITY_PASSWORD_INVALID);
        }
        if (!password.equals(passwordConfirm)) {
            return new FieldValidationError("passwordConfirm", IdentityMessages.IDENTITY_PASSWORD_CONFIRM_MISMATCH);
        }
        return null;
    }

    private FieldValidationError validateUsername(String username) {
        if (!Util.validateString(username, usernameMin, usernameMax, usernameCharset)) {
            return new FieldValidationError("username", IdentityMessages.IDENTITY_USERNAME_INVALID);
        }
        return null;
    }

    private FieldValidationError validateEmail(String email) {
        if (!Util.validateString(email, emailMin, emailMax, null)) {
            return new FieldValidationError("email", IdentityMessages.IDENTITY_EMAIL_INVALID);
        }
        return null;
    }

    @Override
    public Class<Identity> getType() {
        return Identity.class;
    }
}
