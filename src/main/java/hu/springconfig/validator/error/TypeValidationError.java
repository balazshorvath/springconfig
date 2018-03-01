package hu.springconfig.validator.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TypeValidationError {
    private Class<?> type;
    private String message;
    private List<FieldValidationError> errors;

    /**
     * Add {@link FieldValidationError} to {@link #errors}.
     * Performs a null check.
     *
     * @param error error to add.
     */
    public void addErrorIfNotNull(FieldValidationError error) {
        if (error != null) {
            errors.add(error);
        }
    }
}
