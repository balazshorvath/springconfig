package hu.springconfig.authentication;


import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.error.APIValidationError;
import hu.springconfig.config.message.application.HttpMessages;
import hu.springconfig.config.message.entity.IdentityMessages;
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
import hu.springconfig.exception.*;
import hu.springconfig.helper.CustomPageImpl;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.util.Util;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
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
    private final Logger log = LoggerFactory.getLogger(getClass());
    @MockBean
    private MailingService mailingService;

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testAuthentication() {
        final Role userRole = roleRepository.findOne(RoleService.USER_ROLE_ID);
        final Set<Role> userRoles = Collections.singleton(userRole);
        final String email = "user@user.com";
        addIdentity(userRoles, email);

        /*
         * Test empty request
         */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<APIError> error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_BAD_REQUEST,
                BadRequestException.class,
                HttpStatus.BAD_REQUEST
        );

        /*
         * Test invalid request
         */
        entity = new HttpEntity<>("{\"usename\": \"user\", \"password\":\"user\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_BAD_REQUEST,
                BadRequestException.class,
                HttpStatus.BAD_REQUEST
        );
        /*
         * Test empty credentials
         */
        entity = new HttpEntity<>("{\"username\": \"\", \"password\":\"\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
        assertAPIError(
                error.getBody(),
                "authentication.failed.credentials",
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );
        /*
         * Test invalid credentials
         */
        entity = new HttpEntity<>("{\"username\": \"user\", \"password\":\"admin\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
        assertAPIError(
                error.getBody(),
                "authentication.failed.credentials",
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );

        entity = new HttpEntity<>("{\"username\": \"uer\", \"password\":\"user\"}", headers);
        error = restTemplate.exchange("/auth", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
        assertAPIError(
                error.getBody(),
                "authentication.failed.credentials",
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );

        /*
         * Test valid credentials
         */
        Credentials credentials = new Credentials();
        credentials.setPassword(email);
        credentials.setUsername(email);
        authenticateAndValidate(Collections.singleton(RoleService.USER_ROLE_ID), email, credentials);
    }


    @Test
    public void testIdentityFeaturesAdminRole() throws IOException {
        final Role userRole = roleRepository.findOne(RoleService.USER_ROLE_ID);
        final Role adminRole = roleRepository.findOne(RoleService.ADMIN_ROLE_ID);
        final Role managerRole = roleRepository.findByRole("MANAGER");
        final Set<Role> adminRoles = Collections.singleton(adminRole);
        final Set<Role> userRoles = Collections.singleton(userRole);
        final String password = "admin";
        Identity admin = new Identity();

        admin.setPassword(encoder.encode(password));
        admin.setEmail("admin@admin.com");
        admin.setRoles(adminRoles);

        admin = identityRepository.save(admin);
        admin = new Identity();
        admin.setPassword(encoder.encode(password));
        admin.setEmail("admin1@admin.com");
        admin.setRoles(adminRoles);
        admin = identityRepository.save(admin);

        for (int i = 0; i < 50; i++) {
            Identity identity = new Identity();
            identity.setRoles(userRoles);
            identity.setEmail("user" + i + "@user." + (i % 2 == 0 ? "hu" : "com"));
            identity.setPassword(encoder.encode("password" + i));
            identityRepository.save(identity);
        }

        /*
         * Authenticate
         */
        Credentials credentials = new Credentials();
        credentials.setUsername(admin.getEmail());
        credentials.setPassword(password);
        authenticateAndValidate(
                adminRoles.stream().map(Role::getId).collect(Collectors.toSet()),
                admin.getEmail(),
                credentials
        );

        /*
         * Specifications test
         */
        ParameterizedTypeReference<CustomPageImpl<IdentityDTO>> parameterizedTypeReference =
                new ParameterizedTypeReference<CustomPageImpl<IdentityDTO>>() {
                };
        int numberOfElements = 10;
        String pageRequest = createPageRequest(0, numberOfElements, Pair.of("name", "desc"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        FieldCondition fieldCondition = new FieldCondition(0, "email", FieldCondition.RelationalOperator.contains,
                                                           "hu"
        );
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(fieldCondition), headers);

        // PropertyReferenceException
        ResponseEntity<APIError> error = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity,
                                                               APIError.class
        );
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                error.getBody(),
                HttpMessages.SPECIFICATIONS_PROPERTY_NOT_FOUND,
                BadRequestException.class,
                HttpStatus.BAD_REQUEST
        );
        assertEquals(PropertyReferenceException.class, error.getBody().getOriginalException());

        // Fix property name
        pageRequest = createPageRequest(0, numberOfElements, Pair.of("email", "desc"));
        ResponseEntity<CustomPageImpl<IdentityDTO>> queryResult
                = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity,
                                        parameterizedTypeReference
        );
        assertPageResult(queryResult.getBody(), numberOfElements, 3, 25);

        // Test "join" properties
        fieldCondition = new FieldCondition(0, "roles.id", FieldCondition.RelationalOperator.eq, RoleService
                .ADMIN_ROLE_ID);
        pageRequest = createPageRequest(0, 5, Pair.of("email", "desc"));
        entity = new HttpEntity<>(objectMapper.writeValueAsString(fieldCondition), headers);
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity,
                                            parameterizedTypeReference
        );
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
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity,
                                            parameterizedTypeReference
        );
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
                new FieldCondition(1, "email", FieldCondition.RelationalOperator.contains, "1")
        );
        /*
         * role is "user" (50)
         * email ends with "com" (25)
         * email contains "1", user1, user10-19 (10), user21, user31, user41 (14)
         *
         * with AND: user1, user11, user13, user15, user17, user19, user21, user31, user41 (9)
         */
        pageRequest = createPageRequest(0, 5, Pair.of("email", "asc"));
        entity = new HttpEntity<>(objectMapper.writeValueAsString(conditionSet), headers);
        queryResult = restTemplate.exchange("/auth/list?" + pageRequest, HttpMethod.POST, entity,
                                            parameterizedTypeReference
        );
        CustomPageImpl<IdentityDTO> page = queryResult.getBody();
        List<IdentityDTO> content = page.getContent();
        assertPageResult(page, 5, 2, 9);

        log.info("Page response: " + page);
        IdentityDTO identity = content.get(0);
        assertEquals("user11@user.com", identity.getEmail());
        assertEquals("user13@user.com", content.get(1).getEmail());
        assertEquals("user19@user.com", content.get(4).getEmail());

        /*
         * Update
         */
        IdentityUpdate update = new IdentityUpdate();
        update.setEmail("user99@user.com");

        IdentityDTO updated = putForObject("/auth/" + identity.getId(), update, IdentityDTO.class);
        assertIdentity(updated, identity.getId(), "user99@user.com", Collections.singleton(RoleService.USER_ROLE_ID));
        /*
         * Grant
         */
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(managerRole.getId());
        updated = restTemplate.postForObject("/auth/" + updated.getId() + "/grant", roleIds, IdentityDTO.class);
        assertIdentity(
                updated,
                identity.getId(),
                "user99@user.com",
                (Set<Integer>) Util.CollectionBuilder.<Integer>newSet().add(userRole.getId()).add(managerRole.getId()
                ).get()
        );
        /*
         * Deny
         */
        roleIds = new HashSet<>();
        roleIds.add(userRole.getId());
        updated = restTemplate.postForObject("/auth/" + updated.getId() + "/deny", roleIds, IdentityDTO.class);
        assertIdentity(
                updated,
                identity.getId(),
                "user99@user.com",
                Collections.singleton(managerRole.getId())
        );
        /*
         * Delete
         */
        OKResponse response = deleteForObject("/auth/" + updated.getId(), OKResponse.class);
        assertNotNull(response);
        assertNull(identityRepository.findByEmail(updated.getEmail()));
    }

    private void assertPageResult(CustomPageImpl<IdentityDTO> identityPage, int numberOfElements, int totalPages, int
            totalElements) {
        assertNotNull(identityPage);
        assertEquals(numberOfElements, identityPage.getNumberOfElements());
        assertEquals(totalPages, identityPage.getTotalPages());
        assertEquals(totalElements, identityPage.getTotalElements());
    }


    @Test
    public void testIdentityFeaturesUserRole() throws IOException, InterruptedException {
        final Set<Integer> roles = Collections.singleton(RoleService.USER_ROLE_ID);
        final String email = "myaddress@soemthing.com";
        final String newEmail = "newmail@something.com";
        final String password = "goodpw";
        final String newPassword = "newpass12";

        IdentityCreate create = new IdentityCreate();
        Credentials credentials = new Credentials();
        ResponseEntity<APIError> error;
        ResponseEntity<APIValidationError> validationError;
        OKResponse okResponse;

        create.setEmail(email);
        create.setPassword(password);
        create.setPasswordConfirm(password);

        credentials.setUsername(email);
        credentials.setPassword(password);

        /*
         * Register and authenticate
         */
        restTemplate.postForObject("/auth/register", create, OKResponse.class);
        // Try registering the same name
        validationError = restTemplate.postForEntity("/auth/register", create, APIValidationError.class);
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIValidationError(
                validationError.getBody().getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("email_unique", IdentityMessages.IDENTITY_EMAIL_UNIQUE_VIOLATION)
        );
        IdentityDTO identityToken = authenticateAndValidate(roles, email, credentials);

        testGet(roles, email, identityToken);
        // NOTE: After this request, the token should be expired, and user should re-authenticate
        testChangePassword(password, newPassword);
        // NOTE: After this request, the token should be expired, and user should re-authenticate
//        identityToken = testChangeUsername(roles, email, newPassword, credentials);
//        assertNotNull(identityToken);
        identityToken = testChangeEmail(roles, email, newEmail, newPassword, credentials);
        assertNotNull(identityToken);

        /*
         * Try grant, should fail   POST    /auth/{id}/grant
         */
        Set<Integer> roleIds = new HashSet<>();
        roleIds.add(1000);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/grant", roleIds, APIError.class);

        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );

        /*
         * Try deny, should fail    POST    /auth/{id}/deny
         */
        roleIds = new HashSet<>();
        roleIds.add(1);
        error = restTemplate.postForEntity("/auth/" + identityToken.getId() + "/deny", roleIds, APIError.class);

        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );

        /*
         * Try update, should fail  PUT     /auth/{id}
         */
        IdentityUpdate update = new IdentityUpdate();
        update.setEmail("whatevea");

        error = putForEntity("/auth/" + identityToken.getId(), update, APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );

        /*
         * Try delete, should fail  DELETE  /auth/{id}
         */
        error = deleteForEntity("/auth/" + identityToken.getId(), APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );
        // Try list, should fail    POST    /auth/list
        error = restTemplate.postForEntity("/auth/list", new FieldCondition(), APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );

        /*
         * Reset password           POST    /auth/resetPassword
         */
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setEmail("invalid");

        error = restTemplate.postForEntity("/auth/resetPassword", resetPassword, APIError.class);
        assertResponseEntity(error, HttpStatus.NOT_FOUND);
        assertAPIError(
                error.getBody(),
                IdentityMessages.IDENTITY_NOT_FOUND,
                NotFoundException.class,
                HttpStatus.NOT_FOUND
        );

