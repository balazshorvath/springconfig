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

    default TypeValidationError createTypeValidationError() {
        Class<T> type = getType();
        return new TypeValidationError(type, type.getSimpleName().toLowerCase() + ".validation.error", new ArrayList<>());
    }
}
