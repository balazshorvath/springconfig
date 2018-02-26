package hu.springconfig.authentication;


import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.SecuredUpdate;
import hu.springconfig.data.dto.authentication.identity.*;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.query.model.FieldCondition;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.exception.*;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.mail.MailingService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
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
    public void testIdentityCRUD() throws IOException, InterruptedException {
        final Set<Integer> roles = Collections.singleton(RoleService.USER_ROLE_ID);
        final String username = "myname";
        final String newUsername = "newusername";
        final String email = "myaddress@soemthing.com";
        final String newEmail = "newmail@something.com";
        final String password = "goodpw";
        final String newPassword = "newpass12";

        IdentityCreate create = new IdentityCreate();
        Credentials credentials = new Credentials();
        ResponseEntity<APIError> error;
        OKResponse okResponse;

        create.setEmail(email);
        create.setPassword(password);
        create.setPasswordConfirm(password);
        create.setUsername(username);

        credentials.setUsername(username);
        credentials.setPassword(password);

        // Register and authenticate
        restTemplate.postForObject("/auth/register", create, OKResponse.class);
        IdentityDTO identityToken = authenticateAndValidate(roles, username, email, credentials);

        testGet(roles, username, email, identityToken);
        // NOTE: After this request, the token should be expired, and user should re-authenticate
        testChangePassword(password, newPassword);
        // NOTE: After this request, the token should be expired, and user should re-authenticate
        identityToken = testChangeUsername(roles, username, newUsername, email, newPassword, credentials);
        assertNotNull(identityToken);
        identityToken = testChangeEmail(roles, newUsername, email, newEmail, newPassword, credentials);
        assertNotNull(identityToken);

        // Try grant, should fail   POST    /auth/{id}/grant
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(1000);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/grant", roleIds, APIError.class);

        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Try deny, should fail    POST    /auth/{id}/deny
        roleIds = new HashSet<>();
        roleIds.add(1);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/deny", roleIds, APIError.class);

        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Try update, should fail  PUT     /auth/{id}
        IdentityUpdate update = new IdentityUpdate();
        update.setEmail("whatevea");
        update.setUsername("asdqt");

        error = putForEntity("/auth/" + identityToken.getId(), update, APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Try delete, should fail  DELETE  /auth/{id}
        error = deleteForEntity("/auth/" + identityToken.getId(), APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);
        // Try list, should fail    POST    /auth/list
        error = restTemplate.postForEntity("/auth/list", new FieldCondition(), APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Reset password           POST    /auth/resetPassword
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setEmail("invalid");
        resetPassword.setUsername("invalid");

        error = restTemplate.postForEntity("/auth/resetPassword", resetPassword, APIError.class);
        assertError(error, "identity.not_found", NotFoundException.class, HttpStatus.NOT_FOUND);

        resetPassword.setUsername(newUsername);
        error = restTemplate.postForEntity("/auth/resetPassword", resetPassword, APIError.class);
        assertError(error, "identity.reset_password_failed", ForbiddenException.class, HttpStatus.FORBIDDEN);

        resetPassword.setEmail(newEmail);
        okResponse = restTemplate.postForObject("/auth/resetPassword", resetPassword, OKResponse.class);

        assertNotNull(okResponse);
        verify(mailingService, times(1)).sendPasswordReset(anyString(), passwordCaptor.capture());
        String generatedPassword = passwordCaptor.getValue();
        assertNotNull(generatedPassword);
        assertEquals(8, generatedPassword.length());

        // Test token expiration
        error = restTemplate.getForEntity("/auth/" + identityToken.getId(), APIError.class);
        assertError(error, "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        // Authenticate with the new password
        credentials.setPassword(generatedPassword);
        authenticateAndValidate(roles, newUsername, newEmail, credentials);
    }


    private IdentityDTO testChangeEmail(Set<Integer> roles, String username, String email, String newEmail, String newPassword, Credentials credentials) throws InterruptedException {
        SecuredUpdate securedUpdate;
        ResponseEntity<APIError> error;
        IdentityDTO identityToken;
        OKResponse okResponse;
        // Change email             POST    /auth/changeEmail
        // NOTE: After the previous request, the token should be expired, and user should re-authenticate
        // Case 1: Token expired
        securedUpdate = new SecuredUpdate();
        securedUpdate.setPassword("wrongpw");
        securedUpdate.setValue(newEmail);

//        Thread.sleep(1000);
        error = restTemplate.postForEntity("/auth/changeEmail", securedUpdate, APIError.class);
        assertError(error, "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        // Acquire new token
        credentials.setPassword(newPassword);
        credentials.setUsername(username);
        identityToken = authenticateAndValidate(roles, username, email, credentials);

        // Try changing email again
        // Case 2: Password is invalid
        error = restTemplate.postForEntity("/auth/changeEmail", securedUpdate, APIError.class);
        assertError(error, "identity.check_password_failed", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Case 3: Success
        securedUpdate.setPassword(newPassword);
        okResponse = restTemplate.postForObject("/auth/changeEmail", securedUpdate, OKResponse.class);

        assertNotNull(okResponse);
        return identityToken;
    }

    private IdentityDTO testChangeUsername(Set<Integer> roles, String username, String newUsername, String email, String newPassword, Credentials credentials) throws InterruptedException {
        ResponseEntity<APIError> error;
        OKResponse okResponse;
        // Change username          POST    /auth/changeUsername
        // NOTE: After the previous request, the token should be expired, and user should re-authenticate
        // Case 1: Token expired
        SecuredUpdate securedUpdate = new SecuredUpdate();
        securedUpdate.setPassword("wrongpw");
        securedUpdate.setValue(newUsername);

//        Thread.sleep(1000);
        error = restTemplate.postForEntity("/auth/changeUsername", securedUpdate, APIError.class);
        assertError(error, "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        // Acquire new token
        credentials.setPassword(newPassword);
        IdentityDTO identityDTO = authenticateAndValidate(roles, username, email, credentials);

        // Try changing username again
        // Case 2: Password is invalid
        error = restTemplate.postForEntity("/auth/changeUsername", securedUpdate, APIError.class);
        assertError(error, "identity.check_password_failed", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Case 3: Success
        securedUpdate.setPassword(newPassword);
        okResponse = restTemplate.postForObject("/auth/changeUsername", securedUpdate, OKResponse.class);

        assertNotNull(okResponse);
        return identityDTO;
    }

    private void testChangePassword(String password, String newPassword) {
        ResponseEntity<APIError> error;
        // Change password          POST    /auth/changePassword
        // Case 1: old password is wrong
        ChangePassword changePassword = new ChangePassword();
        changePassword.setNewPassword("newpass");
        changePassword.setNewPasswordConfirm(newPassword);
        changePassword.setOldPassword("wrongpw");

        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);
        assertError(error, "identity.check_password_failed", ForbiddenException.class, HttpStatus.FORBIDDEN);

        // Case 2: passwords don't match
        changePassword.setOldPassword(password);

        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);
        assertError(error, "identity.password.confirm.mismatch", BadRequestException.class, HttpStatus.BAD_REQUEST);

        // Case 3: passwords contain bad characters
        changePassword.setNewPassword("izgnmhjgaáé");
        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);
        assertError(error, "identity.password.invalid", BadRequestException.class, HttpStatus.BAD_REQUEST);

        // Case 4: passwords are too long
        changePassword.setNewPassword("1234569789123456789123456789123456789");

        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);
        assertError(error, "identity.password.invalid", BadRequestException.class, HttpStatus.BAD_REQUEST);

        // Case 5: success!!
        changePassword.setNewPassword(newPassword);
        OKResponse okResponse = restTemplate.postForObject("/auth/changePassword", changePassword, OKResponse.class);

        assertNotNull(okResponse);
    }

    private void testGet(Set<Integer> roles, String username, String email, IdentityDTO identityToken) {
        // Get self                 GET     /auth/{id}
        IdentityDTO identity = restTemplate.getForObject("/auth/" + identityToken.getId(), IdentityDTO.class);

        assertIdentity(identity, identityToken.getId(), username, email, roles);

        // Try get other, fail      GET     /auth/{id}
        // NOTE: Doesn't matter if the id doesn't exist. Authorization happens before that
        ResponseEntity<APIError> error = restTemplate.getForEntity("/auth/100", APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);
    }

}
