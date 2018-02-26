package hu.springconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.dto.authentication.identity.IdentityUpdate;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.ResponseException;
import io.jsonwebtoken.Claims;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class IntegrationTestBase {
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setOutputStreaming(false);
        restTemplate.getRestTemplate().setRequestFactory(factory);
    }

    private void setupRestTemplateToken(JWTTokenParser.TokenResponse response) {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add(JWTTokenParser.AUTHENTICATION_HEADER, JWTTokenParser.TOKEN_PREFIX + response.getToken());
                    return execution.execute(request, body);
                })
        );
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, Object request, Class<T> responseType) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity;
        if(request == null){
            entity = new HttpEntity<>(headers);
        }else {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);
        }
        return restTemplate.exchange(url, httpMethod, entity, responseType);
    }

    protected IdentityDTO authenticateAndValidate(Set<Integer> roles, String username, String email, Credentials credentials) {
        JWTTokenParser.TokenResponse response = restTemplate.postForObject("/auth", credentials, JWTTokenParser.TokenResponse.class);

        IdentityDTO identityToken = response.getIdentity();
        assertIdentity(identityToken, identityToken.getId(), username, email, roles);
        assertNotNull(response.getToken());

        setupRestTemplateToken(response);
        return identityToken;
    }


    protected void assertIdentity(IdentityDTO identity, Long id, String username, String email, Set<Integer> roles) {
        assertNotNull(identity);
        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(email, identity.getEmail());

        Set<Role> identityRoles = identity.getRoles();
        assertEquals(roles.size(), identityRoles.size());
        for (Role role : identityRoles) {
            assertTrue(roles.contains(role.getId()));
        }
    }

    protected void assertError(ResponseEntity<APIError> error, String message, Class<? extends ResponseException> exception, HttpStatus status) {
        assertNotNull(error);
        assertNotNull(error.getBody());
        assertEquals(status, error.getStatusCode());
        assertEquals((Integer) status.value(), error.getBody().getStatus());
        assertEquals(exception, error.getBody().getException());
        assertEquals(message, error.getBody().getMessage());
    }


    protected <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType) throws IOException {
        return exchange(url, HttpMethod.PUT, request, responseType);
    }

    protected <T> T putForObject(String url, Object request, Class<T> responseType) throws IOException {
        return exchange(url, HttpMethod.PUT, request, responseType).getBody();
    }

    protected <T> ResponseEntity<T> deleteForEntity(String url, Class<T> responseType) throws IOException {
        return exchange(url, HttpMethod.DELETE, null, responseType);
    }

    protected <T> T deleteForObject(String url, Class<T> responseType) throws IOException {
        return exchange(url, HttpMethod.DELETE, null, responseType).getBody();
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public ClientHttpRequestFactory clientHttpRequestFactory() {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setOutputStreaming(false);
            return requestFactory;
        }
    }
}
