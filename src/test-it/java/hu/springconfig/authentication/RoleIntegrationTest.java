package hu.springconfig.authentication;

import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.config.error.APIError;
import hu.springconfig.data.dto.authentication.role.RoleCreate;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.BadRequestException;
import hu.springconfig.service.authentication.RoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;

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

        Identity identity = addIdentity(adminRoles, username, email);
        /*
         * Create role
         */
        // Invalid JSON

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{qwe}", headers);
        ResponseEntity<APIError> error = restTemplate.exchange("/role", HttpMethod.POST, entity, APIError.class);
        assertError(error, "http.bad.request", BadRequestException.class, HttpStatus.BAD_REQUEST);

        // Empty role name and id
        RoleCreate create = new RoleCreate();
        error = restTemplate.postForEntity("/role", create, APIError.class);
        assertError(error, "");
        // Success
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
}
