package hu.springconfig.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidatorProvider {
    private Map<Class, ITypeValidator> validators;

    @SuppressWarnings("unchecked")
    public <T> ITypeValidator<T> getTypeValidator(Class<T> type) {
        return validators.get(type);
    }

    @Autowired
    private void setValidators(List<ITypeValidator> validators) {
        this.validators = new HashMap<>(validators.size());
        validators.forEach(iTypeValidator -> this.validators.put(iTypeValidator.getType(), iTypeValidator));
    }
}
