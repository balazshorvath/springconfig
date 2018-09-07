package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.config.message.entity.IdentityMessages;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceUpdateDeleteTest extends TestBase {
    @Autowired
    private IdentityService underTest;
    @MockBean
    private MailingService mailingService;

    private Identity user;
    private Identity admin;

    @Override
    public void setup() {
        super.setup();
        user = createIdentity(20L, "user@email", "password", userRole);
        admin = createIdentity(21L, "admin@email", "password", userRole, adminRole);
        mockIdentityDatabase(user, admin);

        when(identityRepository.save(any(Identity.class))).thenAnswer(invocation -> new Identity(invocation.getArgument(
                0)));
    }

    @Test
    public void testDelete() {
        underTest.delete(admin, user.getId());
        verify(identityRepository, times(1)).delete(user.getId());
    }

    @Test
    public void testDeleteAccessDenied() {
        ForbiddenException exception = null;
        try {
            underTest.delete(user, admin.getId());
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_LOW_RANK, exception.getMessage());
    }

    @Test
    public void testDeleteNotFound() {
        NotFoundException exception = null;
        try {
            underTest.delete(admin, 10L);
        } catch (NotFoundException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testResetPassword() {
        String oldPassword = user.getPassword();
        Identity updated = underTest.resetPassword(user.getEmail());
        assertNotNull(updated.getTokenExpiration());
        assertNotEquals(oldPassword, updated.getPassword());
        verify(mailingService, times(1)).sendPasswordReset(eq(updated.getEmail()), anyString());
    }

    @Test
    public void testChangePassword() {
        Identity updated = underTest.changePassword(user, "password", "newpassword", "newpassword");
        assertIdentity(updated, 20L, "user@email", "newpassword", Collections.singleton(userRole));
    }

    @Test
    public void testChangePasswordInvalid() {
        ValidationException exception = null;
        try {
            underTest.changePassword(user, "password", "ne", "ne");
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("password", IdentityMessages.IDENTITY_PASSWORD_INVALID)
        );
    }

    @Test
    public void testChangePasswordConfirmInvalid() {
        ValidationException exception = null;
        try {
            underTest.changePassword(user, "password", "newp", "newpassword");
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("passwordConfirm", IdentityMessages.IDENTITY_PASSWORD_CONFIRM_MISMATCH)
        );
    }

    @Test
    public void testChangePasswordForbidden() {
        ForbiddenException exception = null;
        try {
            underTest.changePassword(user, "pord", "newpassword", "newpassword");
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, exception.getMessage());
    }

//    @Test
//    public void testChangeUsernameSelf() {
//        Identity updated = underTest.changeUsernameSelf(user, "password", "username");
//        assertIdentity(updated, 20L, "user@email", "password", Collections.singleton(userRole));
//    }

//    @Test
//    public void testChangeUsernameSelfInvalid() {
//        ValidationException exception = null;
//        try {
//            underTest.changeUsernameSelf(user, "password", "use+rname");
//        } catch (ValidationException e) {
//            exception = e;
//        }
//        assertNotNull(exception);
//        assertValidationError(
//                exception.getError(),
//                IdentityMessages.IDENTITY_VALIDATION_ERROR,
//                Identity.class,
//                new FieldValidationError("username", IdentityMessages.IDENTITY_USERNAME_INVALID)
//        );
//    }

//    @Test
//    public void testChangeUsernameSelfForbidden() {
//        ForbiddenException exception = null;
//        try {
//            underTest.changeUsernameSelf(user, "password1", "username");
//        } catch (ForbiddenException e) {
//            exception = e;
//        }
//        assertNotNull(exception);
//        assertEquals(IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, exception.getMessage());
//    }

    @Test
    public void testChangeEmailSelf() {
        Identity updated = underTest.changeEmailSelf(user, "password", "username@email");
        assertIdentity(updated, 20L, "username@email", "password", Collections.singleton(userRole));
    }

    @Test
    public void testChangeEmailSelfInvalid() {
        ValidationException exception = null;
        try {
            underTest.changeEmailSelf(user, "password", "us");
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("email", IdentityMessages.IDENTITY_EMAIL_INVALID)
        );
    }

    @Test
    public void testChangeEmailSelfForbidden() {
        ForbiddenException exception = null;
        try {
            underTest.changeEmailSelf(user, "password1", "username@email");
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, exception.getMessage());
    }

    @Test
    public void testUpdate() {
        Identity updated = underTest.updateIdentity(
                admin,
                user.getId(),
                user.getEmail(),
                user.getVersion()
        );
        assertIdentity(updated, 20L, user.getEmail(), "password", user.getRoles());
        updated = underTest.updateIdentity(admin, user.getId(), "newmail@mail", user.getVersion());
        assertIdentity(updated, user.getId(), "newmail@mail", "password", user.getRoles());
    }

    @Test
    public void testUpdateLowRank() {
        ForbiddenException exception = null;
        try {
            underTest.updateIdentity(user, admin.getId(), "", user.getVersion());
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_LOW_RANK, exception.getMessage());
    }

    @Test
    public void testUpdateInvalidUsernameAndEmail() {
        ValidationException exception = null;
        try {
            underTest.updateIdentity(admin, user.getId(), "", user.getVersion());
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("email", IdentityMessages.IDENTITY_EMAIL_INVALID)
        );
    }

    @Test
    public void testUpdateInvalidEmail() {
        ValidationException exception = null;
        try {
            underTest.updateIdentity(admin, user.getId(), "asd", user.getVersion());
        } catch (ValidationException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("email", IdentityMessages.IDENTITY_EMAIL_INVALID)
        );
    }

    @Test
    public void testUpdateEntityDoesNotExist() {
        NotFoundException exception = null;
        try {
            underTest.updateIdentity(user, 10L, "", user.getVersion());
        } catch (NotFoundException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(IdentityMessages.IDENTITY_NOT_FOUND, exception.getMessage());
    }
}
