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
}
