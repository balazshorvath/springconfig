package hu.springconfig.validator.entity;

import hu.springconfig.config.message.MessageProvider;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.util.Util;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static hu.springconfig.util.Util.checkCharset;

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
    public void validate(Identity entity) throws BadRequestException {
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
            throw new BadRequestException("identity.validation.error", typeValidationError);
        }
    }

    public void validateWithPasswords(Identity entity, String password, String passwordConfirm) throws BadRequestException {
        TypeValidationError typeValidationError = createTypeValidationError();
        FieldValidationError error = validateEmail(entity.getEmail());
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        error = validateUsername(entity.getUsername());
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        error = validatePasswordConfirm(password, passwordConfirm);
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        if (typeValidationError.getErrors().size() > 0) {
            throw new BadRequestException("identity.validation.error", typeValidationError);
        }
    }

    public FieldValidationError validatePasswordConfirm(String password, String passwordConfirm) {
        if (!Util.notNullAndNotEmpty(password) || password.length() < passwordMin || password.length() > passwordMax
                || !checkCharset(password, passwordCharset)) {
            return new FieldValidationError("password", "identity.password.invalid");
        }
        if (!password.equals(passwordConfirm)) {
            return new FieldValidationError("passwordConfirm", "identity.password.confirm.mismatch");
        }
        return null;
    }

    public FieldValidationError validateUsername(String username) {
        if (!Util.notNullAndNotEmpty(username) || username.length() < usernameMin || username.length() > usernameMax
                || !checkCharset(username, usernameCharset)) {
            return new FieldValidationError("username", "identity.username.invalid");
        }
        return null;
    }

    public FieldValidationError validateEmail(String email) {
        if (!Util.notNullAndNotEmpty(email) || email.length() < emailMin || email.length() > emailMax) {
            return new FieldValidationError("email", "identity.email.invalid");
        }
        return null;
    }

    @Override
    public Class<Identity> getType() {
        return Identity.class;
    }
}