//        resetPassword.setEmail(newEmail);
//        error = restTemplate.postForEntity("/auth/resetPassword", resetPassword, APIError.class);
//        assertResponseEntity(error, HttpStatus.FORBIDDEN);
//        assertAPIError(error.getBody(),
//                       IdentityMessages.IDENTITY_RESET_PASSWORD_FAILED, ForbiddenException.class, HttpStatus
//                               .FORBIDDEN
//        );

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
        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
        assertAPIError(error.getBody(), "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        /*
         * Authenticate with the new password
         */
        credentials.setUsername(newEmail);
        credentials.setPassword(generatedPassword);
        authenticateAndValidate(roles, newEmail, credentials);
    }


    private IdentityDTO testChangeEmail(Set<Integer> roles, String email, String newEmail, String
            newPassword, Credentials credentials) throws InterruptedException {
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

        error = restTemplate.postForEntity("/auth/changeEmail", securedUpdate, APIError.class);
        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
        assertAPIError(error.getBody(), "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);

        // Acquire new token
        credentials.setPassword(newPassword);
        credentials.setUsername(email);
        identityToken = authenticateAndValidate(roles, email, credentials);

        // Try changing email again
        // Case 2: Password is invalid
        error = restTemplate.postForEntity("/auth/changeEmail", securedUpdate, APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(error.getBody(),
                       IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, ForbiddenException.class, HttpStatus
                               .FORBIDDEN
        );

        // Case 3: Success
        securedUpdate.setPassword(newPassword);
        okResponse = restTemplate.postForObject("/auth/changeEmail", securedUpdate, OKResponse.class);

        credentials.setUsername(newEmail);
        assertNotNull(okResponse);
        return authenticateAndValidate(roles, newEmail, credentials);
    }

//    private IdentityDTO testChangeUsername(Set<Integer> roles, String newUsername, String email,
//                                           String newPassword, Credentials credentials) throws InterruptedException {
//        ResponseEntity<APIError> error;
//        OKResponse okResponse;
//        // Change username          POST    /auth/changeUsername
//        // NOTE: After the previous request, the token should be expired, and user should re-authenticate
//        // Case 1: Token expired
//        SecuredUpdate securedUpdate = new SecuredUpdate();
//        securedUpdate.setPassword("wrongpw");
//        securedUpdate.setValue(newUsername);
//
//        error = restTemplate.postForEntity("/auth/changeUsername", securedUpdate, APIError.class);
//        assertResponseEntity(error, HttpStatus.UNAUTHORIZED);
//        assertAPIError(error.getBody(), "jwt.expired", InvalidTokenException.class, HttpStatus.UNAUTHORIZED);
//
//        // Acquire new token
//        credentials.setPassword(newPassword);
//        IdentityDTO identityDTO = authenticateAndValidate(roles, email, credentials);
//
//        // Try changing username again
//        // Case 2: Password is invalid
//        error = restTemplate.postForEntity("/auth/changeUsername", securedUpdate, APIError.class);
//        assertResponseEntity(error, HttpStatus.FORBIDDEN);
//        assertAPIError(error.getBody(),
//                       IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, ForbiddenException.class, HttpStatus
//                               .FORBIDDEN
//        );
//
//        // Case 3: Success
//        securedUpdate.setPassword(newPassword);
//        okResponse = restTemplate.postForObject("/auth/changeUsername", securedUpdate, OKResponse.class);
//
//        assertNotNull(okResponse);
//        return identityDTO;
//    }

    private void testChangePassword(String password, String newPassword) {
        ResponseEntity<APIError> error;
        ResponseEntity<APIValidationError> validationError;
        // Change password          POST    /auth/changePassword
        // Case 1: old password is wrong
        ChangePassword changePassword = new ChangePassword();
        changePassword.setNewPassword("newpass");
        changePassword.setNewPasswordConfirm(newPassword);
        changePassword.setOldPassword("wrongpw");

        error = restTemplate.postForEntity("/auth/changePassword", changePassword, APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(error.getBody(),
                       IdentityMessages.IDENTITY_CHECK_PASSWORD_FAILED, ForbiddenException.class, HttpStatus
                               .FORBIDDEN
        );
        // Case 2: passwords don't match
        changePassword.setOldPassword(password);

        validationError = restTemplate.postForEntity("/auth/changePassword", changePassword, APIValidationError.class);
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIValidationError(
                validationError.getBody().getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("passwordConfirm", IdentityMessages.IDENTITY_PASSWORD_CONFIRM_MISMATCH)
        );

        // Case 3: passwords contain bad characters
        changePassword.setNewPassword("izgnmhjgaáé");
        validationError = restTemplate.postForEntity("/auth/changePassword", changePassword, APIValidationError.class);
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIValidationError(
                validationError.getBody().getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("password", IdentityMessages.IDENTITY_PASSWORD_INVALID)
        );

        // Case 4: passwords are too long
        changePassword.setNewPassword("1234569789123456789123456789123456789");

        validationError = restTemplate.postForEntity("/auth/changePassword", changePassword, APIValidationError.class);
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIValidationError(
                validationError.getBody().getError(),
                IdentityMessages.IDENTITY_VALIDATION_ERROR,
                Identity.class,
                new FieldValidationError("password", IdentityMessages.IDENTITY_PASSWORD_INVALID)
        );

        // Case 5: success!!
        changePassword.setNewPassword(newPassword);
        OKResponse okResponse = restTemplate.postForObject("/auth/changePassword", changePassword, OKResponse.class);

        assertNotNull(okResponse);
    }

    private void testGet(Set<Integer> roles, String email, IdentityDTO identityToken) {
        // Get self                 GET     /auth/{id}
        IdentityDTO identity = restTemplate.getForObject("/auth/" + identityToken.getId(), IdentityDTO.class);

        assertIdentity(identity, identityToken.getId(), email, roles);

        // TODO: Use APIValidationError
        // Try get other, fail      GET     /auth/{id}
        // NOTE: Doesn't matter if the id doesn't exist. Authorization happens before that
        ResponseEntity<APIError> error = restTemplate.getForEntity("/auth/100", APIError.class);
        assertResponseEntity(error, HttpStatus.FORBIDDEN);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_FORBIDDEN_MESSAGE,
                ForbiddenException.class,
                HttpStatus.FORBIDDEN
        );

    }

}
