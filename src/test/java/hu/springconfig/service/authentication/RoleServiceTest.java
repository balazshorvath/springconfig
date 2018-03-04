package hu.springconfig.service.authentication;

import hu.springconfig.TestApplication;
import hu.springconfig.config.message.RoleMessages;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.data.repository.authentication.IRoleRepository;
import hu.springconfig.exception.ForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RoleServiceTest {
    @MockBean
    protected IIdentityRepository identityRepository;
    @MockBean
    private IRoleRepository roleRepository;
    @Autowired
    private RoleService underTest;

    private final Role adminRole = new Role();
    private final Role userRole = new Role();

    @Before
    public void setup() {
        Set<Privilege> adminPrivileges = Arrays.stream(Privilege.Privileges.values()).map(Privilege::new).collect(
                Collectors.toSet());

        adminRole.setId(RoleService.ADMIN_ROLE_ID);
        adminRole.setPrivileges(adminPrivileges);
        adminRole.setRole("ADMIN");
        userRole.setId(RoleService.USER_ROLE_ID);
        userRole.setRole("USER");

        when(roleRepository.findOne(RoleService.ADMIN_ROLE_ID)).thenReturn(adminRole);
        when(roleRepository.findOne(RoleService.USER_ROLE_ID)).thenReturn(userRole);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testCreate() {
        Set<Privilege> privileges = new HashSet<>();
        final String newRole = "NewRole";
        privileges.add(new Privilege(Privilege.Privileges.IDENTITY_GET));
        privileges.add(new Privilege(Privilege.Privileges.IDENTITY_DENY));
        privileges.add(new Privilege(Privilege.Privileges.IDENTITY_GRANT));
        Role role = underTest.create(10, newRole, privileges);

        assertRole(role, 10, newRole, privileges);
    }

    @Test
    public void testUpdate() {
        Role role = new Role();
        role.setId(10);
        role.setRole("USER_2");
        when(roleRepository.findOne(role.getId())).thenReturn(role);

        Set<Privilege> privileges = new HashSet<>();
        privileges.add(new Privilege(Privilege.Privileges.IDENTITY_GET));
        Role updated = underTest.update(role.getId(), null, "USER_1", privileges, 1);

        assertRole(updated, role.getId(), "USER_1", privileges);
    }

    @Test
    public void testUpdateId() {
        Role role = new Role();
        role.setId(10);
        role.setRole("USER_2");
        when(roleRepository.findOne(role.getId())).thenReturn(role);

        Role updated = underTest.update(role.getId(), 11, "USER_1", null, 1);

        assertRole(updated, 11, "USER_1", null);
        verify(roleRepository, times(1)).delete(10);
    }

    @Test
    public void testUpdateStaticId() {
        ForbiddenException exception = null;
        try {
            underTest.update(userRole.getId(), 11, "USER_1", null, 1);
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(RoleMessages.ROLE_ID_STATIC, exception.getMessage());
    }

    @Test
    public void testUpdateStaticName() {
        ForbiddenException exception = null;
        try {
            underTest.update(userRole.getId(), null, "USER_1", null, 1);
        } catch (ForbiddenException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertEquals(RoleMessages.ROLE_ROLE_NAME_STATIC, exception.getMessage());
    }

    private void assertRole(Role role, Integer id, String roleName, Set<Privilege> privileges) {
        assertNotNull(role);
        assertEquals(id, role.getId());
        assertEquals(roleName, role.getRole());
        assertEquals(privileges, role.getPrivileges());
    }
}
