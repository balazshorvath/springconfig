package hu.springconfig;

import hu.springconfig.data.entity.authentication.Identity;
import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.data.repository.authentication.IIdentityRepository;
import hu.springconfig.service.authentication.RoleService;
import hu.springconfig.validator.error.FieldValidationError;
import hu.springconfig.validator.error.TypeValidationError;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TestBase {
    @MockBean
    protected IIdentityRepository identityRepository;
    @MockBean
    protected RoleService roleService;
    @Autowired
    protected PasswordEncoder encoder;

    protected final Role adminRole = new Role();
    protected final Role userRole = new Role();

    @Before
    public void setup() {
        Set<Privilege> adminPrivileges = Arrays.stream(Privilege.Privileges.values()).map(Privilege::new).collect(Collectors.toSet());

        adminRole.setId(RoleService.ADMIN_ROLE_ID);
        adminRole.setPrivileges(adminPrivileges);
        adminRole.setRole("ADMIN");
        userRole.setId(RoleService.USER_ROLE_ID);
        userRole.setRole("USER");

        when(roleService.get(RoleService.ADMIN_ROLE_ID)).thenReturn(adminRole);
        when(roleService.get(RoleService.USER_ROLE_ID)).thenReturn(userRole);
    }

    protected Identity createIdentity(Role... roles) {
        return createIdentity(0L, "test", "test", "test", roles);
    }

    protected Identity createIdentity(Long id, Role... roles) {
        return createIdentity(id, "test", "test", "test", roles);
    }

    protected Identity createIdentity(Long id, String username, String email, String password, Role... roles) {
        Identity identity = new Identity();
        identity.setId(id);
        identity.setUsername(username);
        identity.setPassword(encoder.encode(password));
        identity.setEmail(email);
        identity.setRoles(roles == null ? null : new HashSet<>(Arrays.asList(roles)));
        return identity;
    }

    protected void mockIdentityDatabase(Identity... identities) {
        for (Identity identity : identities) {
            when(identityRepository.findOne(identity.getId())).thenReturn(new Identity(identity));
            when(identityRepository.findByUsername(identity.getUsername())).thenReturn(new Identity(identity));
        }
    }

    protected void assertIdentity(Identity identity, Long id, String username, String email, String password, Set<Role> roles) {
        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(email, identity.getEmail());
        assertEquals(roles, identity.getRoles());
        assertTrue(encoder.matches(password, identity.getPassword()));
    }

    public void assertValidationError(TypeValidationError error, String errorMessage, Class<?> type, FieldValidationError... fieldErrors) {
        assertNotNull(error);
        assertEquals(type, error.getType());
        assertEquals(errorMessage, error.getMessage());
        assertEquals(fieldErrors.length, error.getErrors().size());
        Set<FieldValidationError> errorSet = new HashSet<>(error.getErrors());
        for (FieldValidationError fieldValidationError : fieldErrors) {
            assertTrue(errorSet.contains(fieldValidationError));
        }
    }
}
