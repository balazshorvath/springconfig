package hu.springconfig.authentication;

import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.config.error.APIValidationError;
import hu.springconfig.config.message.HttpMessages;
import hu.springconfig.config.message.RoleMessages;
import hu.springconfig.data.dto.authentication.Credentials;
import hu.springconfig.data.dto.authentication.role.RoleCreate;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.exception.ValidationException;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.validator.error.FieldValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {Application.class}
)
public class RoleIntegrationTest extends IntegrationTestBase {

    @Test
    public void testRoleCRUD() {
        final Role adminRole = roleRepository.findOne(RoleService.ADMIN_ROLE_ID);
        final Set<Role> adminRoles = Collections.singleton(adminRole);
        final String username = "user";
        final String email = "user@user.com";

        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPassword(username);
        addIdentity(adminRoles, username, email);
        authenticateAndValidate(
                adminRoles.stream().map(Role::getId).collect(Collectors.toSet()),
                username,
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
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                validationError.getBody(),
                "role.validation.error",
                ValidationException.class,
                HttpStatus.CONFLICT
        );
        assertAPIValidationError(
                validationError.getBody().getError(),
                "role.validation.error",
                Role.class,
                new FieldValidationError("id", RoleMessages.ROLE_ID_NULL),
                new FieldValidationError("role", RoleMessages.ROLE_NAME_INVALID)
        );
        // Invalid values
        create = new RoleCreate();
        create.setId(1);
        create.setRole("");
        validationError = restTemplate.postForEntity("/role", create, APIValidationError.class);
        assertResponseEntity(error, HttpStatus.BAD_REQUEST);
        assertAPIError(
                validationError.getBody(),
                "role.validation.error",
                ValidationException.class,
                HttpStatus.CONFLICT
        );
        assertAPIValidationError(
                validationError.getBody().getError(),
                "role.validation.error",
                Role.class,
                new FieldValidationError("id", RoleMessages.ROLE_ID_RANGE),
                new FieldValidationError("role", RoleMessages.ROLE_NAME_INVALID)
        );
        // Success

        create.setId(100);
        create.setRole("MANAGER");
        Role role = restTemplate.postForObject("/role", create, Role.class);
        assertRole(role, 100, "MANAGER", Collections.emptySet());
        /*
         * Assign additional privileges
         */
        // False privilege id
        // Success
        /*
         * Alter
         */
        // Empty role name
        // Success
        /*
         * Query
         */
        /*
         * Delete
         */
    }

    private void assertRole(Role role, Integer id, String name, Set<Privilege> privileges) {
        assertNotNull(role);
        assertEquals(id, role.getId());
        assertEquals(name, role.getRole());
        assertEquals(privileges, role.getPrivileges());
    }
}
