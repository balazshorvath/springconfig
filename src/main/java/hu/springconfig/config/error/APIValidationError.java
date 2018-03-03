package hu.springconfig.config.error;

import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.error.TypeValidationError;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
public class APIValidationError extends APIError {
    TypeValidationError error;

    public APIValidationError(ValidationException e) {
        super(e);
        this.error = e.getError();
    }
}
