package hu.springconfig.service.authentication;

import hu.springconfig.Application;
import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceCreateTest extends TestBase {
    @MockBean
    private IIdentityRepository identityRepository;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private BCryptPasswordEncoder encoder;

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

        Identity created = identityService.createIdentity(username, email, password, password);
        assertIdentity(created, userId, username, email, password, Collections.singleton(userRole));
    }

    @Test
    public void testCreateUsernameInvalid() {
        final String username = "";
        final String email = "user@mail.com";
        final String password = "user";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.username.invalid", exception.getMessage());
    }
    @Test
    public void testCreateUsernameInvalidTooLong() {
        final String username = "qweiquoijqowieqjdlwknclabcjawudbawéoidaugvwféaudhfsoehflsuebvlisuevhsef";
        final String email = "user@mail.com";
        final String password = "use~r";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.username.invalid", exception.getMessage());
    }
    @Test
    public void testCreateUsernameInvalidTooShort() {
        final String username = "s1";
        final String email = "user@mail.com";
        final String password = "use~r";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.username.invalid", exception.getMessage());
    }
    @Test
    public void testCreateUsernameInvalidSpecialCharacter() {
        final String username = "user'asd";
        final String email = "user@mail.com";
        final String password = "us---er";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.username.invalid", exception.getMessage());
    }
    @Test
    public void testCreateEmailInvalid() {
        final String username = "user";
        final String email = "";
        final String password = "useruseruseruseruseruseruseruser";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.email.invalid", exception.getMessage());
    }
    @Test
    public void testCreateEmailInvalidTooShort() {
        final String username = "s2351";
        final String email = "u@l";
        final String password = "u+%=se~r";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.email.invalid", exception.getMessage());
    }
    @Test
    public void testCreateEmailInvalidTooLong() {
        final String username = "s2351";
        final String email = "uhhh345567gfd3..g.dfdf2hh53h5f7d4h6d7hhhdfhoqkw@asfuuahsha214kcacsl.vobm";
        final String password = "u+%=se~r";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.email.invalid", exception.getMessage());
    }
    @Test
    public void testCreatePasswordInvalid() {
        final String username = "user";
        final String email = "ue.r@mail.com";
        final String password = "a";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.password.invalid", exception.getMessage());
    }
    @Test
    public void testCreatePasswordInvalidTooShort() {
        final String username = "s2351";
        final String email = "u@l";
        final String password = "asd";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.password.invalid", exception.getMessage());
    }
    @Test
    public void testCreatePasswordInvalidTooLong() {
        final String username = "1337h4x0r";
        final String email = "uhhh345567gfd3@srszhr.vobm";
        final String password = "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, password);
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.password.invalid", exception.getMessage());
    }
    @Test
    public void testCreatePasswordConfirmInvalid() {
        final String username = "user";
        final String email = "user@mail.com";
        final String password = "use!\"r";

        BadRequestException exception = null;
        try {
            identityService.createIdentity(username, email, password, "asdasd");
        }catch (BadRequestException e){
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.password.confirm.mismatch", exception.getMessage());
    }

    private void assertIdentity(Identity identity, Long id, String username, String email, String password, Set<Role> roles) {
        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(email, identity.getEmail());
        assertEquals(roles, identity.getRoles());
        assertTrue(encoder.matches(password, identity.getPassword()));
    }
}
