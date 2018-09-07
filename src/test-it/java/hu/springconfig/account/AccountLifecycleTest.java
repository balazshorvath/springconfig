package hu.springconfig.account;

import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.error.APIValidationError;
import hu.springconfig.config.message.application.AuthenticationMessages;
import hu.springconfig.config.message.entity.IdentityMessages;
import hu.springconfig.config.message.entity.InviteMessages;
import hu.springconfig.data.dto.account.AccountCreate;
import hu.springconfig.data.dto.account.AccountDTO;
import hu.springconfig.data.dto.account.AccountUpdate;
import hu.springconfig.data.dto.account.InviteCreate;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.identity.IdentityCreate;
import hu.springconfig.data.dto.authentication.identity.IdentityDTO;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.Invite;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.query.model.FieldCondition;
import hu.springconfig.data.repository.account.IInviteRepository;
import hu.springconfig.exception.AccountLockedException;
import hu.springconfig.exception.AuthenticationFailedException;
import hu.springconfig.exception.ResponseException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.helper.CustomPageImpl;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.service.mail.MailingService;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class AccountLifecycleTest extends IntegrationTestBase {

    @Autowired
    private IInviteRepository inviteRepository;
    @MockBean
    private MailingService mailingService;

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Override
    @After
    public void cleanup() {
        super.cleanup();
        inviteRepository.deleteAll();
    }

    @Test
    public void testInvite() throws IOException {
        final Role adminRole = roleRepository.findOne(RoleService.ADMIN_ROLE_ID);
        final Set<Role> adminRoles = Collections.singleton(adminRole);
        final String email = "user@admin.com";
        final String firstName = "first";
        final String lastName = "last";
        final Long dailyCalorieGoal = 10000L;
        addAccount(adminRoles, email, firstName, lastName, dailyCalorieGoal);

        Credentials credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(email);
        authenticateAndValidate(
                adminRoles.stream().map(Role::getId).collect(Collectors.toSet()),
                email,
                credentials
        );

        /*
         * Invite User1.
         */
        final String invitedMail = "invitedus3r";
        InviteCreate inviteCreate = new InviteCreate();
        String inviteKey;

        inviteCreate.setEmail(invitedMail);
        Invite invite = restTemplate.postForObject("/account/invite", inviteCreate, Invite.class);
        // Try inviting the same address.
        ResponseEntity<APIValidationError> error = restTemplate.postForEntity(
                "/account/invite",
                inviteCreate,
                APIValidationError.class
        );
        assertAPIError(
                error.getBody(),
                InviteMessages.INVITE_VALIDATION_ERROR,
                ValidationException.class,
                HttpStatus.CONFLICT
        );
        assertAPIValidationError(
                error.getBody().getError(),
                InviteMessages.INVITE_VALIDATION_ERROR,
                Invite.class,
                new FieldValidationError(
                        "invite_email_unique",
                        InviteMessages.INVITE_EMAIL_UNIQUE_ERROR
                )
        );

        ArgumentCaptor<String> inviteKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailingService, times(1)).sendInvite(eq(invitedMail), inviteKeyCaptor.capture());
        inviteKey = inviteKeyCaptor.getValue();

        assertNotNull(invite);
        assertNotNull(inviteKey);
        assertFalse(invite.isUsed());
        assertEquals(invitedMail, invite.getEmail());
        assertEquals(inviteKey, invite.getInviteKey());

        /*
         * "Logout" and accept invite.
         */
        unsetRestTemplateToken();

        AccountCreate account = new AccountCreate();
        IdentityCreate newIdentity = new IdentityCreate();
        final String newPassword = "stronkp4ss";
        final String newFirstName = "first";
        final String newLastName = "last";
        final Long newDailyCalorieGoal = 10000L;

        newIdentity.setPassword(newPassword);
        newIdentity.setPasswordConfirm(newPassword);

        account.setDailyCalorieGoal(newDailyCalorieGoal);
        account.setFirstName(newFirstName);
        account.setLastName(newLastName);
        account.setIdentity(newIdentity);

        OKResponse response = restTemplate.postForObject(
                "/account/invite/" + inviteKey + "/accept",
                account,
                OKResponse.class
        );
        // Try activating again
        ResponseEntity<APIError> errorResponseEntity = restTemplate.postForEntity(
                "/account/invite/" + inviteKey + "/accept",
                account,
                APIError.class
        );
        assertResponseEntity(errorResponseEntity, HttpStatus.CONFLICT);
        assertAPIError(
                errorResponseEntity.getBody(),
                InviteMessages.INVITE_USED,
                ResponseException.class,
                HttpStatus.CONFLICT
        );

        assertNotNull(response);

        credentials = new Credentials();
        credentials.setUsername(invitedMail);
        credentials.setPassword(newPassword);
        Set<Integer> userRoles = Collections.singleton(RoleService.USER_ROLE_ID);
        IdentityDTO identityDTO = authenticateAndValidate(
                userRoles,
                invitedMail,
                credentials
        );

        // GET account
        AccountDTO accountDTO = restTemplate.getForObject("/account/" + identityDTO.getId(), AccountDTO.class);
        assertNotNull(accountDTO);
        /*
         * Update account
         */
        AccountUpdate update = new AccountUpdate();
        final String updateFirstName = "Second";
        final String updateLastName = "First";
        final Long updateCalorieGoal = 2L;
        update.setDailyCalorieGoal(updateCalorieGoal);
        update.setFirstName(updateFirstName);
        update.setLastName(updateLastName);
        update.setVersion(accountDTO.getVersion());

        accountDTO = putForObject(
                "/account/" + accountDTO.getIdentity().getId(),
                update,
                AccountDTO.class
        );

        assertAccount(
                accountDTO,
                identityDTO.getId(),
                invitedMail,
                userRoles,
                updateFirstName,
                updateLastName,
                updateCalorieGoal
        );
        /*
         * List and delete invites
         */
        credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(email);
        authenticateAndValidate(
                adminRoles.stream().map(Role::getId).collect(Collectors.toSet()),
                email,
                credentials
        );
        // Get previous invite and check if it's marked as used
        invite = restTemplate.getForObject("/account/invite/" + invite.getId(), Invite.class);
        assertTrue(invite.isUsed());

        /*
         * Dynamic query
         */
        FieldCondition condition = new FieldCondition(0, "email", FieldCondition.RelationalOperator.eq, invitedMail);
        ParameterizedTypeReference<CustomPageImpl<Invite>> parameterizedTypeReference =
                new ParameterizedTypeReference<CustomPageImpl<Invite>>() {
                };
        String pageRequest = createPageRequest(0, 5);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(condition), headers);
        ResponseEntity<CustomPageImpl<Invite>> invites = restTemplate.exchange(
                "/account/invite/list?" + pageRequest,
                HttpMethod.POST,
                entity,
                parameterizedTypeReference
        );
        assertNotNull(invites.getBody());
        assertEquals(1, invites.getBody().getNumberOfElements());
        assertEquals(invite, invites.getBody().getContent().get(0));

        /*
         * Delete invite
         */
        response = deleteForObject("/account/invite/" + invite.getId(), OKResponse.class);
        assertNotNull(response);

        /*
         * List accounts
         */
        ParameterizedTypeReference<CustomPageImpl<AccountDTO>> accountPageTypeRef =
                new ParameterizedTypeReference<CustomPageImpl<AccountDTO>>() {
                };
        entity = new HttpEntity<>("", headers);
        ResponseEntity<CustomPageImpl<AccountDTO>> accounts = restTemplate.exchange(
                "/account/list",
                HttpMethod.POST,
                entity,
                accountPageTypeRef
        );
        assertNotNull(accounts.getBody());
        assertEquals(2, accounts.getBody().getNumberOfElements());
        /*
         * Delete account
         */
        response = deleteForObject("/account/" + accountDTO.getIdentity().getId(), OKResponse.class);
        assertNotNull(response);

    }

    @Test
    public void testRegister() {
        final Set<Integer> userRoles = Collections.singleton(RoleService.USER_ROLE_ID);
        AccountCreate account = new AccountCreate();
        IdentityCreate identity = new IdentityCreate();
        final String email = "mailz";
        final String password = "stronkp4ss";
        final String firstName = "first";
        final String lastName = "last";
        final Long dailyCalorieGoal = 10000L;

        identity.setEmail(email);
        identity.setPassword(password);
        identity.setPasswordConfirm(password);

        account.setDailyCalorieGoal(dailyCalorieGoal);
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setIdentity(identity);

        OKResponse response = restTemplate.postForObject("/account/register", account, OKResponse.class);
        assertNotNull(response);

        ArgumentCaptor<String> verificationCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailingService, times(1)).sendVerification(anyString(), anyString(), verificationCaptor.capture());
        String verificationKey = verificationCaptor.getValue();

        response = restTemplate.postForObject("/account/" + verificationKey + "/verify", null, OKResponse.class);
        assertNotNull(response);

        Credentials credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(password);
        IdentityDTO identityDTO = authenticateAndValidate(
                userRoles,
                email,
                credentials
        );

        ResponseEntity<AccountDTO> accountDTO = restTemplate.getForEntity(
                "/account/" + identityDTO.getId(),
                AccountDTO.class
        );

        assertAccount(
                accountDTO.getBody(),
                identityDTO.getId(),
                email,
                userRoles,
                firstName,
                lastName,
                dailyCalorieGoal
        );

        /*
         * "Logout" and add admin user
         */
        unsetRestTemplateToken();
        Set<Role> adminRole = Collections.singleton(roleRepository.findOne(RoleService.ADMIN_ROLE_ID));
        String adminEmail = "admin";
        addAccount(
                adminRole,
                adminEmail,
                firstName,
                lastName,
                dailyCalorieGoal
        );

        /*
         * Failed login 3 times.
         */
        failedLogin(
                email,
                "wrongq1",
                AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS,
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );
        failedLogin(
                email,
                "wrong2",
                AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS,
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );
        failedLogin(
                email,
                "wrong3",
                AuthenticationMessages.AUTHENTICATION_FAILED_CREDENTIALS,
                AuthenticationFailedException.class,
                HttpStatus.UNAUTHORIZED
        );
        failedLogin(
                email,
                password,
                IdentityMessages.IDENTITY_LOCKED,
                AccountLockedException.class,
                HttpStatus.FORBIDDEN
        );
        /*
         * Login as admin.
         */
        credentials = new Credentials();
        credentials.setUsername(adminEmail);
        credentials.setPassword(adminEmail);
        authenticateAndValidate(
                Collections.singleton(RoleService.ADMIN_ROLE_ID),
                adminEmail,
                credentials
        );

        response = restTemplate.postForObject("/auth/" + identityDTO.getId() + "/unlock", null, OKResponse.class);
        assertNotNull(response);

        /*
         * "Logout" and try with user
         */
        unsetRestTemplateToken();
        credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(password);
        authenticateAndValidate(userRoles, email, credentials);

    }

    private void failedLogin(String email, String password, String message, Class<?> exception, HttpStatus status) {
        Credentials credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(password);

        ResponseEntity<APIError> response = restTemplate.postForEntity("/auth", credentials, APIError.class);

        APIError error = response.getBody();
        assertNotNull(error);
        assertEquals(message, error.getMessage());
        assertEquals(exception, error.getException());
        assertEquals((Integer) status.value(), error.getStatus());
    }

}
