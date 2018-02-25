package hu.springconfig.authentication;


import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.SecuredUpdate;
import hu.springconfig.data.dto.authentication.identity.ChangePassword;
import hu.springconfig.data.dto.authentication.identity.IdentityCreate;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.ForbiddenException;
import hu.springconfig.exception.InvalidTokenException;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.mail.MailingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class IdentityIntegrationTest extends IntegrationTestBase {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private PasswordEncoder encoder;
    @MockBean
    private MailingService mailingService;

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testIdentityCRUD() {
        final Set<Integer> roles = Collections.singleton(RoleService.USER_ROLE_ID);
        final String username = "myname";
        final String email = "myaddress@soemthing.com";
        final String password = "goodpw";
        final String newPassword = "newpass12";

        IdentityCreate create = new IdentityCreate();
        Credentials credentials = new Credentials();

        create.setEmail(email);
        create.setPassword(password);
        create.setPasswordConfirm(password);
        create.setUsername(username);

        credentials.setUsername(username);
        credentials.setPassword(password);

        // Register and authenticate
        restTemplate.postForObject("/auth/register", create, OKResponse.class);
        JWTTokenParser.TokenResponse response = restTemplate.postForObject("/auth", credentials, JWTTokenParser.TokenResponse.class);

        IdentityDTO identityToken = response.getIdentity();
        assertIdentity(identityToken, identityToken.getId(), username, email, roles);
        assertNotNull(response.getToken());

        setupRestTemplateToken(response);
        // Get self                 GET     /auth/{id}
        IdentityDTO identity = restTemplate.getForObject("/auth/" + identityToken.getId(), IdentityDTO.class);

        assertIdentity(identity, identityToken.getId(), username, email, roles);

        // Try get other, fail      GET     /auth/{id}
        // NOTE: Doesn't matter if the id doesn't exist. Authorization happens before that
        ResponseEntity<APIError> error = restTemplate.getForEntity("/auth/100", APIError.class);

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals((Integer) HttpStatus.FORBIDDEN.value(), error.getBody().getStatus());
        assertEquals("forbidden.message", error.getBody().getMessage());
        // Change password          POST    /auth/changePassword

        // Case 1: old password is wrong
        ChangePassword changePassword = new ChangePassword();
        changePassword.setNewPassword("newpass");
        changePassword.setNewPasswordConfirm(newPassword);
        changePassword.setOldPassword("wrongpw");
        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals((Integer) HttpStatus.FORBIDDEN.value(), error.getBody().getStatus());
        assertEquals(ForbiddenException.class, error.getBody().getException());
        assertEquals("identity.check_password_failed", error.getBody().getMessage());
        // Case 2: passwords don't match
        changePassword.setOldPassword(password);
        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals((Integer) HttpStatus.BAD_REQUEST.value(), error.getBody().getStatus());
        assertEquals(BadRequestException.class, error.getBody().getException());
        assertEquals("identity.password.confirm.mismatch", error.getBody().getMessage());
        // Case 3: passwords contain bad characters
        changePassword.setNewPassword("izgnmhjgaáé");
        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals((Integer) HttpStatus.BAD_REQUEST.value(), error.getBody().getStatus());
        assertEquals(BadRequestException.class, error.getBody().getException());
        assertEquals("identity.password.invalid", error.getBody().getMessage());
        // Case 4: passwords are too long
        changePassword.setNewPassword("1234569789123456789123456789123456789");
        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals((Integer) HttpStatus.BAD_REQUEST.value(), error.getBody().getStatus());
        assertEquals(BadRequestException.class, error.getBody().getException());
        assertEquals("identity.password.invalid", error.getBody().getMessage());
        // Case 5: success!!
        changePassword.setNewPassword(newPassword);
        OKResponse okResponse = restTemplate.postForObject("/auth/changePassword", changePassword, OKResponse.class);

        assertNotNull(okResponse);

        // Change email             POST    /auth/changeEmail
        // NOTE: After the previous request, the token should be expired, and user should re-authenticate
        SecuredUpdate securedUpdate = new SecuredUpdate();

        error = restTemplate.postForEntity("/auth/changeEmail", securedUpdate, APIError.class);

        assertNotNull(error);
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        assertEquals((Integer) HttpStatus.UNAUTHORIZED.value(), error.getBody().getStatus());
        assertEquals(InvalidTokenException.class, error.getBody().getException());
        assertEquals("jwt.expired", error.getBody().getMessage());
        // Change username          POST    /auth/changeUsername
        // Try grant, should fail   POST    /auth/{id}/grant
        // Try deny, should fail    POST    /auth/{id}/deny
        // Try update, should fail  POST    /auth/{id}
        // Try delete, should fail  POST    /auth/{id}
        // Try list, should fail    POST    /auth/list

        // Reset password           POST    /auth/resetPassword
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        okResponse = restTemplate.getForObject("/auth/resetPassword", OKResponse.class);

        assertNotNull(okResponse);
        verify(mailingService, times(1)).sendPasswordReset(anyString(), passwordCaptor.capture());
        String generatedPassword = passwordCaptor.getValue();
        assertNotNull(generatedPassword);
        assertEquals(8, generatedPassword.length());
    }

}
