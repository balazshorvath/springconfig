package hu.springconfig.service.account;

import hu.springconfig.Application;
import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.config.message.entity.InviteMessages;
import hu.springconfig.data.entity.Invite;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class InviteTest extends TestBase {
    @Autowired
    private InviteService underTest;
    @MockBean
    private MailingService mailingService;

    @Before
    @Override
    public void setup() {
        super.setup();
        when(inviteRepository.save(any(Invite.class)))
                .thenAnswer(invocation -> new Invite(invocation.getArgument(0)));
    }

    @Test
    public void testInvite() {
        // GIVEN
        final String email = "email";
        when(inviteRepository.existsByInviteKey(anyString())).thenReturn(false);
        // THEN
        Invite invite = underTest.create(email);

        assertEquals(email, invite.getEmail());
        assertNotNull(invite.getDate());
        assertNotNull(invite.getInviteKey());
        assertFalse(invite.isUsed());

        verify(mailingService, times(1)).sendInvite(eq(email), anyString());
    }

    @Test
    public void testInviteEmailInvalid() {
        // GIVEN
        final String email = "ema";
        // THEN
        ValidationException exception = null;
        try {
            underTest.create(email);
        } catch (ValidationException e) {
            exception = e;
        }

        assertNotNull(exception);
        assertValidationError(
                exception.getError(),
                InviteMessages.INVITE_VALIDATION_ERROR,
                Invite.class,
                new FieldValidationError("email", InviteMessages.INVITE_EMAIL_INVALID)
        );
    }
}
