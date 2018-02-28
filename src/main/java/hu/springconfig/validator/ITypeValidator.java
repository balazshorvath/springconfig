package hu.springconfig.validator;

import hu.springconfig.exception.BadRequestException;
import hu.springconfig.validator.error.TypeValidationError;

import java.util.ArrayList;

public interface ITypeValidator<T> {
    void validate(T entity) throws BadRequestException;

    Class<T> getType();

    default TypeValidationError createTypeValidationError() {
        Class<T> type = getType();
        return new TypeValidationError(type, type.getSimpleName().toLowerCase() + ".validation.error", new ArrayList<>());
    }
}
