package hu.springconfig;

import hu.springconfig.data.entity.authentication.Privilege;
import hu.springconfig.data.entity.authentication.Role;
import hu.springconfig.service.authentication.RoleService;
import org.junit.Before;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

public class TestBase {
    @MockBean
    protected RoleService roleService;

    protected final Role adminRole = new Role();
    protected final Role userRole = new Role();

    @Before
    public void setup(){
        Set<Privilege> adminPrivileges = (Arrays.asList(Privilege.Privileges.values()))
                .stream().map(Privilege::new).collect(Collectors.toSet());

        adminRole.setId(RoleService.ADMIN_ROLE_ID);
        adminRole.setPrivileges(adminPrivileges);
        adminRole.setRole("ADMIN");
        userRole.setId(RoleService.USER_ROLE_ID);
        userRole.setRole("USER");

        when(roleService.get(RoleService.ADMIN_ROLE_ID)).thenReturn(adminRole);
        when(roleService.get(RoleService.USER_ROLE_ID)).thenReturn(userRole);
    }

}
