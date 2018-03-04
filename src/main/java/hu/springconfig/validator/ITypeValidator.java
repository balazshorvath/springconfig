package hu.springconfig.validator;

import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.error.TypeValidationError;

import java.util.ArrayList;

/**
 * Why should the Validation end up in 409 and not 400?
 * Check javadoc at: {@link ValidationException}
 */
public interface ITypeValidator<T> {
    void validate(T entity) throws ValidationException;

    Class<T> getType();

    String getValidationErrorMessage();

    default TypeValidationError createTypeValidationError() {
        return new TypeValidationError(
                getType(),
                getValidationErrorMessage(),
                new ArrayList<>()
        );
    }

    default void checkResult(TypeValidationError error) throws ValidationException {
        if (error.getErrors().size() > 0) {
            throw new ValidationException(getValidationErrorMessage(), error);
        }
    }
}
