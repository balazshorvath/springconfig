package hu.springconfig;

import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.entity.authentication.Role;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class IntegrationTestBase {
    @Autowired
    protected TestRestTemplate restTemplate;

    protected void setupRestTemplateToken(JWTTokenParser.TokenResponse response){
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add(JWTTokenParser.AUTHENTICATION_HEADER, JWTTokenParser.TOKEN_PREFIX + response.getToken());
                    return execution.execute(request, body);
                })
        );
    }

    protected void assertIdentity(IdentityDTO identity, Long id, String username, String email, Set<Integer> roles) {
        assertNotNull(identity);
        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(email, identity.getEmail());

        Set<Role> identityRoles = identity.getRoles();
        assertEquals(roles.size(), identityRoles.size());
        for (Role role : identityRoles){
            assertTrue(roles.contains(role.getId()));
        }
    }
}
