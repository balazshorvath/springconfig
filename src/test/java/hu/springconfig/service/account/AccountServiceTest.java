package hu.springconfig.service.account;

import hu.springconfig.Application;
import hu.springconfig.TestBase;
import hu.springconfig.config.message.entity.AccountMessages;
import hu.springconfig.data.entity.Account;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class AccountServiceTest extends TestBase {
    @Autowired
    private AccountService underTest;

    @Before
    @Override
    public void setup() {
        super.setup();
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> new Account(invocation.getArgument(0)));

    }

    @Test
    public void testCreate() {
        Identity identity = createIdentity(userRole);
        String firstName = "first";
        String lastName = "last";
        Long dailyGoal = 2000L;
        Account account = underTest.create(identity, firstName, lastName, dailyGoal);

        assertEquals(identity, account.getIdentity());
        assertEquals(firstName, account.getFirstName());
        assertEquals(lastName, account.getLastName());
        assertEquals(dailyGoal, account.getDailyCalorieGoal());
    }

    @Test
    public void testCreateCalorieNegative() {
        Identity identity = createIdentity(userRole);
        String firstName = "first";
        String lastName = "last";
        Long dailyGoal = -2000L;

        ValidationException exception = null;
        try {
            underTest.create(identity, firstName, lastName, dailyGoal);
        } catch (ValidationException ex) {
            exception = ex;
        }

        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                AccountMessages.ACCOUNT_VALIDATION_ERROR,
                Account.class,
                new FieldValidationError("dailyCalorieGoal", AccountMessages.ACCOUNT_DAILY_CALORIE_GOAL_INVALID)
        );
    }

}
