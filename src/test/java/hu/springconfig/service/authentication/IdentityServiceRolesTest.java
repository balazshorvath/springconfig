package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.exception.ForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceRolesTest extends TestBase {
    @Autowired
    private IdentityService underTest;

    private Role managerLvl1;
    private Role managerLvl2;

    @Override
    @Before
    public void setup() {
        super.setup();
        managerLvl1 = new Role();
        managerLvl2 = new Role();

        managerLvl1.setRole("MANAGER_1");
        managerLvl1.setId(11);
        managerLvl2.setRole("MANAGER_2");
        managerLvl2.setId(12);

        when(roleService.get(11)).thenReturn(managerLvl1);
        when(roleService.get(12)).thenReturn(managerLvl2);

        when(identityRepository.save(any(Identity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testDeny() {
        Identity user = createIdentity(20L, userRole);
        Identity manager1 = createIdentity(21L, userRole, managerLvl1);
        Identity manager2 = createIdentity(22L, managerLvl2);
        mockIdentityDatabase(user, manager1);

        Identity updated = underTest.denyRoles(manager1, user.getId(), Collections.singleton(userRole));

        assertNotNull(updated);
        assertTrue(updated.getRoles().isEmpty());

        updated = underTest.denyRoles(manager2, manager1.getId(), Collections.singleton(managerLvl1));

        assertNotNull(updated);
        assertTrue(updated.getRoles().contains(userRole));
    }

    @Test
    public void testDenyRankTooLow() {
        Identity user = createIdentity(20L, userRole);
        Identity manager1 = createIdentity(21L, managerLvl1);
        Identity manager2 = createIdentity(22L, managerLvl1, managerLvl2);
        mockIdentityDatabase(user, manager1, manager2);

        ForbiddenException exception = null;
        // Identity tries to deny a role higher, than it's roles
        try {
            underTest.denyRoles(manager1, user.getId(), Collections.singleton(managerLvl2));
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.low_rank", exception.getMessage());
        exception = null;
        // Identity tries to deny a role from an identity with higher rank
        try {
            underTest.denyRoles(manager1, manager2.getId(), Collections.singleton(managerLvl1));
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.low_rank", exception.getMessage());
        exception = null;
    }

    @Test
    public void testGrant() {
        Identity user = createIdentity(20L, userRole);
        Identity manager1 = createIdentity(21L, managerLvl1);
        Identity manager2 = createIdentity(22L, managerLvl2);
        mockIdentityDatabase(user, manager1);

        Identity updated = underTest.grantRoles(manager1, user.getId(), Collections.singleton(managerLvl1));

        assertNotNull(updated);
        assertTrue(updated.getRoles().contains(userRole));
        assertTrue(updated.getRoles().contains(managerLvl1));

        updated = underTest.grantRoles(manager2, manager1.getId(), Collections.singleton(managerLvl2));

        assertNotNull(updated);
        assertTrue(updated.getRoles().contains(managerLvl1));
        assertTrue(updated.getRoles().contains(managerLvl2));
    }

    @Test
    public void testGrantRankTooLow() {
        Identity user = createIdentity(20L, userRole);
        Identity manager1 = createIdentity(21L, managerLvl1);
        Identity manager2 = createIdentity(22L, managerLvl2);
        mockIdentityDatabase(user, manager1, manager2);

        ForbiddenException exception = null;
        // Identity tries to grant a role higher, than it's roles
        try {
            underTest.grantRoles(manager1, user.getId(), Collections.singleton(managerLvl2));
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.low_rank", exception.getMessage());
        exception = null;
        // Identity tries to grant a role to an identity with higher rank
        try {
            underTest.grantRoles(manager1, manager2.getId(), Collections.singleton(managerLvl1));
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals("identity.low_rank", exception.getMessage());
        exception = null;
    }

    @Test
    public void testIdentity() {
        Identity identity1 = createIdentity(userRole, managerLvl1);
        Identity identity2 = createIdentity(userRole, managerLvl2);
        Identity identity3 = createIdentity(adminRole);
        Identity identity4 = createIdentity(userRole);
        Identity identity5 = createIdentity();

        assertEquals(managerLvl1, identity1.getHighestRole());
        assertEquals(managerLvl2, identity2.getHighestRole());
        assertEquals(adminRole, identity3.getHighestRole());
        assertEquals(userRole, identity4.getHighestRole());
        assertNull(identity5.getHighestRole());

        assertFalse(identity1.isSuperiorTo(identity1));
        assertFalse(identity1.isSuperiorTo(identity2));
        assertFalse(identity1.isSuperiorTo(identity3));
        assertTrue(identity1.isSuperiorTo(identity4));
        assertTrue(identity1.isSuperiorTo(identity5));

        assertTrue(identity2.isSuperiorTo(identity1));
        assertFalse(identity2.isSuperiorTo(identity2));
        assertFalse(identity2.isSuperiorTo(identity3));
        assertTrue(identity2.isSuperiorTo(identity4));
        assertTrue(identity2.isSuperiorTo(identity5));

        assertTrue(identity3.isSuperiorTo(identity1));
        assertTrue(identity3.isSuperiorTo(identity2));
        assertFalse(identity3.isSuperiorTo(identity3));
        assertTrue(identity3.isSuperiorTo(identity4));
        assertTrue(identity3.isSuperiorTo(identity5));

        assertFalse(identity4.isSuperiorTo(identity1));
        assertFalse(identity4.isSuperiorTo(identity2));
        assertFalse(identity4.isSuperiorTo(identity3));
        assertFalse(identity4.isSuperiorTo(identity4));
        assertTrue(identity4.isSuperiorTo(identity5));

        assertFalse(identity5.isSuperiorTo(identity1));
        assertFalse(identity5.isSuperiorTo(identity2));
        assertFalse(identity5.isSuperiorTo(identity3));
        assertFalse(identity5.isSuperiorTo(identity4));
        assertFalse(identity5.isSuperiorTo(identity5));
    }

}
