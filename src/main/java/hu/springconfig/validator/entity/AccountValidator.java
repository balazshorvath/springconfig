package hu.springconfig.validator.entity;

import hu.springconfig.config.message.entity.AccountMessages;
import hu.springconfig.data.entity.Account;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.ITypeValidator;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.springframework.stereotype.Component;

@Component
public class AccountValidator implements ITypeValidator<Account> {

    @Override
    public void validate(Account entity) throws ValidationException {
        TypeValidationError error = createTypeValidationError();
        error.addErrorIfNotNull(validateDailyCalories(entity.getDailyCalorieGoal()));
        checkResult(error);
    }

    private FieldValidationError validateDailyCalories(Long calories) {
        if (calories == null || calories < 0) {
            return new FieldValidationError("dailyCalorieGoal", AccountMessages.ACCOUNT_DAILY_CALORIE_GOAL_INVALID);
        }
        return null;
    }

    @Override
    public Class<Account> getType() {
        return Account.class;
    }

    @Override
    public String getValidationErrorMessage() {
        return AccountMessages.ACCOUNT_VALIDATION_ERROR;
    }
}
