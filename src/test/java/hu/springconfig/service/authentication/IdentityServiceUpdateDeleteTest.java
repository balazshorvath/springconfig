package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.service.mail.MailingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        user = createIdentity(20L, "user", "user@email", "password", userRole);
        admin = createIdentity(21L, "admin", "admin@email", "password", userRole, adminRole);
        mockIdentityDatabase(user, admin);

        when(identityRepository.save(any(Identity.class))).thenAnswer(invocation -> new Identity(invocation.getArgument(0)));
    }

    @Test
    public void testResetPassword(){
        String oldPassword = user.getPassword();
        underTest.resetPassword(user);
        assertNotNull(user.getTokenExpiration());
        assertNotEquals(oldPassword, user.getPassword());
        verify(mailingService, times(1)).sendMail(user.getEmail(), "mail.password_reset.subject", "mail.password_reset.text");
    }
    @Test
    public void testChangePassword(){
        fail("Not implemented");
    }
    @Test
    public void testChangePasswordInvalid(){
        fail("Not implemented");
    }
    @Test
    public void testChangePasswordForbidden(){
        fail("Not implemented");
    }
    @Test
    public void testChangeUsernameSelf(){
        fail("Not implemented");
    }
    @Test
    public void testChangeUsernameSelfInvalid(){
        fail("Not implemented");
    }
    @Test
    public void testChangeUsernameSelfForbidden(){
        fail("Not implemented");
    }
    @Test
    public void testChangeEmailSelf(){
        fail("Not implemented");
    }
    @Test
    public void testChangeEmailSelfInvalid(){
        fail("Not implemented");
    }
    @Test
    public void testChangeEmailSelfForbidden(){
        fail("Not implemented");
    }

    @Test
    public void testUpdate(){
        Identity updated = underTest.updateIdentity(admin, user.getId(), "somethingelse", "");
        assertIdentity(updated, 20L, "somethingelse", user.getEmail(), "password", user.getRoles());
        updated = underTest.updateIdentity(admin, user.getId(), "somethingelse1", "newmail@mail");
        assertIdentity(updated, user.getId(), "somethingelse1", "newmail@mail", "password", user.getRoles());
    }

    @Test
    public void testUpdateLowRank() {
        AccessDeniedException exception = null;
        try{
            underTest.updateIdentity(user, admin.getId(), "somethingelse", "");
        }catch (AccessDeniedException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.low_rank", exception.getMessage());
    }
    @Test
    public void testUpdateInvalidUsername() {
        BadRequestException exception = null;
        try{
            underTest.updateIdentity(admin, user.getId(), "qwe", "");
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.username.invalid", exception.getMessage());
    }
    @Test
    public void testUpdateInvalidEmail() {
        BadRequestException exception = null;
        try{
            underTest.updateIdentity(admin, user.getId(), "", "asd");
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.email.invalid", exception.getMessage());
    }
    @Test
    public void testUpdateEntityDoesNotExist() {
        NotFoundException exception = null;
        try{
            underTest.updateIdentity(user, 10L, "somethingelse", "");
        }catch (NotFoundException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.not_found", exception.getMessage());
    }
}
