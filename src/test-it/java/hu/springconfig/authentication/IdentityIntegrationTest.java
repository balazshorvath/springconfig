package hu.springconfig.authentication;


import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.identity.IdentityCreate;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.service.authentication.RoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class IdentityIntegrationTest extends IntegrationTestBase {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Test
    public void testIdentityCRUD() {
        final Set<Integer> roles = Collections.singleton(RoleService.USER_ROLE_ID);
        final String username = "myname";
        final String email = "myaddress@soemthing.com";
        final String password = "goodpw";

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
        APIError error = restTemplate.getForObject("/auth/100", APIError.class);

        assertEquals((Integer) HttpStatus.FORBIDDEN.value(), error.getStatus());

        // Change password          POST    /auth/changePassword
        // Change email             POST    /auth/changeEmail
        // Change username          POST    /auth/changeUsername
        // Try grant, should fail   POST    /auth/{id}/grant
        // Try deny, should fail    POST    /auth/{id}/deny
        // Try update, should fail  POST    /auth/{id}
        // Try delete, should fail  POST    /auth/{id}
        // Try list, should fail    POST    /auth/list

    }

}
