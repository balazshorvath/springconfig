package hu.springconfig.validator.request;

import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.query.model.ConditionSet;
import hu.springconfig.data.query.model.FieldCondition;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.util.Util;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.stereotype.Component;

@Component
public class ConditionValidator implements ITypeValidator<Condition> {

    @Override
    public void validate(Condition condition) throws ValidationException {
        if (condition instanceof ConditionSet) {
            validateConditionSet((ConditionSet) condition);
        } else {
            validateFieldCondition((FieldCondition) condition);
        }
    }

    private void validateFieldCondition(FieldCondition condition) throws ValidationException {
        TypeValidationError typeValidationError = validateCondition(condition);
        if (!Util.notNullAndNotEmpty(condition.getFieldName())) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("fieldName", "condition.fieldName.null")
            );
        }
        if (condition.getRelationalOperator() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("relationalOperator", "condition.relationalOperator.null")
            );
        }
        if (condition.getValue() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("value", "condition.value.null")
            );
        }
        if (typeValidationError.getErrors().size() > 0) {
            throw new ValidationException("condition.validation.error", typeValidationError);
        }
    }

    private void validateConditionSet(ConditionSet condition) throws ValidationException {
        TypeValidationError typeValidationError = validateCondition(condition);
        if (condition.getLogicalOperator() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("logicalOperator", "condition.logicalOperator.null")
            );
        }
        if (!Util.notNullAndNotEmpty(condition.getConditions())) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("conditions", "condition.conditions.null")
            );
        }

        for (Condition condition1 : condition.getConditions()) {
            try {
                validate(condition1);
            } catch (ValidationException exception) {
                typeValidationError.getErrors().addAll(exception.getError().getErrors());
            }
            // STOP! JUST STOP!!?44!!
            if (typeValidationError.getErrors().size() > 10) {
                break;
            }
        }

        if (typeValidationError.getErrors().size() > 0) {
            throw new ValidationException("condition.validation.error", typeValidationError);
        }
    }

    private TypeValidationError validateCondition(Condition condition) {
        TypeValidationError typeValidationError = createTypeValidationError();
        FieldValidationError error = validateOrder(condition.getOrder());
        if (error != null) {
            typeValidationError.getErrors().add(error);
        }
        return typeValidationError;
    }

    private FieldValidationError validateOrder(Integer order) {
        if (order == null) {
            return new FieldValidationError("order", "condition.order.null");
        }
        return null;
    }

    @Override
    public Class<Condition> getType() {
        return Condition.class;
    }
}
