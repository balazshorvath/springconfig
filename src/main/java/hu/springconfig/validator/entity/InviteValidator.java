package hu.springconfig.validator.entity;

import hu.springconfig.config.message.entity.InviteMessages;
import hu.springconfig.data.entity.Invite;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.util.Util;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InviteValidator implements ITypeValidator<Invite> {
    @Value("${email.length.min}")
    private Integer emailMin;
    @Value("${email.length.max}")
    private Integer emailMax;

    @Override
    public void validate(Invite entity) throws ValidationException {
        TypeValidationError error = createTypeValidationError();
        error.addErrorIfNotNull(validateEmail(entity.getEmail()));
        checkResult(error);
    }


    private FieldValidationError validateEmail(String email) {
        if (!Util.validateString(email, emailMin, emailMax, null)) {
            return new FieldValidationError("email", InviteMessages.INVITE_EMAIL_INVALID);
        }
        return null;
    }

    @Override
    public Class<Invite> getType() {
        return Invite.class;
    }

    @Override
    public String getValidationErrorMessage() {
        return InviteMessages.INVITE_VALIDATION_ERROR;
    }
}
