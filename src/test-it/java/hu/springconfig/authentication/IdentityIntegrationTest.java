package hu.springconfig.authentication;


import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.SecuredUpdate;
import hu.springconfig.data.dto.authentication.identity.*;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.Condition;
import hu.springconfig.data.query.model.ConditionSet;
import hu.springconfig.data.query.model.FieldCondition;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.*;
import hu.springconfig.helper.CustomPageImpl;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class IdentityIntegrationTest extends IntegrationTestBase {
    @Autowired
    private IIdentityRepository identityRepository;
    @Autowired
    private IRoleRepository roleRepository;
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
    public void testAuthentication() throws IOException {
        final Role userRole = roleRepository.findOne(RoleService.USER_ROLE_ID);
        final Set<Role> userRoles = Collections.singleton(userRole);
        final String username = "user";
        final String email = "user@user.com";
        Identity identity = new Identity();

        identity.setPassword(encoder.encode(username));
        identity.setEmail(email);
        identity.setUsername(username);
        identity.setRoles(userRoles);

        identity = identityRepository.save(identity);

        /*
         * Test empty request
         */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<APIError> error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertError(error, "http.bad.request", BadRequestException.class, HttpStatus.BAD_REQUEST);

        /*
         * Test invalid request
         */
        entity = new HttpEntity<>("{\"usename\": \"user\", \"password\":\"user\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertError(error, "http.bad.request", BadRequestException.class, HttpStatus.BAD_REQUEST);
        /*
         * Test empty credentials
         */
        entity = new HttpEntity<>("{\"username\": \"\", \"password\":\"\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertError(error, "authentication.failed.credentials", AuthenticationFailedException.class, HttpStatus.UNAUTHORIZED);
        /*
         * Test invalid credentials
         */
        entity = new HttpEntity<>("{\"username\": \"user\", \"password\":\"admin\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertError(error, "authentication.failed.credentials", AuthenticationFailedException.class, HttpStatus.UNAUTHORIZED);
        /*
         * Test valid credentials
         */
        Credentials credentials = new Credentials();
        credentials.setPassword(username);
        credentials.setUsername(username);
        authenticateAndValidate(Collections.singleton(RoleService.USER_ROLE_ID), username, email, credentials);
    }

    @Test
    public void testIdentityFeaturesAdminRole() throws IOException {
        final Role userRole = roleRepository.findOne(RoleService.USER_ROLE_ID);
        final Role adminRole = roleRepository.findOne(RoleService.ADMIN_ROLE_ID);
        final Role managerRole = roleRepository.save(
                new Role(500, "MANAGER", Collections.singleton(new Privilege(Privilege.Privileges.IDENTITY_GRANT)))
        );
        final Set<Role> adminRoles = Collections.singleton(adminRole);
        final Set<Role> userRoles = Collections.singleton(userRole);
        final String password = "admin";
        Identity admin = new Identity();

        admin.setPassword(encoder.encode(password));
        admin.setEmail("admin@admin.com");
        admin.setUsername(password);
        admin.setRoles(adminRoles);

        admin = identityRepository.save(admin);

        for (int i = 0; i < 50; i++) {
            Identity identity = new Identity();
            identity.setRoles(userRoles);
            identity.setUsername("user" + i);
            identity.setEmail("user" + i + "@user." + (i % 2 == 0 ? "hu" : "com"));
            identity.setPassword(encoder.encode("password" + i));
            identityRepository.save(identity);
        }

        /*
         * Authenticate
         */
        Credentials credentials = new Credentials();
        credentials.setUsername(admin.getUsername());
        credentials.setPassword(password);
        authenticateAndValidate(adminRoles.stream().map(Role::getId).collect(Collectors.toSet()), admin.getUsername(), admin.getEmail(), credentials);

        /*
         * Specifications test
         */
        ParameterizedTypeReference<CustomPageImpl<IdentityDTO>> parameterizedTypeReference = new ParameterizedTypeReference<CustomPageImpl<IdentityDTO>>() {
        };
        int numberOfElements = 10;
        String pageRequest = createPageRequest(0, numberOfElements, Pair.of("name", "desc"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        FieldCondition fieldCondition = new FieldCondition(0, "email", FieldCondition.RelationalOperator.contains, "hu");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(fieldCondition), headers);

        // PropertyReferenceException
        ResponseEntity<APIError> error = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity, APIError.class);
        assertError(error, "specifications.property.not_found", BadRequestException.class, HttpStatus.BAD_REQUEST);
        assertEquals(PropertyReferenceException.class, error.getBody().getOriginalException());

        // Fix property name
        pageRequest = createPageRequest(0, numberOfElements, Pair.of("email", "desc"));
        ResponseEntity<CustomPageImpl<IdentityDTO>> queryResult
                = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity, parameterizedTypeReference);
        assertPageResult(queryResult.getBody(), numberOfElements, 3, 25);

        // Test "join" properties
        fieldCondition = new FieldCondition(0, "roles.id", FieldCondition.RelationalOperator.eq, RoleService.ADMIN_ROLE_ID);
        pageRequest = createPageRequest(0, 5, Pair.of("email", "desc"));
        entity = new HttpEntity<>(objectMapper.writeValueAsString(fieldCondition), headers);
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity, parameterizedTypeReference);
        assertPageResult(queryResult.getBody(), 2, 1, 2);

        // Test "join" properties
        fieldCondition = new FieldCondition(
                0,
                "roles.privileges.id",
                FieldCondition.RelationalOperator.eq,
                Privilege.Privileges.IDENTITY_GET.getValue()
        );
        pageRequest = createPageRequest(0, 5, Pair.of("email", "desc"));
        entity = new HttpEntity<>(objectMapper.writeValueAsString(fieldCondition), headers);
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity, parameterizedTypeReference);
        assertPageResult(queryResult.getBody(), 2, 1, 2);

        // Use condition set
        ConditionSet conditionSet = new ConditionSet(Condition.LogicalOperator.and, 0, new ArrayList<>());
        conditionSet.getConditions().add(
                new FieldCondition(1, "roles.id", FieldCondition.RelationalOperator.eq, RoleService.USER_ROLE_ID)
        );
        conditionSet.getConditions().add(
                new FieldCondition(1, "email", FieldCondition.RelationalOperator.endswith, "com")
        );
        conditionSet.getConditions().add(
                new FieldCondition(1, "username", FieldCondition.RelationalOperator.contains, "1")
        );
        /*
         * role is "user" (50)
         * email ends with "com" (25)
         * username contains "1", user1, user10-19 (10), user21, user31, user41 (14)
         *
         * with AND: user1, user11, user13, user15, user17, user19, user21, user31, user41 (9)
         */
        pageRequest = createPageRequest(0, 5, Pair.of("username", "asc"));
        entity = new HttpEntity<>(objectMapper.writeValueAsString(conditionSet), headers);
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity, parameterizedTypeReference);
        CustomPageImpl<IdentityDTO> page = queryResult.getBody();
        List<IdentityDTO> content = page.getContent();
        assertPageResult(page, 5, 2, 9);

        IdentityDTO identity = content.get(0);
        assertEquals("user1", identity.getUsername());
        assertEquals("user11", content.get(1).getUsername());
        assertEquals("user17", content.get(4).getUsername());

        /*
         * Update
         */
        IdentityUpdate update = new IdentityUpdate();
        update.setEmail("user99@user.com");
        update.setUsername("user99");

        IdentityDTO updated = putForObject("/auth/" + identity.getId(), update, IdentityDTO.class);
        assertIdentity(updated, identity.getId(), "user99", "user99@user.com", Collections.singleton(RoleService.USER_ROLE_ID));
        /*
         * Grant
         */
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(managerRole.getId());
        updated = restTemplate.postForObject("/auth/" + updated.getId() + "/grant", roleIds, IdentityDTO.class);
        assertIdentity(updated,
                identity.getId(),
                "user99",
                "user99@user.com",
                (Set<Integer>) Util.CollectionBuilder.<Integer>newSet().add(userRole.getId()).add(managerRole.getId()).get()
        );
        /*
         * Deny
         */
        roleIds = new HashSet<>();
        roleIds.add(userRole.getId());
        updated = restTemplate.postForObject("/auth/" + updated.getId() + "/deny", roleIds, IdentityDTO.class);
        assertIdentity(updated,
                identity.getId(),
                "user99",
                "user99@user.com",
                Collections.singleton(managerRole.getId())
        );
        /*
         * Delete
         */
        OKResponse response = deleteForObject("/auth/" + updated.getId(), OKResponse.class);
        assertNotNull(response);
        assertNull(identityRepository.findByUsername(updated.getUsername()));
    }

    private void assertPageResult(CustomPageImpl<IdentityDTO> identityPage, int numberOfElements, int totalPages, int totalElements) {
        assertNotNull(identityPage);
        assertEquals(numberOfElements, identityPage.getNumberOfElements());
        assertEquals(totalPages, identityPage.getTotalPages());
        assertEquals(totalElements, identityPage.getTotalElements());
    }


    @Test
    public void testIdentityFeaturesUserRole() throws IOException, InterruptedException {
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

        /*
         * Register and authenticate
         */
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

        /*
         * Try grant, should fail   POST    /auth/{id}/grant
         */
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(1000);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/grant", roleIds, APIError.class);

        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        /*
         * Try deny, should fail    POST    /auth/{id}/deny
         */
        roleIds = new HashSet<>();
        roleIds.add(1);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/deny", roleIds, APIError.class);

        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        /*
         * Try update, should fail  PUT     /auth/{id}
         */
        IdentityUpdate update = new IdentityUpdate();
        update.setEmail("whatevea");
        update.setUsername("asdqt");

        error = putForEntity("/auth/" + identityToken.getId(), update, APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        /*
         * Try delete, should fail  DELETE  /auth/{id}
         */
        error = deleteForEntity("/auth/" + identityToken.getId(), APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);
        // Try list, should fail    POST    /auth/list
        error = restTemplate.postForEntity("/auth/list", new FieldCondition(), APIError.class);
        assertError(error, "forbidden.message", ForbiddenException.class, HttpStatus.FORBIDDEN);

        /*
         * Reset password           POST    /auth/resetPassword
         */
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

        /*
         * Test token expiration
         */
        error = restTemplate.getForEntity("/auth/" + identityToken.getId(), APIError.class);
        assertError(error, "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        /*
         * Authenticate with the new password
         */
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
