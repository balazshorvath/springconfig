package hu.springconfig.config.error;

import hu.springconfig.exception.BadRequestException;
import hu.springconfig.validator.error.TypeValidationError;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIValidationError extends APIError {
    TypeValidationError error;

    public APIValidationError(BadRequestException e) {
        super(e);
        this.error = e.getError();
    }
}
