package hu.springconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.security.authentication.JWTTokenParser;
import hu.springconfig.data.dto.account.AccountDTO;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.entity.Account;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.account.IAccountRepository;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.data.repository.meal.IMealRepository;
import hu.springconfig.exception.ResponseException;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IntegrationTestBase {
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected PasswordEncoder encoder;
    @Autowired
    protected IRoleRepository roleRepository;
    @Autowired
    protected IIdentityRepository identityRepository;
    @Autowired
    private IMealRepository mealRepository;
    @Autowired
    protected IAccountRepository accountRepository;

    @Before
    public void setup() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setOutputStreaming(false);
        restTemplate.getRestTemplate().setRequestFactory(factory);
        unsetRestTemplateToken();
        mealRepository.deleteAll();
        accountRepository.deleteAll();
        identityRepository.deleteAll();
    }

    private void setupRestTemplateToken(JWTTokenParser.TokenResponse response) {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                           .add(
                                   JWTTokenParser.AUTHENTICATION_HEADER,
                                   JWTTokenParser.TOKEN_PREFIX + response.getToken()
                           );
                    return execution.execute(request, body);
                })
        );
    }

    protected void unsetRestTemplateToken() {
        restTemplate.getRestTemplate().setInterceptors(Collections.emptyList());
    }

    private <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, Object request, Class<T> responseType)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity;
        if (request == null) {
            entity = new HttpEntity<>(headers);
        } else {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);
        }
        return restTemplate.exchange(url, httpMethod, entity, responseType);
    }

    protected IdentityDTO authenticateAndValidate(Set<Integer> roles, String email, Credentials
            credentials) {
        JWTTokenParser.TokenResponse response = restTemplate.postForObject(
                "/auth",
                credentials,
                JWTTokenParser.TokenResponse.class
        );

        IdentityDTO identityToken = response.getIdentity();
        assertIdentity(identityToken, identityToken.getId(), email, roles);
        assertNotNull(response.getToken());

        setupRestTemplateToken(response);
        return identityToken;
    }

    protected IdentityDTO authenticateAndValidate(Set<Integer> roles, String email) {
        Credentials credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(email);
        return authenticateAndValidate(roles, email, credentials);
    }


    protected void assertIdentity(IdentityDTO identity, Long id, String email, Set<Integer> roles) {
        assertNotNull(identity);
        assertEquals(id, identity.getId());
        assertEquals(email, identity.getEmail());

        Set<Role> identityRoles = identity.getRoles();
        assertEquals(roles.size(), identityRoles.size());
        for (Role role : identityRoles) {
            assertTrue(roles.contains(role.getId()));
        }
    }

    protected void assertAccount(AccountDTO accountDTO, Long id, String email, Set<Integer> roles,
                                 String firstName, String lastName, Long dailyCalorieGoal) {
        assertNotNull(accountDTO);
        assertIdentity(accountDTO.getIdentity(), id, email, roles);
        assertEquals(firstName, accountDTO.getFirstName());
        assertEquals(lastName, accountDTO.getLastName());
        assertEquals(dailyCalorieGoal, accountDTO.getDailyCalorieGoal());
    }

    protected void assertResponseEntity(ResponseEntity<? extends APIError> error, HttpStatus status) {
        assertNotNull(error);
        assertNotNull(error.getBody());
        assertEquals(status, error.getStatusCode());
        assertEquals((Integer) status.value(), error.getBody().getStatus());
    }

    protected void assertAPIError(APIError error, String message, Class<? extends ResponseException> exception,
                                  HttpStatus status) {
        assertNotNull(error);
        assertEquals((Integer) status.value(), error.getStatus());
        assertEquals(exception, error.getException());
        assertEquals(message, error.getMessage());
    }

    protected void assertAPIValidationError(TypeValidationError error, String errorMessage, Class<?> type,
                                            FieldValidationError... fieldErrors) {
        assertNotNull(error);
        assertEquals(type, error.getType());
        assertEquals(errorMessage, error.getMessage());
        assertEquals(fieldErrors.length, error.getErrors().size());
        Set<FieldValidationError> errorSet = new HashSet<>(error.getErrors());
        for (FieldValidationError fieldValidationError : fieldErrors) {
            assertTrue(errorSet.contains(fieldValidationError));
        }
    }

    protected String createPageRequest(Integer page, Integer size, Pair... sorts) {
        StringBuilder sb = new StringBuilder();
        sb.append("page=").append(page);
        sb.append('&').append("size=").append(size);
        for (Pair sort : sorts) {
            sb.append('&').append("sort=").append(sort.getFirst()).append(',').append(sort.getSecond());
        }
        return sb.toString();
    }

    protected Identity addIdentity(Set<Role> userRoles, String email) {
        Identity identity = new Identity();

        identity.setPassword(encoder.encode(email));
        identity.setEmail(email);
        identity.setRoles(userRoles);

        return identityRepository.save(identity);
    }

    protected Account addAccount(Set<Role> userRoles, String email, String firstName, String
            lastName, Long dailyGoal) {
        Account account = new Account();
        Identity identity = addIdentity(userRoles, email);
        account.setDailyCalorieGoal(dailyGoal);
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setIdentity(identity);
        return accountRepository.save(account);
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

    public void cleanup() {
        mealRepository.deleteAll();
        accountRepository.deleteAll();
        identityRepository.deleteAll();
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
