package hu.springconfig.validator.request;

import hu.springconfig.config.message.application.ConditionMessages;
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
        if (condition == null) {
            return;
        }
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
                    new FieldValidationError("fieldName", ConditionMessages.CONDITION_FIELD_NAME_NULL)
            );
        }
        if (condition.getRelationalOperator() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("relationalOperator", ConditionMessages.CONDITION_RELATIONAL_OPERATOR_NULL)
            );
        }
        if (condition.getValue() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("value", ConditionMessages.CONDITION_VALUE_NULL)
            );
        }
        if (typeValidationError.getErrors().size() > 0) {
            throw new ValidationException(ConditionMessages.CONDITION_VALIDATION_ERROR, typeValidationError);
        }
    }

    private void validateConditionSet(ConditionSet condition) throws ValidationException {
        TypeValidationError typeValidationError = validateCondition(condition);
        if (condition.getLogicalOperator() == null) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("logicalOperator", ConditionMessages.CONDITION_LOGICAL_OPERATOR_NULL)
            );
        }
        if (!Util.notNullAndNotEmpty(condition.getConditions())) {
            typeValidationError.getErrors().add(
                    new FieldValidationError("conditions", ConditionMessages.CONDITION_CONDITIONS_NULL)
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
            throw new ValidationException(ConditionMessages.CONDITION_VALIDATION_ERROR, typeValidationError);
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
            return new FieldValidationError("order", ConditionMessages.CONDITION_ORDER_NULL);
        }
        return null;
    }

    @Override
    public Class<Condition> getType() {
        return Condition.class;
    }

    @Override
    public String getValidationErrorMessage() {
        return ConditionMessages.CONDITION_VALIDATION_ERROR;
    }
}
