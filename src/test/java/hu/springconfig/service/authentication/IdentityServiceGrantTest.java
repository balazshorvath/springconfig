package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.TestBase;
import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdentityServiceGrantTest extends TestBase {
    @MockBean
    private IIdentityRepository identityRepository;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private BCryptPasswordEncoder encoder;

    private Role managerLvl1;
    private Role managerLvl2;

    @Override
    @Before
    public void setup(){
        super.setup();
        managerLvl1 = new Role();
        managerLvl2 = new Role();
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(new Privilege(Privilege.Privileges.IDENTITY_GRANT));

        managerLvl1.setPrivileges(privileges);
        managerLvl1.setRole("MANAGER_1");
        managerLvl1.setId(11);
        managerLvl2.setPrivileges(privileges);
        managerLvl2.setRole("MANAGER_2");
        managerLvl2.setId(12);

        when(roleService.get(11)).thenReturn(managerLvl1);
        when(roleService.get(12)).thenReturn(managerLvl2);
    }

    public void testGrant() {

    }

    @Test
    public void testIdentity(){
        Identity identity1 = new Identity();
        Identity identity2 = new Identity();
        Identity identity3 = new Identity();
        Identity identity4 = new Identity();
        Identity identity5 = new Identity();

        identity1.setRoles(new HashSet<>(2));
        identity1.getRoles().add(userRole);
        identity1.getRoles().add(managerLvl1);
        identity2.setRoles(new HashSet<>(2));
        identity2.getRoles().add(userRole);
        identity2.getRoles().add(managerLvl2);
        identity3.setRoles(Collections.singleton(adminRole));
        identity4.setRoles(Collections.singleton(userRole));

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
