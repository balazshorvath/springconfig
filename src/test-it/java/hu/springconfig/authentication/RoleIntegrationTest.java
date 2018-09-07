package hu.springconfig.authentication;

import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.error.APIValidationError;
import hu.springconfig.config.message.application.HttpMessages;
import hu.springconfig.config.message.entity.RoleMessages;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.role.RoleCreate;
import hu.springconfig.data.dto.authentication.role.RoleUpdate;
import hu.springconfig.data.dto.simple.OKResponse;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.NotFoundException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.helper.CustomPageImpl;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.util.Util;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class RoleIntegrationTest extends IntegrationTestBase {

    @Test
    public void testRoleCRUD() throws IOException {
        final Role adminRole = roleRepository.findOne(RoleService.ADMIN_ROLE_ID);
        final Set<Role> adminRoles = Collections.singleton(adminRole);
        final String email = "user@user.com";

        Credentials credentials = new Credentials();
        credentials.setUsername(email);
        credentials.setPassword(email);
        addIdentity(adminRoles, email);
        authenticateAndValidate(
                adminRoles.stream().map(Role::getId).collect(Collectors.toSet()),
                email,
                credentials
        );
        /*
         * Create role
         */
        // Invalid JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{qwe}", headers);
        ResponseEntity<APIError> error = restTemplate.exchange("/role", HttpMethod.POST, entity, APIError.class);
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                error.getBody(),
                HttpMessages.HTTP_BAD_REQUEST,
                BadRequestException.class,
                HttpStatus.BAD_REQUEST
        );

        // Empty role name and id
        RoleCreate create = new RoleCreate();
        ResponseEntity<APIValidationError> validationError = restTemplate.postForEntity(
                "/role",
                create,
                APIValidationError.class
        );
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIError(
                validationError.getBody(),
                RoleMessages.ROLE_VALIDATION_ERROR,
                ValidationException.class,
                HttpStatus.CONFLICT
        );
        assertAPIValidationError(
                validationError.getBody().getError(),
                RoleMessages.ROLE_VALIDATION_ERROR,
                Role.class,
                new FieldValidationError("id", RoleMessages.ROLE_ID_NULL),
                new FieldValidationError("role", RoleMessages.ROLE_NAME_INVALID)
        );
        // Invalid values
        create = new RoleCreate();
        create.setId(1);
        create.setRole("");
        validationError = restTemplate.postForEntity("/role", create, APIValidationError.class);
        assertResponseEntity(validationError, HttpStatus.CONFLICT);
        assertAPIError(
                validationError.getBody(),
                RoleMessages.ROLE_VALIDATION_ERROR,
                ValidationException.class,
                HttpStatus.CONFLICT
        );
        assertAPIValidationError(
                validationError.getBody().getError(),
                RoleMessages.ROLE_VALIDATION_ERROR,
                Role.class,
                new FieldValidationError("role", RoleMessages.ROLE_NAME_INVALID)
        );

        // Success
        create.setId(100);
        create.setRole("MANAGER_2");
        Role role = restTemplate.postForObject("/role", create, Role.class);
        assertRole(role, 100, "MANAGER_2", Collections.emptySet());
        /*
         * Assign additional privileges
         */
        // False privilege id
        RoleUpdate update = new RoleUpdate();
        update.setId(100);
        update.setRole("MANAGER_2");
        update.setPrivileges(
                (Set<Integer>) Util.CollectionBuilder.<Integer>newSet()
                        .add(1000)
                        .get()
        );
        update.setVersion(role.getVersion());
        error = putForEntity("/role/" + role.getId(), update, APIError.class);
        assertResponseEntity(error, HttpStatus.NOT_FOUND);
        assertAPIError(
                error.getBody(),
                RoleMessages.PRIVILEGE_NOT_FOUND,
                NotFoundException.class,
                HttpStatus.NOT_FOUND
        );
        // Success
        update.setPrivileges(
                (Set<Integer>) Util.CollectionBuilder.<Integer>newSet()
                        .add(Privilege.Privileges.IDENTITY_GRANT.getValue())
                        .add(Privilege.Privileges.IDENTITY_DENY.getValue())
                        .get()
        );
        role = putForObject("/role/" + role.getId(), update, Role.class);

        Set<Integer> privilegeIds = role.getPrivileges().stream().map(Privilege::getId).collect(Collectors.toSet());
        assertEquals(2, privilegeIds.size());
        assertTrue(privilegeIds.contains(Privilege.Privileges.IDENTITY_GRANT.getValue()));
        assertTrue(privilegeIds.contains(Privilege.Privileges.IDENTITY_DENY.getValue()));
        /*
         * Alter
         */

        update.setId(10);
        update.setRole("MNGR_2");
        update.setVersion(role.getVersion());
        role = putForObject("/role/" + role.getId(), update, Role.class);
        assertRole(role, 10, "MNGR_2", role.getPrivileges());
        /*
         * Query
         */
        ParameterizedTypeReference<CustomPageImpl<Role>> accountPageTypeRef =
                new ParameterizedTypeReference<CustomPageImpl<Role>>() {
                };
        entity = new HttpEntity<>("", headers);
        ResponseEntity<CustomPageImpl<Role>> accounts = restTemplate.exchange(
                "/role/list",
                HttpMethod.POST,
                entity,
                accountPageTypeRef
        );
        assertNotNull(accounts.getBody());
        assertEquals(3, accounts.getBody().getNumberOfElements());
        /*
         * Delete
         */
        OKResponse response = deleteForObject("/role/" + role.getId(), OKResponse.class);
        assertNotNull(response);
        /*
         * Query
         */
        entity = new HttpEntity<>("", headers);
        accounts = restTemplate.exchange(
                "/role/list",
                HttpMethod.POST,
                entity,
                accountPageTypeRef
        );
        assertNotNull(accounts.getBody());
        assertEquals(2, accounts.getBody().getNumberOfElements());
    }

    private void assertRole(Role role, Integer id, String name, Set<Privilege> privileges) {
        assertNotNull(role);
        assertEquals(id, role.getId());
        assertEquals(name, role.getRole());
        assertEquals(privileges, role.getPrivileges());
    }
}
