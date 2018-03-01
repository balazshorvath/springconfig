package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceCreateTest extends TestBase {
    @Autowired
    private IdentityService underTest;

    /* CREATE TESTS */

    @Test
    public void testCreateSuccess() {
        final Long userId = 100L;
        final String username = "user";
        final String email = "user@mail.com";
        final String password = "user";
        when(identityRepository.save(any(Identity.class))).then(invocation -> {
            Identity identity = invocation.getArgument(0);
            identity.setId(userId);
            identity.setVersion(0);
            return identity;
        });

        Identity created = underTest.createIdentity(username, email, password, password);
        assertIdentity(created, userId, username, email, password, Collections.singleton(userRole));
    }

    @Test
    public void testCreateUsernameInvalid() {
        final String username = "";
        final String email = "user@mail.com";
        final String password = "user";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("username", "identity.username.invalid")
        );
    }

    @Test
    public void testCreateUsernameInvalidTooLong() {
        final String username = "qweiquoijqowieqjdlwknclabcjawudbawéoidaugvwféaudhfsoehflsuebvlisuevhsef";
        final String email = "user@mail.com";
        final String password = "use~r";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("username", "identity.username.invalid")
        );
    }

    @Test
    public void testCreateUsernameInvalidTooShort() {
        final String username = "s1";
        final String email = "user@mail.com";
        final String password = "use~r";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("username", "identity.username.invalid")
        );
    }

    @Test
    public void testCreateUsernameInvalidSpecialCharacter() {
        final String username = "user'asd";
        final String email = "user@mail.com";
        final String password = "us---er";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("username", "identity.username.invalid")
        );
    }

    @Test
    public void testCreateEmailInvalid() {
        final String username = "user";
        final String email = "";
        final String password = "useruseruseruseruseruseruseruser";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("email", "identity.email.invalid")
        );
    }

    @Test
    public void testCreateEmailInvalidTooShort() {
        final String username = "s2351";
        final String email = "u@l";
        final String password = "u+%=se~r";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("email", "identity.email.invalid")
        );
    }

    @Test
    public void testCreateEmailInvalidTooLong() {
        final String username = "s2351";
        final String email = "uhhh345567gfd3..g.dfdf2hh53h5f7d4h6d7hhhdfhoqkw@asfuuahsha214kcacsl.vobm";
        final String password = "u+%=se~r";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("email", "identity.email.invalid")
        );
    }

    @Test
    public void testCreatePasswordInvalid() {
        final String username = "user";
        final String email = "ue.r@mail.com";
        final String password = "a";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("password", "identity.password.invalid")
        );
    }

    @Test
    public void testCreatePasswordInvalidTooShort() {
        final String username = "s2351";
        final String email = "u@l";
        final String password = "asd";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("password", "identity.password.invalid"),
                new FieldValidationError("email", "identity.email.invalid")
        );
    }

    @Test
    public void testCreatePasswordInvalidTooLong() {
        final String username = "1337h4x0r";
        final String email = "uhhh345567gfd3@srszhr.vobm";
        final String password = "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, password);
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("password", "identity.password.invalid")
        );
    }

    @Test
    public void testCreatePasswordConfirmInvalid() {
        final String username = "user";
        final String email = "user@mail.com";
        final String password = "use!\"r";

        ValidationException exception = null;
        try {
            underTest.createIdentity(username, email, password, "asdasd");
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                "identity.validation.error",
                Identity.class,
                new FieldValidationError("passwordConfirm", "identity.password.confirm.mismatch")
        );
    }

}
