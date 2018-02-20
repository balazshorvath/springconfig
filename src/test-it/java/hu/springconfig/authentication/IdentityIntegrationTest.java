package hu.springconfig.authentication;


import hu.springconfig.Application;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.IdentityCreate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class IdentityIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Test
    public void testRegistration() {
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

        restTemplate.postForObject("/auth/register", create, OKResponse.class);
        JWTTokenParser.TokenResponse response = restTemplate.postForObject("/auth", credentials, JWTTokenParser.TokenResponse.class);

        assertNotNull(response.getToken());
        assertNotNull(response.getIdentity());

    }

    protected void assertIdentity(Identity identity, Long id, String username, String email, String password, Set<Role> roles) {
        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(email, identity.getEmail());
        assertEquals(roles, identity.getRoles());
        assertTrue(encoder.matches(password, identity.getPassword()));
    }
}
