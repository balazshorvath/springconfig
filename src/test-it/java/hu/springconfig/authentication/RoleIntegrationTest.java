package hu.springconfig.authentication;

import hu.springconfig.Application;
import hu.springconfig.IntegrationTestBase;
import hu.springconfig.data.dto.authentication.role.RoleCreate;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.service.authentication.RoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
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
        RoleCreate create = new RoleCreate();
        create.setId(20);
        // Invalid JSON
        // Empty role name and id
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
